/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.spartacusrex.spartacuside.web;

import android.content.Context;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Enumeration;
import java.util.Vector;
import javax.net.ServerSocketFactory;

/**
 *
 * @author Spartacus Rex
 */
public class webserver implements Runnable{

    Thread mMainThread;
    boolean mRunning;
    
    ServerSocket mMainServer;
    sockethandler mLastHandler;
    
    Context mContext;

    Vector  mHandlers;
    boolean mStopMode = false;

    
    public webserver(Context zContext){
        mContext    = zContext;
        mHandlers = new Vector();
        mStopMode = false;
    }

    public void log(String zLog){
        Log.v("SpartacusRex", "AppServer - "+zLog);
    }

    public void start(){
        log("START THE WEBSERVER!!!");
        mRunning    = true;
        mMainThread = new Thread(this);
        mMainThread.start();
    }

    public void stop(){
        log("WEBSERVER STOPPING");
        mStopMode = true;
        if(mMainServer!=null){
            try {
                if(!mMainServer.isClosed()){
                    mMainServer.close();
                }
            } catch (IOException iOException) {
                log("server stop - "+iOException.toString());
            }

            //Stop all the socket handlers
            for(Enumeration e=mHandlers.elements();e.hasMoreElements();){
                sockethandler handler = (sockethandler)e.nextElement();
                handler.stop();
            }
        }

        mRunning = false;
        mMainThread.interrupt();
    }

    public void run() {
        log("Server Started");

        mMainServer = null;
        Socket sock = null;
        try {
            //Open a socket..
            mMainServer = ServerSocketFactory.getDefault().createServerSocket(10000);
            mMainServer.setSoTimeout(0);

            while(mRunning){
                sock = mMainServer.accept();
                sock.setSoTimeout(0);
                sock.setTcpNoDelay(true);
                
                log("Client accepted");
                if(mLastHandler != null){
                   mLastHandler.stop();
                }
                sockethandler handler = new sockethandler(sock,mContext,this);
                mLastHandler = handler;
            
                //Add to our list
                mHandlers.add(handler);
            }

        }catch (Exception iOException) {
            log("Client accept "+iOException.toString());
            
        }

        try {
            if(mMainServer!=null){
                if(!mMainServer.isClosed()){
                    mMainServer.close();
                }
            }
        } catch (IOException iOException1) {
            log("internal - "+iOException1.toString());
        }

        log("Server finished");
    }

    public void sockethandlerFinished(sockethandler zHandler){
        if(!mStopMode){
            mHandlers.remove(zHandler);
        }
    }
}
