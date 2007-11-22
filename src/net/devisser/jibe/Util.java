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

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.sun.org.apache.xml.internal.serialize.DOMSerializer;
import com.sun.org.apache.xml.internal.serialize.Method;
import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.SerializerFactory;

public class Util {

  private static DocumentBuilderFactory s_dom_factory =
    DocumentBuilderFactory.newInstance();

  public static boolean notEmpty(String s) {
    return (s != null) && (s.length() > 0);
  }
  
  public static boolean isEmpty(String s) {
    return (s == null) || (s.length() == 0);
  }
  
  public static boolean asBoolean(String value) {
    if (Util.isEmpty(value)) return false;
    value = value.toLowerCase();
    return "true".equals(value) || "yes".equals(value) || "t".equals(value) || "y".equals(value);
  }
  
  public static Document parseXML(InputStream is) throws IOException, SAXException {
    try {
      Document ret = s_dom_factory.newDocumentBuilder().parse(is);
      return ret;
    } catch (ParserConfigurationException ex) {
      throw new RuntimeException("Configuration Exception parsing XML: " + ex);
    }
  }
  
  public static void toXML(Document doc, Writer w, boolean indent) throws IOException {
    OutputFormat of = new OutputFormat();
    of.setIndenting(indent);
    of.setEncoding( "UTF-8" );
    DOMSerializer serializer = SerializerFactory.getSerializerFactory(Method.XML)
      .makeSerializer( w, of )
      .asDOMSerializer();
    serializer.serialize(doc.getDocumentElement());
  }
  
  static {
    s_dom_factory.setNamespaceAware(true);
  }
}