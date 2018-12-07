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
import com.google.api.services.drive.model.FileList;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

@RestController
public class HelloworldController {

  private static final String APPLICATION_NAME = "Google Drive API Java Quickstart";

  private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

  // Directory to store user credentials for this application.
//    private static final java.io.File CREDENTIALS_FOLDER //
//            = new java.io.File(System.getProperty("user.home"), "credentials");

  private static final String CLIENT_SECRET_FILE_NAME = "client_secret.json";

  private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE);

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
  public String createCredencial() throws IOException, GeneralSecurityException {

    java.io.File credentials_folder= ResourceUtils.getFile("classpath:credentials");

    System.out.println("CREDENTIALS_FOLDER: " + credentials_folder.getAbsolutePath());

    // 1: Create CREDENTIALS_FOLDER
    if (!credentials_folder.exists()) {
      credentials_folder.mkdirs();

      System.out.println("Created Folder: " + credentials_folder.getAbsolutePath());
      System.out.println("Copy file " + CLIENT_SECRET_FILE_NAME + " into folder above.. and rerun this class!!");
      return "Carpeta Creada";
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
      for (com.google.api.services.drive.model.File file : files) {
        System.out.printf("%s (%s)\n", file.getName(), file.getId());
      }
    }
    return "Hola Mundo Entelgy Google Cloud";
  }
}
