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

import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.QAbstractItemView;
import com.trolltech.qt.gui.QBrush;
import com.trolltech.qt.gui.QColor;
import com.trolltech.qt.gui.QGroupBox;
import com.trolltech.qt.gui.QHBoxLayout;
import com.trolltech.qt.gui.QListWidget;
import com.trolltech.qt.gui.QListWidgetItem;
import com.trolltech.qt.gui.QPushButton;
import com.trolltech.qt.gui.QVBoxLayout;
import com.trolltech.qt.gui.QWidget;
import java.util.*;

/**
 * Description of class, first sentence should be a summary (used for index).
 *
 * @version $Revision: 1.27 $ $Date: 2006/10/17 14:02:18 $
 * @author <a href="mailto:customercare@digitalfairway.com">Digital Fairway</a>
 */
public class BufferList extends AbstractBuffer {
  //-------------------------------------------------------------------------
  // ATTRIBUTES
  //-------------------------------------------------------------------------

  private QWidget m_top;
  private QListWidget m_lw;
  
  //-------------------------------------------------------------------------
  // CONSTRUCTORS
  //-------------------------------------------------------------------------
  
  public BufferList() {
    super();
    m_top = new QWidget();
        
    QHBoxLayout box = new QHBoxLayout();
    m_lw = new QListWidget();
    m_top.setFocusProxy(m_lw);
    box.addWidget(m_lw);
    m_top.setLayout(box);
    //QWidget b = new QWidget();
    //box.addWidget(b);
    //QVBoxLayout buttons = new QVBoxLayout();
    //b.setLayout(buttons);
    
    QGroupBox actions = new QGroupBox("Actions");
    box.addWidget(actions);
    QVBoxLayout actionbuttons = new QVBoxLayout();
    QPushButton select = new QPushButton("Switch");
    QPushButton save = new QPushButton("Save");
    QPushButton close = new QPushButton("Close");
    actionbuttons.addWidget(select);
    actionbuttons.addWidget(save);
    actionbuttons.addWidget(close);
    actions.setLayout(actionbuttons);
    
    /*
    QGroupBox cvs = new QGroupBox("CVS");
    buttons.addWidget(cvs);
    QVBoxLayout cvsbuttons = new QVBoxLayout();
    QPushButton refresh = new QPushButton("Refresh");
    QPushButton update = new QPushButton("Update");
    QPushButton commit = new QPushButton("Commit");
    QPushButton diff = new QPushButton("Diff");
    cvsbuttons.addWidget(refresh);
    cvsbuttons.addWidget(update);
    cvsbuttons.addWidget(commit);
    cvsbuttons.addWidget(diff);
    cvs.setLayout(cvsbuttons);
    */
    m_lw.setSelectionMode(QAbstractItemView.SelectionMode.ExtendedSelection);
    
    m_lw.itemDoubleClicked.connect(this, "select()");
    select.pressed.connect(this, "select()");
    close.pressed.connect(this, "close()");
  }
  
  //-------------------------------------------------------------------------
  // PUBLIC METHODS
  //-------------------------------------------------------------------------
  
  public void setBufferManager(BufferManager bufmgr) {
    super.setBufferManager(bufmgr);
    populate();
  }
  
  public QWidget getWidget() {
    return m_top;
  }
  
  public boolean isDirty() {
    return false;
  }
  
  public void save() {
  }
  
  public String getLabel() {
    return "Buffers";
  }
  
  public String getName() {
    return "Buffer List";
  }

  @Override
  public void activate() {
    super.activate();
    populate();
  }
  
  //-------------------------------------------------------------------------
  // SLOTS
  //-------------------------------------------------------------------------
  
  public void select() {
    getBufferManager().select((Buffer) m_lw.currentItem().data(Qt.ItemDataRole.UserRole));
  }
  
  public void close() {
    getBufferManager().closeBuffer((Buffer) m_lw.currentItem().data(Qt.ItemDataRole.UserRole));
    populate();
  }
  
  
  //-------------------------------------------------------------------------
  // PROTECTED METHODS
  //-------------------------------------------------------------------------
  
  //-------------------------------------------------------------------------
  // PRIVATE METHODS
  //-------------------------------------------------------------------------
  
  private void populate(){
    m_lw.clear();
    Set<String> buffers = getBufferManager().getBufferNames();
    for (String buffer : buffers) {
      Buffer b = getBufferManager().getBuffer(buffer);
      QListWidgetItem lwi = new QListWidgetItem(buffer, m_lw);
      lwi.setData(Qt.ItemDataRole.UserRole, b);
      QColor color = null;
      if (b.isDirty()) {
        color = new QColor("red");
      } else {
        switch (b.getVCSStatus()) {
          case MODIFIED: color = new QColor("blue"); break;
          case NEW: color = new QColor("green"); break;
        }
      }
      if (color != null) {
        lwi.setForeground(new QBrush(color));
      }
    }
  }
  
  
  //-------------------------------------------------------------------------
  // STATIC METHODS
  //-------------------------------------------------------------------------
  
}
