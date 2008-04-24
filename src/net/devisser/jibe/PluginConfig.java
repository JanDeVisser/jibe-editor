/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.devisser.jibe;

import java.io.InputStream;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author jan
 */
public class PluginConfig {
  
  private String m_classname;
  
  public PluginConfig(String name, InputStream is) {
    try {
      Document doc = Util.parseXML(is);
      Element elem = doc.getDocumentElement();
      m_classname = elem.getAttribute("classname");
      Config.readMetadata(name, is);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    
  }
  
  public String getClassName() {
    return m_classname;
  }
}
