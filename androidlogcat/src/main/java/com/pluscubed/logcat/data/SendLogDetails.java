package com.pluscubed.logcat.data;

import java.io.File;

public class SendLogDetails {

    private String subject;
    private String body;
    private File attachment;
    private AttachmentType attachmentType;

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public File getAttachment() {
        return attachment;
    }

    public void setAttachment(File attachment) {
        this.attachment = attachment;
    }

    public AttachmentType getAttachmentType() {
        return attachmentType;
    }

    public void setAttachmentType(AttachmentType attachmentType) {
        this.attachmentType = attachmentType;
    }

    public enum AttachmentType {
        None("text/plain"),
        Zip("application/zip"),
        Text("application/*");

        private String mimeType;

        AttachmentType(String mimeType) {
            this.mimeType = mimeType;
        }

        public String getMimeType() {
            return this.mimeType;
        }
    }
}
