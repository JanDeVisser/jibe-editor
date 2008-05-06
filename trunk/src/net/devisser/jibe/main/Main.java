/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.devisser.jibe.main;

import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author jan
 */
public class Main {
  
  public static void main(String[] args) {
    String systemhome = System.getProperty("jibe.system");
    if (systemhome == null) {
      if (System.getenv("JIBE_DIR") != null) {
        systemhome = System.getenv("JIBE_DIR");
        System.setProperty("jibe.system", systemhome);
      } else {
        System.err.println("jibe.system not set");
        System.exit(1);
      }
    }
    String userhome = System.getProperty("user.home") + "/.jibe";
    if (System.getProperty("jibe.home") != null) {
      userhome = System.getProperty("jibe.home");
    } else if (System.getenv("JIBE_HOME") != null) {
      userhome = System.getenv("JIBE_HOME");
    }
    System.setProperty("jibe.home", userhome);
    
    boolean writable = false;
    try {
      File home = new File(userhome);
      if (home.exists() && !home.isDirectory()) {
        System.err.println("Jibe home directory " + userhome + " exists, but is not a directory");
      } else if (!home.exists()) {
        System.err.println("Jibe home directory " + userhome + " does not exist. Creating it now");
        writable = home.mkdir();
      } else if (!home.canWrite()) {
        System.err.println("Jibe home directory " + userhome + " exists but is not writable");
      } else {
        writable = true;
      }
    } catch (Exception ee) {
      System.err.println("Exception initializing Jibe home directory " + userhome);
      ee.printStackTrace();      
    }
    
    if (!writable) {
      System.err.println("Could not initialize Jibe. Terminating...");
      System.exit(1);
    }
    
    try {
      List<URL> jarURLs = new ArrayList<URL>();
      //jarURLs.add(new File(systemhome, "classes").toURI().toURL());
      jarURLs.addAll(getJars(new File(systemhome, "lib")));
      jarURLs.addAll(getJars(new File(userhome, "lib")));
      System.out.println("Jars added to classloader: " + jarURLs);
      ClassLoader cl = URLClassLoader.newInstance(jarURLs.toArray(new URL[0]), Main.class.getClassLoader());
      Thread.currentThread().setContextClassLoader(cl);
      Class c = cl.loadClass("net.devisser.jibe.Jibe");
      c.getMethod("main", List.class).invoke(null, Arrays.asList(args));
    } catch (Exception e) {
      e.printStackTrace();      
      System.err.println("Exception bootstrapping Jibe. Terminating...");
      System.exit(1);
    }
    
  }
  
  protected static List<URL> getJars(File libdir) {
    List<URL> ret = new ArrayList<URL>();

    if (libdir.exists() && libdir.isDirectory()) {
      File[] jars = libdir.listFiles(
        new FilenameFilter() {
          public boolean accept(File dir, String name) {
            return name.toLowerCase().endsWith(".jar");
          }
        });
      for (File j : jars) {
        try {
          ret.add(j.toURI().toURL());
        } catch (MalformedURLException mfue) {
          System.err.println("What? " + j + ".toURI().toURL() failed?");
        }
      }
    }
    return Collections.unmodifiableList(ret);
  }
  
}
