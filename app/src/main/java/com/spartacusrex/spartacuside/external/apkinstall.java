/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.spartacusrex.spartacuside.external;

import android.content.Intent;
import android.net.Uri;

import java.io.File;

/**
 * @author Spartacus Rex
 */
public class apkinstall {
    public static void main(String[] zArgs) {
        if (zArgs.length < 1) {
            System.out.println("Must specify the APK file..!");
            System.exit(1);
        }

        File apkFile = new File(zArgs[0]);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");

        //Term.IntentLinker(intent);
    }
}
