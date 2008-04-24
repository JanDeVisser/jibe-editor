/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.devisser.jibe;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 *
 * @author jan
 */
public class PluginManager {
  
  private Jibe m_jibe = null;
  private Map<String, Plugin> m_plugins;
  
  public PluginManager(Jibe jibe) {
    m_jibe = jibe;
    Map<String, File> pluginFiles = new HashMap<String, File>();
    scanPlugins(Config.getSystemHome(), pluginFiles);
    scanPlugins(Config.getUserHome(), pluginFiles);
    initialize(pluginFiles);
  }
  
  public Jibe getJibe() {
    return m_jibe;
  }
  
  private void scanPlugins(String dir, Map<String, File> plugins) {
    File plugindir = new File(dir, "plugins");
    if (!plugindir.exists()) {
      System.err.println("Plug-in directory " + plugindir + " does not exist");
      return;
    } else if (!plugindir.isDirectory()) {
      System.err.println("Plug-in directory " + plugindir + " is not a directory");
      return;
    }
    File[] p = plugindir.listFiles(
      new FileFilter() {
        public boolean accept(File pathname) {
          if (pathname.isDirectory()) {
            File config = new File(pathname, "config.xml");
            return config.exists() && config.isFile();
          } else {
            return pathname.getName().toLowerCase().endsWith(".jar");
          }
        }
      });
    for (File plugin : p) {
      String name = null;
      if (plugin.isDirectory()) {
        name = plugin.getName();
      } else {
        name = plugin.getName().replace(".jar", "");
      }
      plugins.put(name, plugin);
    }
  }
  
  private void initialize(Map<String, File> plugins) {
    for (String name : plugins.keySet()) {
      File codebase = plugins.get(name);
      if (codebase.isDirectory()) {
        initializeDirectory(name, codebase);
      } else {
        initializeJar(name, codebase);
      }
    }
  }
  
  private void initializeDirectory(String name, File codebase) {
    try {
      List<URL> classpath = new ArrayList<URL>();
      classpath.add(codebase.toURI().toURL());

      File[] jars = codebase.listFiles(
        new FilenameFilter() {
          public boolean accept(File dir, String name) {
            return name.toLowerCase().endsWith(".jar");
          }
        });
      for (File j : jars) {
        classpath.add(j.toURI().toURL());
      }
      initialize(name, new FileInputStream(new File(codebase, "config.xml")),
          classpath.toArray(new URL[0]));
    } catch (Exception e) {
      System.err.println("Failed to initialize plugin " + name);
      e.printStackTrace();
    }
  }

  private void initializeJar(String name, File codebase) {
    try {
      ZipFile zf = new ZipFile(codebase);
      ZipEntry entry = zf.getEntry("config.xml");
      initialize(name, zf.getInputStream(entry), 
          new URL[] { codebase.toURI().toURL() });
    } catch (Exception e) {
      System.err.println("Failed to initialize plugin " + name);
      e.printStackTrace();
    }
  }
  
  private void initialize(String name, InputStream is, URL[] classpath) {
    try {
      PluginConfig config = new PluginConfig(name, is);
      URLClassLoader cl = URLClassLoader.newInstance(classpath, 
          Thread.currentThread().getContextClassLoader());
      Class cls = Class.forName(config.getClassName(), true, cl);
      Plugin p = (Plugin) cls.newInstance();
      p.initialize(getJibe(), config);
      m_plugins.put(name, p);
    } catch (Exception e) {
      System.err.println("Failed to initialize plugin " + name);
      e.printStackTrace();
    }
  }

}
