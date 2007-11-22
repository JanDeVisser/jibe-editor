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

import com.trolltech.qt.gui.QColor;
import com.trolltech.qt.gui.QFont;
import com.trolltech.qt.gui.QFontMetrics;
import com.trolltech.qt.gui.QPaintEvent;
import com.trolltech.qt.gui.QPainter;
import com.trolltech.qt.gui.QTextEdit;

/**
 * Description of class, first sentence should be a summary (used for index).
 *
 * @version $Revision: 1.27 $ $Date: 2006/10/17 14:02:18 $
 * @author <a href="mailto:customercare@digitalfairway.com">Digital Fairway</a>
 */
public class JibeTextEdit extends QTextEdit {
  //-------------------------------------------------------------------------
  // ATTRIBUTES
  //-------------------------------------------------------------------------

  private int m_guidepx = 0;
  
  
  //-------------------------------------------------------------------------
  // CONSTRUCTORS
  //-------------------------------------------------------------------------
  
  public JibeTextEdit() {
    super();
    QFont font = new QFont();
    font.setFamily(Config.getProperty("jibe.editor.font"));
    font.setFixedPitch(true);
    font.setPointSize(Config.getIntProperty("jibe.editor.fontsize"));
    
    setLineWrapMode(QTextEdit.LineWrapMode.NoWrap);
    setFont(font);
    
    int guideat = Config.getIntProperty("jibe.editor.guide.column");
    if (guideat > 0) {
      StringBuffer sb = new StringBuffer();
      while (sb.length() < guideat) sb.append('W');
      QFontMetrics fm = new QFontMetrics(font);
      m_guidepx = fm.width(sb.toString()) + 2;
    } else {
      m_guidepx = 0;
    }
    
  }
  
  //-------------------------------------------------------------------------
  // PUBLIC METHODS
  //-------------------------------------------------------------------------
  
  public void paintEvent(QPaintEvent event) {
    super.paintEvent(event);
    if (m_guidepx > 0) {
      QPainter p = new QPainter();
      p.begin(viewport());
      p.setPen(new QColor(Config.getProperty("jibe.editor.guide.color")));
      p.drawLine(m_guidepx, 0, m_guidepx, height());
      p.end();
    }
  }
  
  //-------------------------------------------------------------------------
  // PROTECTED METHODS
  //-------------------------------------------------------------------------
  
  //-------------------------------------------------------------------------
  // PRIVATE METHODS
  //-------------------------------------------------------------------------
  
  //-------------------------------------------------------------------------
  // STATIC METHODS
  //-------------------------------------------------------------------------
  
}
