package net.devisser.jibe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ConfigCategory implements Comparable<ConfigCategory> {
  private String m_name;
  private String m_label = null;
  private boolean m_visible = true;
  private Map<String, ConfigKey> m_keys = new TreeMap<String, ConfigKey>();
  
  private static Map<String, ConfigCategory> s_categories = new TreeMap<String, ConfigCategory>();
  private static XPathFactory s_xpath = XPathFactory.newInstance();
  
  private ConfigCategory(String name) {
    assert Util.notEmpty(name);
    m_name = name;
  }
  
  public String getName() {
    return m_name;
  }
  
  public String getLabel() {
    return Util.isEmpty(m_label) ? getName() : m_label;
  }
  
  private void setLabel(String label) {
    m_label = label;
  }
  
  public boolean isVisible() {
    return m_visible;
  }
  
  public Collection<ConfigKey> getKeys() {
    return Collections.unmodifiableList(new ArrayList<ConfigKey>(m_keys.values()));
  }
  
  public int compareTo(ConfigCategory other) {
    return getName().compareTo(other.getName());
  }
  
  public int hashCode() {
    return getName().hashCode();
  }
  
  public boolean equals(Object obj) {
    return getName().equals(((ConfigCategory) obj).getName());
  }
  
  public synchronized static ConfigCategory parse(Element e) {
    String name = e.getAttribute("name");
    if (Util.isEmpty(name)) {
      throw new RuntimeException("Config category element without name");
    }
    ConfigCategory ret = s_categories.get(name);
    try {
      if (ret == null) {
        ret = new ConfigCategory(name);
        String label = e.getAttribute("label");
        if (Util.notEmpty(label)) ret.setLabel(label);
        s_categories.put(name, ret);
      }
      String visible = e.getAttribute("visible");
      if (Util.notEmpty(visible)) {
        ret.m_visible = Util.asBoolean(visible);
      }
      
      XPath xpath = s_xpath.newXPath();
      NodeList nl = (NodeList) xpath.evaluate("key", e, XPathConstants.NODESET);
      for (int i = 0; i < nl.getLength(); i++) {
        e = (Element) nl.item(i);
        try {
          ConfigKey key = Config.getInstance().parseKeyDefinition(ret, e);
          ret.m_keys.put(key.getName(), key);
        } catch (Exception ex) {
          System.err.println("Exception parsing config key in category " + ret.getName());
          ex.printStackTrace();
        }
      }
    } catch (Exception exx) {
      System.err.println("Exception parsing config category");
      exx.printStackTrace();
    }
    return ret;
  }
  
  public synchronized static ConfigCategory getInstance(String name) {
    return s_categories.get(name);
  }
  
  public synchronized static Set<ConfigCategory> getCategories() {
    return Collections.unmodifiableSet(new TreeSet<ConfigCategory>(s_categories.values()));
  }
}
