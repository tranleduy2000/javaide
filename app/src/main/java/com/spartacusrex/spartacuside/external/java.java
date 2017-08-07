/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.spartacusrex.spartacuside.external;

import com.spartacusrex.spartacuside.external.dexloader.dexclassloader;

import java.lang.reflect.Method;

/**
 * @author Spartacus Rex
 */
public class java {

    public static void usage() {
        System.out.println("Usage : java -v -jar [List of Jar files] CLASSNAME");
    }

    public static void main(String[] zArgs) {
        try {
            String dexfolder = null;
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

            //Environment Variables..
            dexfolder = System.getenv("ODEX_FOLDER");
            if (dexfolder == null || dexfolder.equals("")) {
                //Try the TEMP Folder
                //System.out.println("No ODEX_FOLDER environment variable specified. Using TEMP");
                dexfolder = System.getenv("TEMP");
                if (dexfolder == null || dexfolder.equals("")) {
                    System.out.println("No TEMP OR ODEX_FOLDER specified!");
                    throw new InvokeException("Please specify ODEX_FOLDER or TEMP environment variable");
                }
            }

            //Output INFO
            if (verbose) {
                System.out.println("ODEX_FOLDER  : " + dexfolder);
                System.out.println("JAR/DEX FILE : " + jarfile);
                System.out.println("CLASSNAME    : " + classname);
            }

            //Check wee have the info we need..
            if (jarfile.equals("") || classname.equals("")) {
                throw new InvokeException("Incorrect parameters");
            }

            //Now load this class..
            //DexClassLoader loader = new DexClassLoader(jarfile, dexfolder, null, ClassLoader.getSystemClassLoader());
            dexclassloader loader = new dexclassloader(jarfile, dexfolder, null, ClassLoader.getSystemClassLoader(), verbose);
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

        } catch (Exception ex) {
            ex.printStackTrace();
            usage();
            //Logger.getLogger(java.class.getName()).log(Level.SEVERE, null, ex);
        } /*catch (IllegalAccessException ex) {     s//Err..
            Logger.getLogger(java.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(java.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger(java.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(java.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(java.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(java.class.getName()).log(Level.SEVERE, null, ex);
        }*/
    }
}
