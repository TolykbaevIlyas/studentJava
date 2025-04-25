package com.example.Lab.dto;

import com.example.Lab.dto.EmailRequest;

public class EmailAttachmentRequest extends EmailRequest {
    private String pathToAttachment;

    public String getPathToAttachment() {
        return pathToAttachment;
    }

    public void setPathToAttachment(String pathToAttachment) {
        this.pathToAttachment = pathToAttachment;
    }
}
