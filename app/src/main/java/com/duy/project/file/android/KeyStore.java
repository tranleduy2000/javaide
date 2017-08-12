package com.duy.project.file.android;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

/**
 * Created by Duy on 07-Aug-17.
 */

public class KeyStore implements Serializable, Cloneable {
    private File file;
    private char[] password;
    private String certAlias;
    private char[] certPassword;

    public KeyStore(File file, char[] password, String certAlias, char[] certPassword) {
        this.file = file;
        this.password = password;
        this.certAlias = certAlias;
        this.certPassword = certPassword;
    }

    public File getFile() throws IOException {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public char[] getPassword() {
        return password;
    }

    public void setPassword(char[] password) {
        this.password = password;
    }

    public String getCertAlias() {
        return certAlias;
    }

    public void setCertAlias(String certAlias) {
        this.certAlias = certAlias;
    }

    public char[] getCertPassword() {
        return certPassword;
    }

    public void setCertPassword(char[] certPassword) {
        this.certPassword = certPassword;
    }
}
