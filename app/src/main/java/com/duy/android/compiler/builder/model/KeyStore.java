package com.duy.android.compiler.builder.model;

import java.util.Arrays;

public class KeyStore {
    private String keystore;
    private char[] keystorePassword;
    private String keyAlias;
    private char[] keyAliasPassword;

    @Override
    public String toString() {
        return "KeyStore{" +
                "keystore='" + keystore + '\'' +
                ", keystorePassword=" + Arrays.toString(keystorePassword) +
                ", keyAlias='" + keyAlias + '\'' +
                ", keyAliasPassword=" + Arrays.toString(keyAliasPassword) +
                '}';
    }

    public KeyStore(String keystore, char[] keystorePassword, String keyAlias, char[] keyAliasPassword) {
        this.keystore = keystore;
        this.keystorePassword = keystorePassword;
        this.keyAlias = keyAlias;
        this.keyAliasPassword = keyAliasPassword;
    }
}
