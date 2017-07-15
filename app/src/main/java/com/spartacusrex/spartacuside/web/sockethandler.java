/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.spartacusrex.spartacuside.web;

import android.content.Context;
import android.util.Log;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.Date;

/**
 *
 * @author Spartacus Rex
 */
public class sockethandler implements Runnable {

    /*
     * Static Variables
     */
    public static final int BYTE_SKIP_AMOUNT     = 512*512;

    public static final int HTTP_OK              = 200;
    public static final int HTTP_NOT_FOUND      = 404;
    public static final int HTTP_BAD_METHOD     = 405;

    static final byte[] EOL = {(byte)'\r', (byte)'\n' };

    public static final int MAX_RANGE           = 100000;

    long mStartTime = 0;
    
    Context mContext;
    Socket  mSocket;
    boolean mRunning;
    Thread  mHandlerThread;

    webserver mServer;

    public void log(String zLog){
        Log.v("SpartacusRex", "SocketHandler - "+mStartTime+" "+zLog);
    }

    public sockethandler(Socket zSocket, Context zContext, webserver zServer){
        mSocket  = zSocket;
        mContext = zContext;
        mRunning = true;
        mServer  = zServer;
        
        mStartTime = System.currentTimeMillis();

        mHandlerThread = new Thread(this);
        mHandlerThread.start();
    }

    public void stop(){
        log("Stopped");
        mRunning = false;
        /*if(mHandlerThread != null){
            if(mHandlerThread.isAlive()){
                mHandlerThread.interrupt();
            }
        }*/
    }

    public void run() {
        log("Socket handler started");

        try {
            //Wait a second..
            Thread.sleep(250);

            //Get the streams..
            OutputStream os = mSocket.getOutputStream();
            InputStream is  = mSocket.getInputStream();

            //Convert to something useful
            BufferedReader br = new BufferedReader(new InputStreamReader(new BufferedInputStream(is,8192)),8192);
            PrintStream ps    = new PrintStream(os);

            //Read in complete command
            StringBuffer sbuffer = new StringBuffer("");
            String in            = null;
            boolean keepalive    = true;

            while(keepalive && mRunning){

//                while( mRunning && (in = br.readLine()) != null){
                if( mRunning && (in = br.readLine()) != null){
                    /*if(in.equals("")){
                        break;
                    }*/

                    //String in = br.readLine();
                    log("Server Read Line : "+in);
                    sbuffer.append(in+"\n");
                }

                //Log complete
                String com = sbuffer.toString();

                log("Server Complete  : "+com);

                //Write somethinfg to the stream
                ps.println("1 The server says hello!!");
                ps.println("2 The server says hello!!");
                ps.println("3 The server says hello!!");

                //Now process the command
                if(false && !com.equals("")){
                    //Get Command Part
                    int space1      = com.indexOf(" ");
                    int space2      = com.indexOf(" ", space1+1);
                    String command  = com.substring(0,space1).trim();
                    String getfile  = com.substring(space1,space2).trim();

                    String urlfile = URLDecoder.decode(getfile);

                    log("READ FILE : ."+urlfile);
                    
                    File httpfile         = new File(urlfile);
                    
                    boolean head = command.equalsIgnoreCase("HEAD");
                    boolean get  = command.equalsIgnoreCase("GET");

                    int index = com.indexOf("Keep-Alive");
                    keepalive = (index != -1);
                    //log("FULL COMMAND : "+com+" INDEX:"+index);

                    //Get the required range
                    long[] range = getRange(com);
                    if(range[0] == -1){
                        range[0] = 0;
                        range[1] = httpfile.length();
                        log("No Range Specified..!");
                    }

                    log("Write Head "+range[0]+" "+range[1]);
                    writeHead(ps, httpfile);
                    
                    //rangeavail = false;
                    if(get){
                        log("Write File xx  "+range[0]+" "+range[1]);
//                        writeFromStream(ps, httpfile,range);
                        writeFile(ps, httpfile,range);
                    }

                    ps.flush();

                }else{
                    //No Input
                    log("No Input - pausing..");
                    keepalive = false;
                    Thread.sleep(100);
                }

                log("keepalive "+keepalive);
                if(keepalive){
                    sbuffer = new StringBuffer("");
                    in      = null;
                }
            }
            
            //Close streams..
            ps.close();
            os.close();
            is.close();

            mSocket.close();

        } catch (Exception iOException) {
            log(iOException.toString());
        }

        //Tell the server
        mServer.sockethandlerFinished(this);

        log("Finished");
    }

    /*void sendFile(File targ, PrintStream ps) throws IOException {
        InputStream is = null;
        ps.write(EOL);
        
        byte[] buffer = new byte[8192];
        
        File input= new File(targ.getAbsolutePath());
        long size = input.length();

        log("Sending file "+targ.getPath());

        while(size < 256*1024){
            try {
                Thread.sleep(250);
            } catch (InterruptedException ex) {
                return;
            }

            log("Waiting for filer downlad "+size);

            size = input.length();
        }

        log("File ready "+targ.getPath());

        /*if(size >= streamdownloader.MAXIMUM_DOWNLOAD){
            //Limit reached..
            return;
        }*/

