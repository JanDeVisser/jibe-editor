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


/**
 * Description of class, first sentence should be a summary (used for index).
 *
 * @version $Revision: 1.27 $ $Date: 2006/10/17 14:02:18 $
 */
public class TextBuffer extends AbstractBuffer {
  
  //-------------------------------------------------------------------------
  // ATTRIBUTES
  //-------------------------------------------------------------------------
  private static String __version = "@(#)$Id: src.java,v 1.27 2006/10/17 14:02:18 artur Exp $";

  private static int s_untitled = 1;
  
  private JibeTextEdit m_textedit;
  private QFileInfo m_finfo = null;
  private boolean m_dirty = false;
  private int m_untitled = 0;
  
  //-------------------------------------------------------------------------
  // CONSTRUCTORS
  //-------------------------------------------------------------------------
  
  public TextBuffer(String name) {
    super();
    if (Util.isEmpty(name)) name = null;
    m_textedit = new JibeTextEdit();
    
    new Highlighter(m_textedit.document(), MIMEType.getInstanceForName(name));
    m_textedit.textChanged.connect(this, "textChanged()");
    m_dirty = false;
    if (name != null) {
      load(name);
    }
  }
  
  public TextBuffer() {
    this(null);
  }
  
  //-------------------------------------------------------------------------
  // PUBLIC METHODS
  //-------------------------------------------------------------------------
  
  public String load(String file) {
    if (Util.notEmpty(file)) {
      QFile f = new QFile(file);
      if (f.open(new QFile.OpenMode(QFile.OpenModeFlag.ReadOnly, QFile.OpenModeFlag.Text))) {
        try {
          m_textedit.setPlainText(f.readAll().toString());
          m_finfo = new QFileInfo(f);
          m_dirty = false;
          return m_finfo.fileName();
        } finally {
          f.close();
        }
      }
    }
    return null;
  }
  
  public boolean isNew() {
    return m_finfo == null;
  }
  
  public void save() {
    if (!isNew() && isDirty()) {
      QFile f = new QFile(m_finfo.filePath());
      if (f.open(new QFile.OpenMode(QFile.OpenModeFlag.WriteOnly, QFile.OpenModeFlag.Text))) {
        try {
          f.write(m_textedit.toPlainText().getBytes());
          m_dirty = false;
        } finally {
          f.close();
        }
      }      
    }
  }
  
  public QFileInfo getFileInfo() {
    return m_finfo;
  }
  
  public boolean isDirty() {
    return m_dirty;
  }
  
  public String getName() {
    if (isNew()) {
      if (m_untitled == 0) m_untitled = getNextUntitledNumber();
      return "Untitled " + m_untitled;
    } else {
      return m_finfo.filePath();
    }
  }
  
  public String getLabel() {
    if (isNew()) {
      if (m_untitled == 0) m_untitled = getNextUntitledNumber();
      return "Untitled " + m_untitled;
    } else {
      return m_finfo.fileName();
    }
  }
  
  public QWidget getWidget() {
    return m_textedit;
  }
  
  //-------------------------------------------------------------------------
  // PROTECTED METHODS
  //-------------------------------------------------------------------------
  
  protected void textChanged() {
    boolean d = m_dirty;
    m_dirty = m_textedit.document().isModified();
    if (d != m_dirty) {
      if (getBufferManager() != null) getBufferManager().markDirty(this);
    }
  }
  
  //-------------------------------------------------------------------------
  // PRIVATE METHODS
  //-------------------------------------------------------------------------
  
  private int getNextUntitledNumber() {
    return m_untitled++;
  }
  
  //-------------------------------------------------------------------------
  // STATIC METHODS
  //-------------------------------------------------------------------------
  
}
