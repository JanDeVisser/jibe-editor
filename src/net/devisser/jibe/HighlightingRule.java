/**
 * 
 */
package net.devisser.jibe;

import com.trolltech.qt.core.QRegExp;
import com.trolltech.qt.gui.QTextCharFormat;

public class HighlightingRule {
  public QRegExp pattern;
  public QTextCharFormat format;
  public String name;
  
  public HighlightingRule(String name, QRegExp pattern, QTextCharFormat format) {
    this.pattern = pattern;
    this.format = format;
    this.name = name;
  }
  
  public String toString() {
    return name;
  }
}