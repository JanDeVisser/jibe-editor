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
import java.net.URL;
import java.util.*;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.trolltech.qt.gui.QColor;
import com.trolltech.qt.gui.QFont;
import java.net.MalformedURLException;
import java.net.URLClassLoader;

public class Config {

  private static Properties s_props = new Properties();
  private static String s_systemhome;
  private static String s_userhome;
  private static File s_propsfile = null;
  private static boolean s_hold = false;
  private static boolean s_dirty = false;
  private static XPathFactory s_xpath = XPathFactory.newInstance();

  static {
    s_systemhome = System.getProperty("jibe.system");
    if (s_systemhome == null) {
      if (System.getenv("JIBE_DIR") != null) {
        s_systemhome = System.getenv("JIBE_DIR");
      } else {
        System.err.println("jibe.system not set");
        System.exit(1);
      }
    }
    s_userhome = System.getProperty("user.home") + "/.jibe";
    if (System.getProperty("jibe.home") != null) {
      s_userhome = System.getProperty("jibe.home");
    } else if (System.getenv("JIBE_HOME") != null) {
      s_userhome = System.getenv("JIBE_HOME");
    }
    
    boolean writable = false;
    try {
      File home = new File(s_userhome);
      if (home.exists() && !home.isDirectory()) {
        System.err.println("Jibe home directory " + s_userhome + " exists, but is not a directory");
      } else if (!home.exists()) {
        System.err.println("Jibe home directory " + s_userhome + " does not exist. Creating it now");
        home.mkdir();
        writable = true;
      } else if (!home.canWrite()) {
        System.err.println("Jibe home directory " + s_userhome + " exists but is not writable");
      } else {
        writable = true;
      }
    } catch (Exception ee) {
      System.err.println("Exception initializing Jibe home directory " + s_userhome);
      ee.printStackTrace();      
    }
    initializeClassLoader();
    
    try {
      readMetadata("Jibe Core", Config.class.getResourceAsStream("/net/devisser/jibe/config.xml"));
    } catch (Exception e) {
      System.err.println("Exception reading config metadata");
      e.printStackTrace();
    }
    ConfigKey.readConfig();
  }
  
  private static void initializeClassLoader() {
    File libdir = new File(getSystemHome(), "lib");
    List<URL> jarURLs = new ArrayList<URL>(getJars(libdir));
    jarURLs.addAll(getJars(new File(getUserHome(), "lib")));
    System.out.println("Jars added to classloader: " + jarURLs);
    URLClassLoader cl = URLClassLoader.newInstance(jarURLs.toArray(new URL[0]), Config.class.getClassLoader());
    Thread.currentThread().setContextClassLoader(cl);
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
  
  public static String getSystemHome() {
    return s_systemhome;
  }
  
  public static String getUserHome() {
    return s_userhome;
  }
  
  public static String getProperty(String name) {
    ConfigKey key = ConfigKey.getInstance(name);
    return key.getStringValue();
  }
  
  public static int getIntProperty(String name) {
    ConfigKey key = ConfigKey.getInstance(name);
    assert key.getType() == ConfigKeyType.INTEGER;
    Integer ret = (Integer) key.getValue();
    return (ret != null) ? ret : 0;
  }
  
  public static boolean getBooleanProperty(String name) {
    ConfigKey key = ConfigKey.getInstance(name);
    assert key.getType() == ConfigKeyType.BOOLEAN;
    Boolean ret = (Boolean) key.getValue();
    return (ret != null) ? ret : false;
  }
  
  public static QColor getColorProperty(String name) {
    ConfigKey key = ConfigKey.getInstance(name);
    assert key.getType() == ConfigKeyType.COLOR;
    return (QColor) key.getValue();
  }

  public static QFont getFontProperty(String name) {
    ConfigKey key = ConfigKey.getInstance(name);
    assert key.getType() == ConfigKeyType.FONT;
    return (QFont) key.getValue();
  }
  
  public static File getFileProperty(String name) {
    ConfigKey key = ConfigKey.getInstance(name);
    assert key.getType() == ConfigKeyType.FILE;
    return (File) key.getValue();
  }
  
  public static void hold() {
    s_hold = true;
  }
  
  public static void write() {
    s_hold = false;
    if (s_dirty) {
      ConfigKey.writeConfig();
    }
    s_dirty = false;
  }
  
  public static void setProperty(String name, Object value) {
    ConfigKey key = ConfigKey.getInstance(name);
    String oldVal = key.getStringValue();
    String newVal = key.getType().toString(value);
    if (oldVal == null) {
      if (newVal == null) return;
    } else {
      if (oldVal.equals(newVal)) return;
    }
    key.setValue(value);
    s_dirty = true;
    if (!s_hold) ConfigKey.writeConfig();
  }
    
  public static void readMetadata(String subsystem, InputStream is) {
    System.err.println("Config: Reading metadata for " + subsystem);
    try {
      Document doc = Util.parseXML(is);
      XPath xpath = s_xpath.newXPath();
      NodeList nl = (NodeList) xpath.evaluate("category", 
          doc.getDocumentElement(), XPathConstants.NODESET);
      for (int i = 0; i < nl.getLength(); i++) {
        Element e = (Element) nl.item(i);
        ConfigCategory.parse(e);
      }
    } catch (Exception ex) {
      System.err.println("Exception reading config metadata for " + subsystem);
      ex.printStackTrace();
    }
  }

}