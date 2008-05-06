package net.devisser.jibe;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.trolltech.qt.core.QRegExp;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.core.QRegExp.PatternSyntax;
import com.trolltech.qt.core.Qt.CaseSensitivity;
import com.trolltech.qt.gui.QBrush;
import com.trolltech.qt.gui.QColor;
import com.trolltech.qt.gui.QFileDialog;
import com.trolltech.qt.gui.QFont;
import com.trolltech.qt.gui.QTextCharFormat;

public class MIMEType {
  
  private String m_mime;
  private String m_descr;
  private Set<String> m_alternatives = new TreeSet<String>();
  
  private Set<String> m_keywords;
  private String m_eolcomment;
  private String m_blockcommentStart;
  private String m_blockcommentEnd;
  private String[] m_quote = new String[3];
  private List<HighlightingRule> m_rules = null;
  
  private Map<String, QRegExp> m_patterns = new TreeMap<String, QRegExp>();
  
  private static final String[] QUOTEDSTRINGCOLOR = {
    "blue", "green", "purple"
  };
  
  private static Map<String, MIMEType> s_mimetypes = 
    new HashMap<String, MIMEType>();
  
  private static XPathFactory s_xpath = XPathFactory.newInstance();
  
  private static Set<MIMEType> s_instances = new HashSet<MIMEType>();
  
  private MIMEType(String mime) {
    assert Util.notEmpty(mime);
    System.err.println("MIMEType: creating " + mime);
    m_mime = mime;
    s_mimetypes.put(mime, this);
    s_instances.add(this);
  }
  
  public String getMIMEType() {
    return m_mime;
  }
  
  public MIMEType setDescription(String descr) {
    System.err.println(this + ": Setting description to " + descr);    
    m_descr = descr;
    return this;
  }
  
  public String getDescription() {
    return m_descr;
  }
  
  public MIMEType addAlternative(String alternative) {
    if (s_mimetypes.containsKey(alternative)) {
      throw new RuntimeException(
          "MIMEType misconfiguration: Cannot add " + 
          alternative + " as alternative to " + getMIMEType());
    }
    m_alternatives.add(alternative);
    s_mimetypes.put(alternative, this);
    return this;
  }
  
  public MIMEType addAlternatives(Collection<String> alternatives) {
    System.err.println(this + ": Adding alternatives " + alternatives);
    for (String alternative : alternatives) {
      addAlternative(alternative);
    }
    return this;
  }
  
  public MIMEType addPattern(String pattern) {
    QRegExp re = new QRegExp(pattern, CaseSensitivity.CaseInsensitive, PatternSyntax.Wildcard);
    m_patterns.put(pattern, re);
    return this;
  }
  
  public MIMEType addPatterns(Collection<String> patterns) {
    System.err.println(this + ": Adding patterns " + patterns);
    for (String pattern : patterns) {
      addPattern(pattern);
    }
    return this;
  }
  
  public Set<String> getPatterns() {
    return Collections.unmodifiableSet(m_patterns.keySet());
  }
  
  public MIMEType setKeywords(Collection<String> keywords) {
    m_keywords = new HashSet<String>();
    for (String kw : keywords) {
      if (kw != null) {
        kw = kw.trim();
        if (Util.notEmpty(kw)) m_keywords.add(kw);
      }
    }
    return this;
  }
  
  public Set<String> getKeywords() {
    return Collections.unmodifiableSet(m_keywords);
  }
  
  public String getBlockCommentStartMarker() {
    return m_blockcommentStart;
  }
  
  public String getBlockCommentEndMarker() {
    return m_blockcommentEnd;
  }
  
