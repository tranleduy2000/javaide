/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.spartacusrex.spartacuside.util;

/**
 *
 * @author Spartacus Rex
 */
public class keydata {
    public int mKeyCode;
    public long mTime;

    public keydata(int zKeyCode){
        mKeyCode = zKeyCode;
        mTime    = System.currentTimeMillis();
    }

    public keydata(keydata zData){
        mKeyCode = zData.mKeyCode;
        mTime    = zData.mTime;
    }
}
