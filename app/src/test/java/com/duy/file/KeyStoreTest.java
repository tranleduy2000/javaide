package com.duy.file;

import com.duy.ide.utils.SecurityUtil;

import junit.framework.TestCase;

import java.io.File;

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
}
