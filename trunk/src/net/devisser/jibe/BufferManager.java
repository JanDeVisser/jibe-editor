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

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.trolltech.qt.gui.QColor;
import com.trolltech.qt.gui.QFileDialog;
import com.trolltech.qt.gui.QTabWidget;


/**
 * Description of class, first sentence should be a summary (used for index).
 *
 * @version $Revision: 1.27 $ $Date: 2006/10/17 14:02:18 $
 */
public class BufferManager extends QTabWidget {
  
  //-------------------------------------------------------------------------
  // ATTRIBUTES
  //-------------------------------------------------------------------------
  
  private Map<String, Buffer> m_buffers = new TreeMap<String, Buffer>();
  private Map<Integer, Buffer> m_buflist = new TreeMap<Integer, Buffer>();
  
  private static BufferManager s_singleton = null; 
  
  //-------------------------------------------------------------------------
  // CONSTRUCTORS
  //-------------------------------------------------------------------------
  
  public BufferManager() {
    if (s_singleton != null) {
      throw new RuntimeException("BufferManager is a singleton");
    }
    s_singleton = this;
    currentChanged.connect(this, "bufferSwitch()");
  }
  
  //-------------------------------------------------------------------------
  // PUBLIC METHODS
  //-------------------------------------------------------------------------
  
  public Buffer getBuffer(String name) {
    return m_buffers.get(name);
  }
  
  public Set<String> getBufferNames() {
    return Collections.unmodifiableSet(m_buffers.keySet());
  }
  
  public Buffer openBuffer(String name) {
    Buffer ret = getBuffer(name);
    if (ret == null) {
      ret = new TextBuffer(name);
      addBuffer(ret);
    } else {
      setCurrentWidget(ret.getWidget());
    }
    return ret;
  }
  
  public void addBuffer(Buffer buffer) {
    buffer.setBufferManager(this);
    int ix = addTab(buffer.getWidget(), buffer.getLabel());
    buffer.setIndex(ix);
    m_buflist.put(ix, buffer);
    m_buffers.put(buffer.getName(), buffer);
    setCurrentWidget(buffer.getWidget());    
  }
  
  public Buffer currentBuffer() {
    return m_buflist.get(currentIndex());
  }
  
  public String getCurrentName() {
    return ((Buffer) currentBuffer()).getName();
  }
  
  public String getCurrentLabel() {
    return ((Buffer) currentBuffer()).getLabel();
  }
  
  public void markDirty(Buffer buffer) {
    int ix = buffer.getIndex();
    tabBar().setTabTextColor(ix, (buffer.isDirty()) ? QColor.red : QColor.black);
  }
  
  public void select(Buffer buffer) {
    tabBar().setCurrentIndex(buffer.getIndex());
  }
  
  //-------------------------------------------------------------------------
  // SLOTS
  //-------------------------------------------------------------------------
  
  public void bufferSwitch() {
    currentBuffer().activate();
    currentWidget().setFocus();
  }
  
  public void showBufferList() {
    singletonBuffer(BufferList.class);
  }
  
  public void singletonBuffer(Class<? extends Buffer> cls) {
    try {
      Buffer b = null;
      for (Buffer buffer : m_buffers.values()) {
        if (cls.isInstance(buffer)) {
          b = buffer;
        }
      }
      if (b == null) {
        b = cls.newInstance();
        addBuffer(b);
      } else {
        setCurrentWidget(b.getWidget());          
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  public void newFile() {
    openBuffer(null);
  }
  
  public void openFile() {
    String fileName = QFileDialog.getOpenFileName(this, 
        tr("Open File"), "",
        MIMEType.getFilter());
    
    if (!fileName.equals("")) {
      openBuffer(fileName);
    }
  }
  
  public void saveBuffer() {
    Buffer cur = currentBuffer();
    if (cur != null) {
      cur.save();
    }    
  }
  
  public void closeBuffer() {
    closeBuffer(currentBuffer());
  }
  
  public void closeBuffer(Buffer buffer) {
    if (buffer != null) {
      removeTab(buffer.getIndex());
      m_buffers.remove(buffer.getName());
      m_buflist.remove(buffer.getIndex());
      buffer.dispose();
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
  
  public static BufferManager getInstance() {
    return s_singleton;
  }
  
}
