/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.spartacusrex.spartacuside.external;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.logging.Level;
import java.util.logging.Logger;

import kellinwood.security.zipsigner.ZipSigner;

/**
 * @author Spartacus Rex
 */
public class jarsigner {


    public static void usage() {
        System.out.println("USAGE : jarsigner [inputfile] [outputfile]");
        System.out.println("Currently always uses the TEST_KEY");
    }

    public static void main(String[] zArgs) {
        try {
            //Check args
            if (zArgs.length != 2) {
                usage();
                System.exit(1);
            }

            ZipSigner zipsigner = new ZipSigner();

            zipsigner.setKeymode(ZipSigner.KEY_TESTKEY);

            //Create URL
            //URL keystoreUrl=new URL("");

            //Get the default asset
            //String keystore="/assets/keystore.ks";
            //keystoreUrl = keystoreUrl.getClass().getResource( keystore);

            zipsigner.signZip(zArgs[0], zArgs[1]);

//
//              if (keystore != null) keystoreUrl = new URL( keystore);
//              else {
//                  keystore = "/assets/keystore.ks";
//                  keystoreUrl = getClass().getResource( keystore);
//                  if (keystoreUrl == null) keystore = "classpath:"+keystore;
//              }
//
//              if (keystoreUrl == null) throw new IllegalArgumentException("Unable to locate keystore " + keystore);
//              zipSigner.signZip(keystoreUrl, keystoreType, keystorePass, keyAlias, keyPass, inputFile, outputFile);



            /*if(zArgs.length>2){
                zipsigner.setKeymode(zArgs[2]);
            }else{
                System.out.println("No key set. testkey used.");
                zipsigner.setKeymode("testkey");
            }*/

            /*zipsigner.addProgressListener(new ProgressListener() {

                public void onProgress(ProgressEvent event) {
                    String message = event.getMessage();
                    int percentDone = event.getPercentDone();
                    // log output or update the display here
                    System.out.println("Message : " + message + " @ " + percentDone + "%");
                }
            });
            zipsigner.signZip(zArgs[0], zArgs[1]);
            */

            System.out.println("Sign finished..!");

        } catch (IOException ex) {
            Logger.getLogger(jarsigner.class.getName()).log(Level.SEVERE, null, ex);
        } catch (GeneralSecurityException ex) {
            Logger.getLogger(jarsigner.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
