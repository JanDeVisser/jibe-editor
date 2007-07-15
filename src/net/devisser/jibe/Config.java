/*************************************************************************
 **
 ** Copyright (C) 2007      Jan de Visser. All rights reserved.
 **
 ** This file may be used under the terms of the GNU General Public
 ** License version 2.0 as published by the Free Software Foundation
 ** and appearing in the file LICENSE.GPL included in the packaging of
 ** this file.  Please review the following information to ensure GNU
 ** General Public Licensing requirements will be met:
 ** http://www.trolltech.com/products/qt/opensource.html
 **
 ** This file is provided AS IS with NO WARRANTY OF ANY KIND, INCLUDING THE
 ** WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 **
 ****************************************************************************/

package net.devisser.jibe;

import java.io.*;
import java.util.*;

public class Config {

  private static Properties s_props = new Properties();
  private static String s_home;
  private static File s_propsfile = null;
  private static boolean s_hold = false;
  private static boolean s_dirty = false;

  static {
    s_home = System.getProperty("user.home") + "/.jibe";
    if (System.getProperty("jibe.home") != null) {
      s_home = System.getProperty("jibe.home");
    } else if (System.getenv("JIBE_HOME") != null) {
      s_home = System.getenv("JIBE_HOME");
    }
    
    boolean writable = false;
    try {
      File home = new File(s_home);
      if (home.exists() && !home.isDirectory()) {
        System.err.println("Jibe home directory " + s_home + " exists, but is not a directory");
      } else if (!home.exists()) {
        System.err.println("Jibe home directory " + s_home + " does not exist. Creating it now");
        home.mkdir();
        writable = true;
      } else if (!home.canWrite()) {
        System.err.println("Jibe home directory " + s_home + " exists but is not writable");
      } else {
        writable = true;
      }
    } catch (Exception ee) {
      System.err.println("Exception initializing Jibe home directory " + s_home);
      ee.printStackTrace();      
    }
    
    try {
      s_propsfile = new File(s_home, "jibe.properties");
      if (s_propsfile.exists()) {
        FileInputStream fis = new FileInputStream(s_propsfile);
        if (writable) writable = s_propsfile.canWrite();
        try {
          s_props.load(fis);
        } finally {
          fis.close();
        }
      } else {
        System.err.println("No Jibe properties file " + s_propsfile.getAbsolutePath());
      }
    } catch (Exception e) {
      System.err.println("Exception reading " + s_home + "/jibe.properties");
      e.printStackTrace();
    } finally {
      if (!writable) s_propsfile = null;
    }
  }
  
  public static String getHome() {
    return s_home;
  }
  
  public static String getProperty(String name, String def) {
    return s_props.getProperty(name, def);
  }

  public static String getProperty(String name) {
    return s_props.getProperty(name);
  }
  
  public static int getIntProperty(String name, int def) {
    String val = getProperty(name);
    try {
      return (val == null) ? def : Integer.parseInt(val);
    } catch (NumberFormatException nfe) {
      return 0;
    }
  }

  public static int getIntProperty(String name) {
    String val = getProperty(name);
    try {
      return (val == null) ? 0 : Integer.parseInt(val);
    } catch (NumberFormatException nfe) {
      return 0;
    }
  }
  
  public static void hold() {
    s_hold = true;
  }
  
  public static void write() {
    s_hold = false;
    if (s_dirty && (s_propsfile != null)) {
      try {
        FileOutputStream fos = new FileOutputStream(s_propsfile);
        try {
          s_props.store(fos, null);
        } finally {
          fos.close();
        }
      } catch (Exception e) {
        System.err.println("Exception writing properties");
        e.printStackTrace();
        s_propsfile = null;
      }
    }
    s_dirty = false;
  }
  
  public static void setProperty(String name, String value) {
    String old = s_props.getProperty(name);
    if (old == null) {
      if (value == null) return;
    } else {
      if (old.equals(value)) return;
    }
    
    if (value == null) {
      s_props.remove(name);
    } else {
      s_props.setProperty(name, value);
    }
    s_dirty = true;
    if (!s_hold) write();
  }
  
  public static void setIntProperty(String name, int value) {
    setProperty(name, String.valueOf(value));
  }

}