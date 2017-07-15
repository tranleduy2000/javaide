/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.spartacusrex.spartacuside.external;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author Spartacus Rex
 */
public class jping {

    public static void main(String[] zArgs) {
        if (zArgs.length < 1) {
            System.out.println("Usage : jping [WEB ADDRESS]");

        } else {
            try {
                InetAddress add = InetAddress.getByName(zArgs[0]);
                System.out.println("Address for " + zArgs[0] + " is : " + add.getHostAddress());
            } catch (UnknownHostException exc) {
                System.out.println("Unknown host : " + zArgs[0]);
            }
        }
    }
}
