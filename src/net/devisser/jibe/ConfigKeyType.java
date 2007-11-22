package net.devisser.jibe;

import com.trolltech.qt.gui.QColor;
import com.trolltech.qt.gui.QFont;

public enum ConfigKeyType {
  
  STRING("string", String.class) {
    public Object toObject(String value) {
      return value;
    }
  },
  INTEGER("integer", Integer.class) {
    public Object toObject(String value) {
      return (value == null) ? null : new Integer(value);
    }
  },
  BOOLEAN("boolean", Boolean.class) {
    public Object toObject(String value) {
      return (value == null) ? null : Util.asBoolean(value);
    }
  },
  COLOR("color", QColor.class) {
    public Object toObject(String value) {
      return (value == null) ? null : new QColor(value);
    }
  },
  FONT("font", QFont.class) {
    public Object toObject(String value) {
      return (value == null) ? null : new QFont(value);
    }
  };
  
  private String m_name;
  private Class m_class;
  
  ConfigKeyType(String name, Class cls) {
    m_name = name;
    m_class = cls;
  }
  
  public String getName() {
    return m_name;
  }
  
  public String toString() {
    return getName();
  }
  
  public abstract Object toObject(String value);
  
  public Class getTypeClass() {
    return m_class;
  }
  
  public boolean checkType(Object value) {
    return (value == null) || (value.getClass().isAssignableFrom(m_class));
  }
  
  public String toString(Object value) {
    assert (value == null) || (value.getClass() == getTypeClass());
    return (value != null) ? value.toString() : null;
  }
  
  public Object convert(Object value) {
    if (value instanceof String) {
      return toObject((String) value);
    } else {
      if (!checkType(value)) {
        throw new ClassCastException("Cannot assign object of type " + value.getClass() + " to config key of type " + this);
      } else {
        return value;
      }
    }
  }
  
  public static ConfigKeyType getInstance(String typename) {
    typename = typename.toLowerCase();
    for (ConfigKeyType type : values()) {
      if (typename.equals(type.getName())) return type; 
    }
    return STRING;
  }
}
