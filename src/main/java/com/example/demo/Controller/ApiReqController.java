/*
 * Copyright 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.demo.Controller;

import com.example.demo.Dto.FileDto;
import com.example.demo.Dto.FolderDto;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.example.demo.Util.CreateFolder.createGoogleFolder;
import static com.example.demo.Util.CreateGoogleFile.createGoogleFile;
import static com.example.demo.Util.FindFilesByName.getGoogleFilesByName;
import static com.example.demo.Util.GetSubFolders.getGoogleRootFolders;
import static com.example.demo.Util.GetSubFoldersByName.getGoogleRootFoldersByName;
import static com.example.demo.Util.ShareGoogleFile.createPermissionForEmail;

@RestController
public class ApiReqController {

  private static final Logger logger = Logger.getLogger(ApiReqController.class.getName());


  private static final String APPLICATION_NAME = "Google Drive API Java Quickstart";

  private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

  private static final String CLIENT_SECRET_FILE_NAME = "client_secret.json";

  private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE);

  // Directory to store user credentials for this application.
  private static final java.io.File CREDENTIALS_FOLDER //
          = new java.io.File(System.getProperty("user.home"), "credentials");

  private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {

    java.io.File clientSecretFilePath= ResourceUtils.getFile("classpath:credentials/"+CLIENT_SECRET_FILE_NAME);

    java.io.File credentials_folder= ResourceUtils.getFile("classpath:credentials");

    if (!clientSecretFilePath.exists()) {
      throw new FileNotFoundException("Please copy " + CLIENT_SECRET_FILE_NAME //
              + " to folder: " + credentials_folder.getAbsolutePath());
    }

    // Load client secrets.
    InputStream in = new FileInputStream(clientSecretFilePath);

    GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

    // Build flow and trigger user authorization request.
    GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY,
            clientSecrets, SCOPES).setDataStoreFactory(new FileDataStoreFactory(credentials_folder))
            .setAccessType("offline").build();

    return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
  }

  @GetMapping(value = "/")
  public List<FolderDto> createCredential() throws IOException, GeneralSecurityException {
    logger.log(Level.INFO, "Retrieved list of all books");
    java.io.File credentials_folder= ResourceUtils.getFile("classpath:credentials");

    System.out.println("CREDENTIALS_FOLDER: " + credentials_folder.getAbsolutePath());
    List<FolderDto> listFiles= new ArrayList<>();
    // 1: Create CREDENTIALS_FOLDER
    if (!credentials_folder.exists()) {
      credentials_folder.mkdirs();

      System.out.println("Created Folder: " + credentials_folder.getAbsolutePath());
      System.out.println("Copy file " + CLIENT_SECRET_FILE_NAME + " into folder above.. and rerun this class!!");

    }

    // 2: Build a new authorized API client service.
    final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

    // 3: Read client_secret.json file & create Credential object.
    Credential credential = getCredentials(HTTP_TRANSPORT);

    // 5: Create Google Drive Service.
    Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential) //
            .setApplicationName(APPLICATION_NAME).build();

    // Print the names and IDs for up to 10 files.
    FileList result = service.files().list().setPageSize(10).setFields("nextPageToken, files(id, name)").execute();
    List<com.google.api.services.drive.model.File> files = result.getFiles();
    if (files == null || files.isEmpty()) {
      System.out.println("No files found.");
    } else {
      System.out.println("Files:");
      return getInfo(files);
    }

    return listFiles;
  }

  @GetMapping(value = "/folder/list")
  public List<FolderDto> getSubFolders() throws IOException, GeneralSecurityException {
    List<File> googleRootFolders = getGoogleRootFolders();

    System.out.println("Servicio que devuelve el listado de carpetas que existe en drive");
    return getInfo(googleRootFolders);
  }

  @GetMapping(value = "/folder/name/{name}")
  public List<FolderDto> getSubFolderByName( @PathVariable("name") String name) throws IOException, GeneralSecurityException {
    List<File> rootGoogleFolders = getGoogleRootFoldersByName(name);

    System.out.println("Servicio que devuelve el listado de carpetas que existe en drive con el nombre ingresado");
    return getInfo(rootGoogleFolders);
  }

  @GetMapping(value = "/file/name/{name}")
  public List<FileDto> getFileByName(@PathVariable("name") String name) throws IOException, GeneralSecurityException {
    List<File> rootGoogleFolders = getGoogleFilesByName(name);

    List<FileDto> listFileResponse= new ArrayList<>();
    for (File folder : rootGoogleFolders) {

      FileDto fileDto = new FileDto();
      fileDto.setFolderIdParent(folder.getId());
      fileDto.setCustomFileName(folder.getName());
      fileDto.setContentType(folder.getMimeType());
      fileDto.setWebContentLink(folder.getWebContentLink());
      fileDto.setWebViewLink(folder.getWebViewLink());

      listFileResponse.add(fileDto);

    }

    return listFileResponse;
  }

  @PostMapping(path = "/folder", consumes = "application/json", produces = "application/json")
  public FolderDto createFolder(@RequestBody FolderDto folderDto) throws IOException, GeneralSecurityException {

    //Si el FolderId es nulo la carpeta se creara en el directorio Raiz
    File folder = createGoogleFolder(folderDto.getFolderId(), folderDto.getFolderName());

    FolderDto info= new FolderDto();
    System.out.println("Created folder with id= "+ folder.getId());
    System.out.println("                    name= "+ folder.getName());
    info.setFolderId(folder.getId());
    info.setFolderName(folder.getName());

    return info;

  }

  @PostMapping(path = "/file", consumes = "application/json", produces = "application/json")
  public FileDto createFile(@RequestBody FileDto fileRequest) throws IOException, GeneralSecurityException {

    java.io.File uploadFile= ResourceUtils.getFile("classpath:templates/D520.html");

    // Create Google File:

    File googleFile = createGoogleFile(fileRequest.getFolderIdParent(), fileRequest.getContentType(), fileRequest.getCustomFileName(), uploadFile);

    System.out.println("Created Google file!");
    System.out.println("WebContentLink: " + googleFile.getWebContentLink() );
    System.out.println("WebViewLink: " + googleFile.getWebViewLink() );

    FileDto fileResponse = new FileDto();
    fileResponse.setFolderIdParent(googleFile.getId());
    fileResponse.setWebContentLink(googleFile.getWebContentLink());
    fileResponse.setWebViewLink(googleFile.getWebViewLink());

    return fileResponse;

  }

  @PostMapping(path = "/file/share", consumes = "application/json", produces = "application/json")
  public String shareFile(String googleFileId, String googleEmail) throws IOException, GeneralSecurityException {

    // Por politicas de la cuenta No se puede compartir un elemento fuera de Bbva.com
    createPermissionForEmail(googleFileId, googleEmail);

//        String googleFileId2 = "some-google-file-id-2";

    // Share for everyone
//        createPublicPermission(googleFileId2);
    return "se compartio el archivo con "+googleEmail;

  }

  private List<FolderDto> getInfo(List<File> googleRootFiles)
  {
    List<FolderDto> listFiles= new ArrayList<>();
    for (File folder : googleRootFiles) {

      FolderDto info= new FolderDto();
      info.setFolderName(folder.getName());
      info.setFolderId(folder.getId());
      listFiles.add(info);
    }
    return listFiles;

  }
}
