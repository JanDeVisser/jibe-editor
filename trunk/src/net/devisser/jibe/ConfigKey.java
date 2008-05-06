package net.devisser.jibe;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import org.w3c.dom.Element;


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
  
  public ConfigKey(ConfigCategory category, String name, ConfigKeyType type, Object defval) {
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
  
  public void setAttribute(String attr, String value) {
    m_attributes.put(attr, value);
  }
  
  public ConfigCategory getCategory() {
    return m_category;
  }
  
  public String getDescription() {
    return m_descr;
  }
  
  public void setDescription(String description) {
    m_descr = description;
  }
  
  public String getLabel() {
    return m_label;
  }
  
  public void setLabel(String label) {
    m_label = label;
  }
  
  public boolean isVisible() {
    return m_visible;
  }
  
  public void isVisible(boolean visible) {
    m_visible = visible;
  }
  
}
