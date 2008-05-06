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

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import com.trolltech.qt.gui.QColor;
import com.trolltech.qt.gui.QFont;

public class Config {

  private Jibe m_jibe;
  private Properties m_props = new Properties();
  private String m_systemhome;
  private String m_userhome;
  private File m_propsfile = null;
  private boolean m_hold = false;
  private boolean m_dirty = false;
  private XPathFactory m_xpath = XPathFactory.newInstance();
  private ClassLoader m_classloader = null;
  
  private Map<String, ConfigKey> m_keys = new HashMap<String, ConfigKey>();
  
  private static Config s_singleton = null;

  public Config(Jibe jibe) {
    if (s_singleton != null) {
      throw new RuntimeException("Config is a singleton");
    }
    m_jibe = jibe;
    
    m_systemhome = System.getProperty("jibe.system");
    assert m_systemhome != null;
    m_userhome = System.getProperty("jibe.home");
    assert m_userhome != null;
    
    try {
      readMetadata("Jibe Core", Config.class.getResourceAsStream("/net/devisser/jibe/config.xml"));
    } catch (Exception e) {
      System.err.println("Exception reading config metadata");
      e.printStackTrace();
    }
    readConfig();
    s_singleton = this;
  }
  
  public Jibe getJibe() {
    return m_jibe;
  }
  
  public String getSystemHome() {
    return m_systemhome;
  }
  
  public String getUserHome() {
    return m_userhome;
  }
  
  public String getProperty(String name) {
    ConfigKey key = getKey(name);
    return key.getStringValue();
  }
  
  public int getIntProperty(String name) {
    ConfigKey key = getKey(name);
    assert key.getType() == ConfigKeyType.INTEGER;
    Integer ret = (Integer) key.getValue();
    return (ret != null) ? ret : 0;
  }
  
  public boolean getBooleanProperty(String name) {
    ConfigKey key = getKey(name);
    assert key.getType() == ConfigKeyType.BOOLEAN;
    Boolean ret = (Boolean) key.getValue();
    return (ret != null) ? ret : false;
  }
  
  public QColor getColorProperty(String name) {
    ConfigKey key = getKey(name);
    assert key.getType() == ConfigKeyType.COLOR;
    return (QColor) key.getValue();
  }

  public QFont getFontProperty(String name) {
    ConfigKey key = getKey(name);
    assert key.getType() == ConfigKeyType.FONT;
    return (QFont) key.getValue();
  }
  
  public File getFileProperty(String name) {
    ConfigKey key = getKey(name);
    assert key.getType() == ConfigKeyType.FILE;
    return (File) key.getValue();
  }
  
  public void hold() {
    m_hold = true;
  }
  
  public void write() {
    m_hold = false;
    if (m_dirty) {
      writeConfig();
    }
    m_dirty = false;
  }
  
  public void setProperty(String name, Object value) {
    ConfigKey key = getKey(name);
    String oldVal = key.getStringValue();
    String newVal = key.getType().toString(value);
    if (oldVal == null) {
      if (newVal == null) return;
    } else {
      if (oldVal.equals(newVal)) return;
    }
    key.setValue(value);
    m_dirty = true;
    if (!m_hold) writeConfig();
  }
    
  public void readMetadata(String subsystem, InputStream is) {
    try {
      Document doc = Util.parseXML(is);
      readMetadata(subsystem, doc);
    } catch (Exception ex) {
      System.err.println("Exception reading config metadata for " + subsystem);
      ex.printStackTrace();
    }
  }

  public void readMetadata(String subsystem, Document doc) {
    System.err.println("Config: Reading metadata for " + subsystem);
    try {
      XPath xpath = m_xpath.newXPath();
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

  private static final List<String> RESERVED_ATTRS = Arrays.asList(
      "name", "type", "default", "visible", "label");
  
  public ConfigKey parseKeyDefinition(ConfigCategory category, Element e) {
    String name = e.getAttribute("name");
    if (Util.isEmpty(name)) {
      throw new RuntimeException("Config element without name");
    }
    ConfigKeyType type = ConfigKeyType.getInstance(e.getAttribute("type"));
    String defval = e.getAttribute("default");
    ConfigKey ret = new ConfigKey(category, name, type, defval);
    ret.setDescription(e.getTextContent());
    
    String visible = e.getAttribute("visible");
    if (Util.notEmpty(visible)) {
      ret.isVisible((Boolean) ConfigKeyType.BOOLEAN.toObject(visible));
    }

    String label = e.getAttribute("label");
    if (Util.notEmpty(label)) {
      ret.setLabel(label);
    }

    NamedNodeMap attrs = e.getAttributes();
    for (int i = 0; i < attrs.getLength(); i++) {
      String attr = attrs.item(i).getNodeName();
      if (RESERVED_ATTRS.contains(attr)) continue;
      ret.setAttribute(attr, attrs.item(i).getNodeValue());
    }
    m_keys.put(name, ret);
    return ret;
  }
  
  protected synchronized ConfigKey getKey(String name) {
    ConfigKey ret = m_keys.get(name);
    if (ret == null) {
      throw new RuntimeException("Config key " + name + " not is not defined");
    }
    return ret;
  }
  
  protected void writeConfig() {
    File home = new File(getUserHome());
    if (!home.exists() || !home.isDirectory() || !home.canWrite()) {
      throw new RuntimeException("Cannot write config to user home directory " + getUserHome());
    }
    File propsfile = new File(home, "jibe.properties");
    if (!propsfile.canWrite()) {
      throw new RuntimeException("Cannot write config to " + propsfile.getPath());
    }
    Properties props = new Properties();
    for (ConfigKey key : m_keys.values()) {
      props.put(key.getName(), key.getStringValue());
    }
    
    try {
      FileOutputStream fos = new FileOutputStream(propsfile);
      try {
        props.store(fos, null);
      } finally {
        fos.close();
      }
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Cannot write config to " + propsfile.getPath());      
    }
  }
  
  public void readConfig() {
    File home = new File(getUserHome());
    if (!home.exists() || !home.isDirectory() || !home.canRead()) {
      throw new RuntimeException("Cannot read config from user home directory " + getUserHome());
    }
    File propsfile = new File(home, "jibe.properties");
    if (!propsfile.exists() || !propsfile.canRead()) {
      throw new RuntimeException("Cannot read config from " + propsfile.getPath());
    }
    Properties props = new Properties();
    try {
      FileInputStream fis = new FileInputStream(propsfile);
      try {
        props.load(fis);
      } finally {
        fis.close();
      }
      for (Iterator iter = props.keySet().iterator(); iter.hasNext(); ) {
        String keyname = (String) iter.next();
        ConfigKey key = m_keys.get(keyname);
        if (key == null) {
          key = new ConfigKey(ConfigCategory.getInstance("lost+found"), keyname, ConfigKeyType.STRING, null);
        }
        key.setValue(props.getProperty(keyname));
      }
    } catch (Exception e) {
      System.err.println("Exception reading " + propsfile);
      e.printStackTrace();
    }    
  }
  
  public static Config getInstance() {
    return s_singleton;
  }
}