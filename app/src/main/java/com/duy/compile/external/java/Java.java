package com.duy.compile.external.java;

import android.support.annotation.Nullable;

import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;

/**
 * Created by duy on 19/07/2017.
 */

public class Java {
    public static void usage() {
        System.out.println("Usage : java -v -jar [List of Jar files] CLASSNAME");
    }

    public static void main(String[] zArgs) {
        run(zArgs, null, null, null, null);
    }

    public static void run(String[] zArgs, @Nullable String tempDir,
                           @Nullable PrintStream out,
                           @Nullable InputStream in,
                           @Nullable PrintStream err) {
        PrintStream stdout = System.out;
        PrintStream stderr = System.err;
        InputStream stdin = System.in;

        if (out != null) System.setOut(out);
        if (in != null) System.setIn(in);
        if (err != null) System.setErr(err);

        try {
            String jarfile = "";
            String classname = "";
            boolean verbose = false;

            //Cycle through the args
            int pargspos = -1;
            int argnum = zArgs.length;
            for (int i = 0; i < argnum; i++) {
                if (zArgs[i].equals("-jar")) {
                    //Its the JAR file
                    if (i < argnum - 1) {
                        i++;
                        jarfile = zArgs[i];
                    } else {
                        //Wrong Varibles
                        throw new InvokeException("Wrong parameters. No JAR file specified.");
                    }
                } else if (zArgs[i].equals("-v") || zArgs[i].equals("-verbose")) {
                    //Property set
                    verbose = true;

                } else {
                    //it's the class
                    classname = zArgs[i];

                    //The rest are the variables to pass on..
                    if (i < argnum - 1) {
                        //Pass em on..
                        pargspos = i + 1;
                    }

                    break;
                }
            }

            if (jarfile.equals("")) {
                throw new InvokeException("No JAR Files specified");
            }

            if (tempDir == null) {
                //Environment Variables..
                tempDir = System.getenv("ODEX_FOLDER");
                if (tempDir == null || tempDir.equals("")) {
                    //Try the TEMP Folder
                    //System.out.println("No ODEX_FOLDER environment variable specified. Using TEMP");
                    tempDir = System.getenv("TEMP");
                    if (tempDir == null || tempDir.equals("")) {
                        System.out.println("No TEMP OR ODEX_FOLDER specified!");
                        throw new InvokeException("Please specify ODEX_FOLDER or TEMP environment variable");
                    }
                }
            }

            //Output INFO
            if (verbose) {
                System.out.println("ODEX_FOLDER  : " + tempDir);
                System.out.println("JAR/DEX FILE : " + jarfile);
                System.out.println("CLASSNAME    : " + classname);
            }

            //Check wee have the info we need..
            if (jarfile.equals("") || classname.equals("")) {
                throw new InvokeException("Incorrect parameters");
            }

            //Now load this class..
            DexClassLoader loader = new DexClassLoader(jarfile, tempDir, null, ClassLoader.getSystemClassLoader());
//            DexClassLoader loader = new DexClassLoader(jarfile, tempDir, null, ClassLoader.getSystemClassLoader(), verbose);
            Class loadedclass = loader.loadClass(classname);

            //Now sort the command line inputs
            String[] mainargs;
            if (pargspos != -1) {
                int args = argnum - pargspos;
                mainargs = new String[args];
                for (int i = 0; i < args; i++) {
                    mainargs[i] = zArgs[pargspos + i];
                }
            } else {
                mainargs = new String[0];
            }

            //Gat public static void main
            Class[] ptypes = new Class[]{mainargs.getClass()};
            Method main = loadedclass.getDeclaredMethod("main", ptypes);
            //String[] pargs = new String[mainargs.length - 1];
            //System.arraycopy(mainargs, 1, pargs, 0, pargs.length);

            //Invoke main method..
            if (verbose) {
                System.out.println("Main parameters : " + mainargs.length + " parameters");
                for (String par : mainargs) {
                    System.out.println("Param : " + par);
                }
            }

//            main.invoke(null, new Object[]{pargs});

            main.invoke(null, new Object[]{mainargs});

            //restore std
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
            usage();
        }
        System.setIn(stdin);
        System.setOut(stdout);
        System.setErr(stderr);
    }
}