  public synchronized List<HighlightingRule> getHighlightingRules() {
    if (m_rules == null) {
      m_rules = new ArrayList<HighlightingRule>();
      for (String keyword : getKeywords()) {
        addHighlightingRule("keyword " + keyword, "\\b" + keyword + "\\b",
          "darkblue", true, false);
      }
      addHighlightingRule("eolcomment", m_eolcomment + "[^\n]*",
          "gray", false, true);
      for (int i = 0; i < 3; i++) {
        addHighlightingRule("quote_" + i, m_quote[i] + ".*" + m_quote[i],
            QUOTEDSTRINGCOLOR[i], false, false);      
      }
      addHighlightingRule("function", "\\b[A-Za-z0-9_]+(?=\\()",
          "darkgreen", false, true);
    }
    return Collections.unmodifiableList(m_rules);
  }
    
  private void addHighlightingRule(String name, String pattern, String color, 
      boolean bold, boolean italic) {
    if (Util.isEmpty(pattern)) return;
    QBrush brush = new QBrush(new QColor(color), Qt.BrushStyle.SolidPattern);
    QTextCharFormat format = new QTextCharFormat();
    format.setForeground(brush);
    if (bold) format.setFontWeight(QFont.Weight.Bold.value());
    format.setFontItalic(italic);
    
    QRegExp re = new QRegExp(pattern);
    m_rules.add(new HighlightingRule(name, re, format));
  }
  
  /*
   * @todo Handle type/*.
   */
  public boolean matchType(String mime) {
    if (getMIMEType().equals(mime)) return true;
    boolean ret = false;
    for (String alternative : m_alternatives) {
      if (alternative.equals(mime)) ret = true;
    }
    return ret;
  }
  
  public boolean matchName(String name) {
    boolean ret = false;
    for (QRegExp re : m_patterns.values()) {
      if (re.exactMatch(name)) ret = true;
    }
    return ret;
  }
  
  public boolean equals(Object o) {
    MIMEType other = (MIMEType) o;
    return getMIMEType().equals(other.getMIMEType());
  }
  
  public int hashCode() {
    return getMIMEType().hashCode();
  }
  
  public String toString() {
    return m_mime;
  }
  
  public static synchronized MIMEType getInstance(String mimetype) {
    MIMEType ret = s_mimetypes.get(mimetype);
    if (ret == null) {
      ret = new MIMEType(mimetype);
    }
    return ret;
  }
  
  public static MIMEType getInstanceForName(String name) {
    MIMEType ret = null;
    for (MIMEType type : s_instances) {
      if (type.matchName(name)) ret = type;
    }
    System.err.println("MIMEType: getInstanceForName(" + name + ") returns " + ((ret != null) ? ret : "null"));
    return ret;
  }
  
  public static QFileDialog.Filter getFilter() {
    StringBuffer sb = new StringBuffer();
    for (MIMEType type : s_instances) {
      if (Util.isEmpty(type.getDescription())) {
        sb.append(type.getMIMEType());
      } else {
        sb.append(type.getDescription());
      }
      sb.append(" (");
      boolean firstpat = true;
      for (String pattern : type.getPatterns()) {
        if (!firstpat) sb.append(" ");
        firstpat = false;
        sb.append(pattern);
      }
      sb.append(");;");
    }
    sb.append("All Files (*)");
    return new QFileDialog.Filter(sb.toString());
  }
  
  public static SortedSet<String> getAllPatterns() {
    SortedSet<String> ret = new TreeSet<String>();
    for (MIMEType type : s_instances) {
      ret.addAll(type.getPatterns());
    }
    return Collections.unmodifiableSortedSet(ret);
  }
  
