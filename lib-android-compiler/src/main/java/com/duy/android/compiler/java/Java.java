package com.duy.android.compiler.java;

import android.support.annotation.Nullable;

import com.android.utils.FileUtils;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;

/**
 * Created by duy on 19/07/2017.
 */

public class Java {

    public static void run(String[] zArgs, @Nullable String tempDir, @Nullable InputStream in) throws Throwable {
        InputStream oldStdIn = System.in;
        if (in != null) System.setIn(in);
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
            Class loadedClass = loader.loadClass(classname);

            //Now sort the command line inputs
            String[] mainArgs;
            if (pargspos != -1) {
                int args = argnum - pargspos;
                mainArgs = new String[args];
                System.arraycopy(zArgs, pargspos, mainArgs, 0, args);
            } else {
                mainArgs = new String[0];
            }

            //Gat public static void main
            @SuppressWarnings({"unchecked", "RedundantArrayCreation"})
            Method main = loadedClass.getDeclaredMethod("main", new Class[]{mainArgs.getClass()});

            //Invoke main method..
            if (verbose) {
                System.out.println("Main parameters : " + mainArgs.length + " parameters");
                for (String par : mainArgs) {
                    System.out.println("Param : " + par);
                }
            }
            //invoke static
            main.invoke(null, new Object[]{mainArgs});

            FileUtils.emptyFolder(new File(tempDir));
        } catch (InvocationTargetException e) {
            throw e.getCause();
        } catch (Throwable internal) {
            internal.printStackTrace();
        }
        //restore std
        System.setIn(oldStdIn);
    }
}
