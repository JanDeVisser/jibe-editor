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

import com.trolltech.qt.gui.QTextEdit;
import com.trolltech.qt.gui.QWidget;
import java.util.*;

/**
 * Description of class, first sentence should be a summary (used for index).
 *
 * @version $Revision: 1.27 $ $Date: 2006/10/17 14:02:18 $
 * @author <a href="mailto:customercare@digitalfairway.com">Digital Fairway</a>
 */
public abstract class AbstractBuffer implements Buffer {
  //-------------------------------------------------------------------------
  // ATTRIBUTES
  //-------------------------------------------------------------------------
  private static String __dfcversion = "@(#)$Id: src.java,v 1.27 2006/10/17 14:02:18 artur Exp $";
  
  private int m_index;
  private BufferManager m_bufmgr = null;
    
  //-------------------------------------------------------------------------
  // CONSTRUCTORS
  //-------------------------------------------------------------------------
  
  //-------------------------------------------------------------------------
  // PUBLIC METHODS
  //-------------------------------------------------------------------------
  
  public abstract String getLabel();

  public abstract String getName();

  public abstract boolean isDirty();
  
  public VCSStatus getVCSStatus() {
    return VCSStatus.UNKNOWN;
  }

  public abstract void save();
  
  public abstract QWidget getWidget();
  
  public void dispose() {
    getWidget().dispose();
  }
  
  public void setBufferManager(BufferManager bufmgr) {
    m_bufmgr = bufmgr;
  }

  public BufferManager getBufferManager() {
    return m_bufmgr;
  }
  
  public int getIndex() {
    return m_index;
  }
  
  public void setIndex(int index) {
    m_index = index;
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
