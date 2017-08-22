package com.duy.ide.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

/**
 * Created by Duy on 22-Aug-17.
 */

public class SecurityUtil {
    /**
     * @param file - the file to save keystore
     */
    public static boolean createAndroidKeyStore(File file, char[] pass) {
        try {
            KeyStore keyStore = KeyStore.getInstance("JKS");
            if (file.exists()) {
                keyStore.load(new FileInputStream(file), pass);
            } else {
                keyStore.load(null, null);
                keyStore.store(new FileOutputStream(file), pass);
            }
            return true;
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
