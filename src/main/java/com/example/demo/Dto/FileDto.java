package com.example.demo.Dto;

public class FileDto {

    private  String folderIdParent;

    private String contentType;

    private  String customFileName;

    private  String webContentLink;

    private  String webViewLink;

    public String getWebContentLink() {
        return webContentLink;
    }

    public void setWebContentLink(String webContentLink) {
        this.webContentLink = webContentLink;
    }

    public String getWebViewLink() {
        return webViewLink;
    }

    public void setWebViewLink(String webViewLink) {
        this.webViewLink = webViewLink;
    }

    public String getFolderIdParent() {
        return folderIdParent;
    }

    public void setFolderIdParent(String folderIdParent) {
        this.folderIdParent = folderIdParent;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getCustomFileName() {
        return customFileName;
    }

    public void setCustomFileName(String customFileName) {
        this.customFileName = customFileName;
    }
}