     /*   size -= BYTE_SKIP_AMOUNT;
        if(size <0){
            size = 0;
        }

        //Open the stream at the end..
        is = new FileInputStream(targ.getAbsolutePath());
        is.skip(size);
        
        try {
            log("Sendfile start "+targ.getPath()+" size "+targ.length()+" "+mRunning);
            int n;
            //while (mRunning && (n = is.read(buffer)) > 0) {
            int count =0;
            while(mRunning) {
                n = is.read(buffer);
                count += n;
                //log("Bytes read : "+n+"  / total "+count);
                if(n>0){
                    ps.write(buffer, 0, n);
                }else if(n==-1){
                    //EOF
                    mRunning = false;
                }else{
                    //Thread.sleep(100);
                }
            }
            log("Sendfile finish "+mRunning+" "+targ.getPath());

        }catch(Exception exc){
            log("Sendfile : "+exc.toString());

        } finally {
            is.close();
        }

    }*/

    protected void writeHead(PrintStream zStream, File zFile) throws IOException{
        zStream.print("HTTP/1.0 " + HTTP_OK +" OK");
        zStream.write(EOL);
        zStream.print("Server: Simple Android Java");
        zStream.write(EOL);
        zStream.print("Date: " + (new Date()));
        zStream.write(EOL);
        //zStream.print("Content-length: " + zFile.length());
        zStream.print("Content-length: 250000000");
        zStream.write(EOL);
        zStream.print("Last Modified: "  + (new Date()));
        zStream.write(EOL);
        zStream.print("Content-type: audio/mpeg");
        zStream.write(EOL);
        zStream.write(EOL);
    }

    /*protected void writeFromStream(PrintStream zStream, File zFile, long[] zRange) throws IOException, InterruptedException{
        byte[] bytes = new byte[8192];

        BufferedInputStream bin = new BufferedInputStream(mMusicStream);

        int counter = 0;
        while(mRunning){
            //Suck from stream..
            int ret = bin.read(bytes);

            if(ret>0){
                zStream.write(bytes,0,ret);
                counter += ret;
                log("Wrote "+ret+" "+counter);
                Thread.sleep(50);
            }else{
                log("Data Sound Stream Pause.. ");
                Thread.sleep(500);
            }
        }

        bin.close();

        log("Write from stream finished");
    }*/

    protected void writeFile(PrintStream zStream, File zFile, long[] zRange) throws IOException, InterruptedException{
        log("Sockethandler writeFile");
        
        long size  = zFile.length();
    
//        while(mRunning && (zRange[0] >= (zFile.length() - MAX_RANGE + 32))) {
        while(mRunning && size<BYTE_SKIP_AMOUNT) {
            //Pause.
            log("Pausing to buffer more of the stream "+size+" / "+BYTE_SKIP_AMOUNT);
            Thread.sleep(500);
            size  = zFile.length();
        }

        //Check still running..
        if(!mRunning){
           log("Sockethandler - not running.. exit");
           return;
        }

        //About to start music
        //log("About to start music..! "+mStartWriteFile);
        //Thread.sleep(5000);

        //Skip to start
        RandomAccessFile filein = new RandomAccessFile(zFile, "r");
        filein.seek(0);

        int bufsize      = 8192;
        byte[] bytes     = new byte[bufsize];
        int counter      = 0;

        int ret;
        boolean slowdown = false;
        int slowcount=0;
        while(mRunning){
            ret = filein.read(bytes);
            if(ret > 0){
                zStream.write(bytes, 0, ret);
                counter           += ret;
                
                //log("Output data : "+counter);
                if(slowdown){
                    Thread.sleep(100);
                    slowcount++;
                    if(slowcount > 10){
                        //log("Slow turned off..!");
                        slowdown  = false;
                        slowcount = 0;
                    }
                }
                
            }else{
                //log("ret == -1 - pause for a sec..");
                Thread.sleep(1000);
                slowcount = 0;
                slowdown  = true;
            }
        }

        filein.close();

        log("Finished Write.. count:"+counter+" running:"+mRunning);
    }

    protected long[] getRange(String zCommand){
        String lower = zCommand.toLowerCase();
        long[] ret = new long[2];

        int index = lower.indexOf("range");

        ret[0] = -1;
        ret[1] = -1;

        if(index != -1){
            int start  = lower.indexOf( "=", index)+1;
            int equals = lower.indexOf( "-", index);
            int eol   = lower.indexOf( "\n", index);

            String substring1 = lower.substring(start, equals);
            String substring2 = lower.substring(equals+1, eol);

            ret[0] = Long.parseLong(substring1);
            ret[1] = Long.parseLong(substring2);
        }

        //if(ret[1] - ret[0] > MAX_RANGE){
        //   ret[1] =  ret[0] + MAX_RANGE;
        //}

        return ret;
    }
}
