package com.duy.ide.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.duy.ide.R;

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
 * Created by Duy on 19-Aug-17.
 */

public class CreateKeystoreActivity extends AbstractAppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_keystore);
    }

    public boolean createKeyStore() {
        try {
            File file = new File(getKeystorePath());
            KeyStore keyStore = KeyStore.getInstance("JKS");
            if (file.exists()) {
                keyStore.load(new FileInputStream(file), getPassword());
            }else {
                keyStore.load(null, null);
                keyStore.setCertificateEntry("", new C);
                keyStore.store(new FileOutputStream(file), getPassword());
            }
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

    private String getKeystorePath() {
        return null;
    }

    public char[] getPassword() {
        return password;
    }
}
