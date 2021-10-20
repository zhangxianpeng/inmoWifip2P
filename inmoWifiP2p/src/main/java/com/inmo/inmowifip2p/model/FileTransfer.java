package com.inmo.inmowifip2p.model;

import java.io.Serializable;

public class FileTransfer implements Serializable {

    private String fileName;
    private long fileLength;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getFileLength() {
        return fileLength;
    }

    public void setFileLength(long fileLength) {
        this.fileLength = fileLength;
    }
}