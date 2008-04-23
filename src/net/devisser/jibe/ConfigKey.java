package net.devisser.jibe;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

public class ConfigKey {
  private String m_name;
  private ConfigKeyType m_type;
  private String m_descr;
  private String m_label;
  private Object m_value;
  private Object m_default;
  private ConfigCategory m_category;
  private boolean m_visible = true;
  private Map<String, String> m_attributes = new HashMap<String, String>();
  
  private static Map<String, ConfigKey> s_keys = new TreeMap<String, ConfigKey>();
  
  private ConfigKey(ConfigCategory category, String name, ConfigKeyType type, Object defval) {
    assert Util.notEmpty(name);
    assert type != null;
    m_category = category;
    m_name = name;
    m_label = name;
    m_type = type;
    m_default = m_type.convert(defval);
  }
  
  public String getName() {
    return m_name;
  }
  
  public ConfigKeyType getType() {
    return m_type;
  }
  
  public Object getValue() {
    return (m_value == null) ? getDefaultValue() : m_value;
  }
  
  public String getStringValue() {
    return getType().toString(getValue());
  }
  
  public void setValue(Object value) {
    m_value = getType().convert(value);
  }
  
  public Object getDefaultValue() {
    return m_default;
  }
  
  public String getAttribute(String attr) {
    return m_attributes.get(attr);
  }
  
  private void setAttribute(String attr, String value) {
    m_attributes.put(attr, value);
  }
  
  public ConfigCategory getCategory() {
    return m_category;
  }
  
  public String getDescription() {
    return m_descr;
  }
  
  public String getLabel() {
    return m_label;
  }
  
  public boolean isVisible() {
    return m_visible;
  }
  
  private static final List<String> RESERVED_ATTRS = Arrays.asList(
      "name", "type", "default", "visible", "label");
  
  public static ConfigKey parse(ConfigCategory category, Element e) {
    String name = e.getAttribute("name");
    if (Util.isEmpty(name)) {
      throw new RuntimeException("Config element without name");
    }
    ConfigKeyType type = ConfigKeyType.getInstance(e.getAttribute("type"));
    String defval = e.getAttribute("default");
    ConfigKey ret = new ConfigKey(category, name, type, defval);
    ret.m_descr = e.getTextContent();
    
    String visible = e.getAttribute("visible");
    if (Util.notEmpty(visible)) {
      ret.m_visible = (Boolean) ConfigKeyType.BOOLEAN.toObject(visible);
    }

    String label = e.getAttribute("label");
    if (Util.notEmpty(label)) {
      ret.m_label = label;
    }

    NamedNodeMap attrs = e.getAttributes();
    for (int i = 0; i < attrs.getLength(); i++) {
      String attr = attrs.item(i).getNodeName();
      if (RESERVED_ATTRS.contains(attr)) continue;
      ret.setAttribute(attr, attrs.item(i).getNodeValue());
    }
    s_keys.put(name, ret);
    return ret;
  }
  
  public static synchronized ConfigKey getInstance(String name) {
    ConfigKey ret = s_keys.get(name);
    if (ret == null) {
      throw new RuntimeException("Config key " + name + " not is not defined");
    }
    return ret;
  }
  
  public static void writeConfig() {
    File home = new File(Config.getUserHome());
    if (!home.exists() || !home.isDirectory() || !home.canWrite()) {
      System.err.println("Cannot write config to user home directory " + Config.getUserHome());
      return;
    }
    File propsfile = new File(home, "jibe.properties");
    if (!propsfile.canWrite()) {
      System.err.println("Cannot write config to " + propsfile.getPath());
      return;
    }
    Properties props = new Properties();
    for (ConfigKey key : s_keys.values()) {
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
      System.err.println("Exception writing config to " + propsfile);
      e.printStackTrace();
    }
  }
  
  public static void readConfig() {
    File home = new File(Config.getUserHome());
    if (!home.exists() || !home.isDirectory() || !home.canRead()) {
      System.err.println("Cannot read config from user home directory " + Config.getUserHome());
      return;
    }
    File propsfile = new File(home, "jibe.properties");
    if (!propsfile.exists() || !propsfile.canRead()) {
      System.err.println("Cannot read config from " + propsfile.getPath());
      return;
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
        ConfigKey key = s_keys.get(keyname);
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
}
