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

import com.trolltech.qt.gui.QComboBox;
import com.trolltech.qt.gui.QGridLayout;
import com.trolltech.qt.gui.QGroupBox;
import com.trolltech.qt.gui.QHBoxLayout;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QLineEdit;
import com.trolltech.qt.gui.QPushButton;
import com.trolltech.qt.gui.QScrollArea;
import com.trolltech.qt.gui.QWidget;

/**
 * Description of class, first sentence should be a summary (used for index).
 *
 * @version $Revision: 1.27 $ $Date: 2006/10/17 14:02:18 $
 * @author <a href="mailto:customercare@digitalfairway.com">Digital Fairway</a>
 */
public class ConfigBuffer extends AbstractBuffer {
  //-------------------------------------------------------------------------
  // ATTRIBUTES
  //-------------------------------------------------------------------------

  private QWidget m_top;
  private QComboBox m_categories;
  
  //-------------------------------------------------------------------------
  // CONSTRUCTORS
  //-------------------------------------------------------------------------
  
  public ConfigBuffer() {
    super();
    m_top = new QWidget();
        
    QGridLayout box = new QGridLayout();
    
    QLabel label = new QLabel("&Category: ");
    box.addWidget(label, 0, 0);
    m_categories = new QComboBox();
    label.setBuddy(m_categories);
    m_categories.setEditable(false);
    for (ConfigCategory cat : ConfigCategory.getCategories()) {
      if (!cat.isVisible()) continue;
      m_categories.addItem(cat.getLabel(), cat.getName());
    }
    m_top.setFocusProxy(m_categories);
    box.addWidget(m_categories, 0, 1);
    m_top.setLayout(box);
    
    QScrollArea sa = new QScrollArea();
    renderConfigKeys(sa);
    box.addWidget(sa);
    
    //QWidget b = new QWidget();
    //box.addWidget(b);
    //QVBoxLayout buttons = new QVBoxLayout();
    //b.setLayout(buttons);
    
    QGroupBox actions = new QGroupBox("Actions");
    box.addWidget(actions);
    QHBoxLayout actionbuttons = new QHBoxLayout();
    QPushButton apply = new QPushButton("Apply");
    QPushButton reset = new QPushButton("Reset");
    QPushButton close = new QPushButton("Close");
    actionbuttons.addWidget(apply);
    actionbuttons.addWidget(reset);
    actionbuttons.addWidget(close);
    actions.setLayout(actionbuttons);
    
  }
  
  //-------------------------------------------------------------------------
  // PUBLIC METHODS
  //-------------------------------------------------------------------------
  
  public QWidget getWidget() {
    return m_top;
  }
  
  public boolean isDirty() {
    return false;
  }
  
  public void save() {
  }
  
  public String getLabel() {
    return "Config";
  }
  
  public String getName() {
    return "Configuration";
  }
  
  //-------------------------------------------------------------------------
  // PROTECTED METHODS
  //-------------------------------------------------------------------------
  
  //-------------------------------------------------------------------------
  // PRIVATE METHODS
  //-------------------------------------------------------------------------
  
  private void renderConfigKeys(QScrollArea parent) {
    String catname = (String) m_categories.itemData(m_categories.currentIndex());
    ConfigCategory cat = ConfigCategory.getInstance(catname);
    QGridLayout box = new QGridLayout();
    
    int row = 0;
    for (ConfigKey key : cat.getKeys()) {
      QWidget widget = getWidget(key);
      if (widget != null) {
        QLabel label = new QLabel(key.getLabel() + ": ");
        box.addWidget(label, row, 0);
        label.setBuddy(widget);
        box.addWidget(widget, row++, 1);
      }
    }
    parent.setLayout(box);
  }
  
  private QWidget getWidget(ConfigKey key) {
    QLineEdit ret = new QLineEdit();
    ret.setText(key.getStringValue());
    return ret;
  }
  
  //-------------------------------------------------------------------------
  // STATIC METHODS
  //-------------------------------------------------------------------------
  
}
