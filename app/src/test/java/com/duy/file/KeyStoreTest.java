package com.duy.file;

import com.duy.ide.utils.SecurityUtil;

import junit.framework.TestCase;

import java.io.File;
import java.util.Enumeration;
import java.util.ResourceBundle;
import java.util.Set;

import sun.security.tools.KeyTool;

/**
 * Created by Duy on 22-Aug-17.
 */

public class KeyStoreTest extends TestCase {

    public void testCreate() {
        File file = new File("C:\\github\\javaide2\\testfile\\keystore.jks");
        boolean success = SecurityUtil.createAndroidKeyStore(file, "1234123123".toCharArray());
        assertEquals(success, true);
    }

    public void testKeytool() throws Exception {
        KeyTool.main(new String[]{});
    }

    public void testReadResource() {
        ResourceBundle resourceBundle = ResourceBundle.getBundle("sun.security.util.Resources");
        Enumeration<String> keys = resourceBundle.getKeys();
        while (keys.hasMoreElements()) {
            String s = keys.nextElement();
            System.out.println(s + " - " + resourceBundle.getString(s));
        }
    }

    public void testReadResource1() {
        ResourceBundle bundle = ResourceBundle.getBundle("sun.security.util.Resources");
        Set<String> strings = bundle.keySet();
        System.out.println(strings);
        Enumeration<String> keys = bundle.getKeys();
        int i = 0;
        while (keys.hasMoreElements()) {
            String s = keys.nextElement();
            System.out.println(i + ": " + s);
            i++;
        }
    }
}
