/*************************************************************************
 **
 ** Copyright (C) 2007      Jan de Visser. All rights reserved.
 ** Copyright (C) 1992-2007 Trolltech ASA. All rights reserved.
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

import java.util.*;
import com.trolltech.qt.core.*;
import com.trolltech.qt.gui.*;

class Highlighter extends QSyntaxHighlighter {
  
  private List<HighlightingRule> m_highlightingRules;
  
  private QRegExp commentStartExpression = null;
  private QRegExp commentEndExpression = null;
  private QTextCharFormat commentFormat = new QTextCharFormat();
  
  public Highlighter(QTextDocument parent, MIMEType mimetype) {    
    super(parent);
    if (mimetype != null) {
      m_highlightingRules = mimetype.getHighlightingRules();
      commentStartExpression = new QRegExp(mimetype.getBlockCommentStartMarker());
      commentEndExpression = new QRegExp(mimetype.getBlockCommentEndMarker());
      QBrush brush = new QBrush(new QColor("gray"), Qt.BrushStyle.SolidPattern);
      commentFormat.setForeground(brush);
      commentFormat.setFontItalic(true);
    } else {
      m_highlightingRules = new ArrayList<HighlightingRule>();
    }
  }
  
  public void highlightBlock(String text) {
    for(HighlightingRule rule : m_highlightingRules) {
      QRegExp expression = rule.pattern;
      int index = expression.indexIn(text);
      //System.err.println("Highlighter: Applying rule " + rule);
      while (index >= 0) {
        int length = expression.matchedLength();
        if (length == 0) {
          System.err.println("Huh? matchedLength() was 0 on re " + expression.pattern());
          break;
        }
        setFormat(index, length, rule.format);
        index = expression.indexIn(text, index + length);
      }
    }
    if (commentStartExpression != null) {
      setCurrentBlockState(0);
      
      int startIndex = 0;
      if (previousBlockState() != 1)
        startIndex = commentStartExpression.indexIn(text);
      
      while (startIndex >= 0) {
        int endIndex = commentEndExpression.indexIn(text, startIndex);
        int commentLength;
        if (endIndex == -1) {
          setCurrentBlockState(1);
          commentLength = text.length() - startIndex;
        } else {
          commentLength = endIndex - startIndex + commentEndExpression.matchedLength();
        }
        setFormat(startIndex, commentLength, commentFormat);
        startIndex = commentStartExpression.indexIn(text, startIndex + commentLength);
      }
    }
  }
}