  private static void readMIMETypeDefinition(String name, InputStream is) {
    try {
      System.err.println("MIMEType: Reading " + name);
      Document doc = Util.parseXML(is);
      XPath xpath = s_xpath.newXPath();
      String primary = null;
      Set<String> alternatives = new HashSet<String>();
      Element e = null;
      NodeList nl = (NodeList) xpath.evaluate("mimetype", 
          doc.getDocumentElement(), XPathConstants.NODESET);
      for (int i = 0; i < nl.getLength(); i++) {
        e = (Element) nl.item(i);
        String mimetype = e.getTextContent();
        if ((primary == null) || "true".equalsIgnoreCase(e.getAttribute("primary"))) {
          primary = mimetype;
        }
        alternatives.add(mimetype);
      }
      if (primary == null) {   
        System.err.println("No mimetypes defined in mimetype definition " + name);
        return;        
      }
      alternatives.remove(primary);
      MIMEType type = getInstance(primary);
      type.addAlternatives(alternatives);
      
      Set<String> patterns = new HashSet<String>();
      nl = (NodeList) xpath.evaluate("pattern", 
          doc.getDocumentElement(), XPathConstants.NODESET);
      for (int i = 0; i < nl.getLength(); i++) {
        e = (Element) nl.item(i);
        patterns.add(e.getTextContent());
      }
      type.addPatterns(patterns);
      
      e = (Element) xpath.evaluate("description", 
          doc.getDocumentElement(), XPathConstants.NODE);
      type.setDescription(e.getTextContent());
      
      e = (Element) xpath.evaluate("keywords", 
          doc.getDocumentElement(), XPathConstants.NODE);
      if (e != null) {
        String keywords = e.getTextContent();
        type.setKeywords(Arrays.asList(keywords.split("\\s")));
      }
      
      e = (Element) xpath.evaluate("quote1", 
          doc.getDocumentElement(), XPathConstants.NODE);
      if (e != null) type.m_quote[0] = e.getTextContent();
      e = (Element) xpath.evaluate("quote2", 
          doc.getDocumentElement(), XPathConstants.NODE);
      if (e != null) type.m_quote[1] = e.getTextContent();
      e = (Element) xpath.evaluate("quote3", 
          doc.getDocumentElement(), XPathConstants.NODE);
      if (e != null) type.m_quote[2] = e.getTextContent();
      e = (Element) xpath.evaluate("comments", 
          doc.getDocumentElement(), XPathConstants.NODE);
      if (e != null) {
        type.m_blockcommentStart = e.getAttribute("blockstart");
        type.m_blockcommentEnd = e.getAttribute("blockend");
        type.m_eolcomment = e.getAttribute("endofline");
      }
    } catch (Exception e) {
      System.err.println("Exception parsing mimetype def " + name);
      e.printStackTrace();
    }
  }
  
  static {
    try {
      InputStream is = MIMEType.class.getResourceAsStream("/net/devisser/jibe/mime/mimetypes");
      Collection<String> mimetypes = new ArrayList<String>();
      BufferedReader rdr = new BufferedReader(new InputStreamReader(is));
      for (String t = rdr.readLine(); t != null; t = rdr.readLine()) {
        mimetypes.add(t);
      }
      for (String mimetype : mimetypes) {
        InputStream is2 = MIMEType.class.getResourceAsStream("/net/devisser/jibe/mime/" + mimetype + ".xml");
        try {
          readMIMETypeDefinition("resource://net/devisser/jibe/mime/" + mimetype + ".xml", is2);
        } finally {
          is2.close();
        }
      }
      
      File mimedir = new File(Config.getInstance().getSystemHome(), "mime");
      if (mimedir.exists() && mimedir.isDirectory()) {
        File[] mimefiles = mimedir.listFiles(new FilenameFilter() {
          public boolean accept(File dir, String name) {
            return name.toLowerCase().endsWith(".xml"); 
          }
        });
        for (File mimefile : mimefiles) {
          readMIMETypeDefinition(mimefile.getName(), new FileInputStream(mimefile));
        }
      }
      mimedir = new File(Config.getInstance().getUserHome(), "mime");
      if (mimedir.exists() && mimedir.isDirectory()) {
        File[] mimefiles = mimedir.listFiles(new FilenameFilter() {
          public boolean accept(File dir, String name) {
            return name.toLowerCase().endsWith(".xml"); 
          }
        });
        for (File mimefile : mimefiles) {
          readMIMETypeDefinition(mimefile.getName(), new FileInputStream(mimefile));
        }
      }
    } catch (Exception e) {
      System.err.println("Exception reading MIME type definitions");
      e.printStackTrace();
    }
  }
  
}
