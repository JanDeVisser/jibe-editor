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

import com.trolltech.qt.core.*;
import com.trolltech.qt.gui.*;
import java.util.List;

public class Jibe extends QMainWindow {
  
  private Config m_config;
  private BufferManager m_bufmgr;
  private PluginManager m_pluginmgr;
  
  public static void main(List<String> args) {
    QApplication.initialize(args.toArray(new String[0]));
    Jibe jibe = new Jibe();
    jibe.show();
    QApplication.exec();
  }
  
  public Jibe() {
    m_config = new Config(this);
    m_bufmgr = new BufferManager(this);
    setupFileMenu();
    setupEditMenu();
    setupHelpMenu();
    new JibeJSBridge(this);
    m_pluginmgr = new PluginManager(this);
    
    setCentralWidget(m_bufmgr);
    resize(m_config.getIntProperty("jibe.mainwindow.width"),
        m_config.getIntProperty("jibe.mainwindow.height"));
    setWindowTitle(tr("Jibe - Jibe Is a Better Editor"));
    setWindowIcon(new QIcon(
        "classpath:/com/trolltech/images/qt-logo.png"));
    m_bufmgr.currentChanged.connect(this, "bufferSwitch()");
  }
  
  public void about() {
    QMessageBox.about(this, tr("About Syntax Highlighter"),
        tr("<p>The <b>Syntax Highlighter</b> example shows how "
        + "to perform simple syntax highlighting by subclassing "
        + "the QSyntaxHighlighter class and describing "
        + "highlighting rules using regular expressions.</p>"));
  }
  
  public void aboutQt() {
    QApplication.aboutQt();
  }
  
  public void config() {
    m_bufmgr.singletonBuffer(ConfigBuffer.class);
  }
  
  public BufferManager getBufferManager() {
    return m_bufmgr;
  }
  
  public PluginManager getPluginManager() {
    return m_pluginmgr;
  }
  
  public Config getConfig() {
    return m_config;
  }
  
  protected void resizeEvent(QResizeEvent ev) {
    QSize s = ev.size();
    m_config.hold();
    m_config.setProperty("jibe.mainwindow.height", s.height());
    m_config.setProperty("jibe.mainwindow.width", s.width());
    m_config.write();
  }
  
  public void bufferSwitch() {
    setWindowTitle(tr("Jibe - ") + m_bufmgr.getCurrentName());    
  }
  
  private void setupFileMenu() {
    QMenu fileMenu = new QMenu(tr("&File"), this);
    menuBar().addMenu(fileMenu);
    
    QAction newAct = new QAction(tr("&New"), this);
    newAct.setShortcut(QKeySequence.StandardKey.New);
    newAct.triggered.connect(m_bufmgr, "newFile()");
    fileMenu.addAction(newAct);
    
    QAction openAct = new QAction(tr("&Open..."), this);
    openAct.setShortcut(QKeySequence.StandardKey.Open);
    openAct.triggered.connect(m_bufmgr, "openFile()");
    fileMenu.addAction(openAct);
    
    QAction saveAct = new QAction(tr("&Save"), this);
    saveAct.setShortcut(QKeySequence.StandardKey.Save);
    saveAct.triggered.connect(m_bufmgr, "saveBuffer()");
    fileMenu.addAction(saveAct);
    
    fileMenu.addSeparator();
    QAction listAct = new QAction(tr("&Buffers"), this);
    listAct.setShortcut(new QKeySequence(
        Qt.KeyboardModifier.ControlModifier.value() + 
        Qt.KeyboardModifier.AltModifier.value() + 
        Qt.Key.Key_Space.value()));
    listAct.triggered.connect(m_bufmgr, "showBufferList()");
    fileMenu.addAction(listAct);
    
    
    fileMenu.addSeparator();
    
    QAction closeAct = new QAction(tr("&Close"), this);
    closeAct.triggered.connect(m_bufmgr, "closeBuffer()");
    closeAct.setShortcut(QKeySequence.StandardKey.Close);
    fileMenu.addAction(closeAct);
    
    QAction quitAct = new QAction(tr("E&xit"), this);
    quitAct.setShortcut(new QKeySequence(Qt.KeyboardModifier.ControlModifier.value() + Qt.Key.Key_Q.value()));
    quitAct.triggered.connect(this, "close()");
    fileMenu.addAction(quitAct);
  }
  
  private void setupEditMenu() {
    QMenu editMenu = new QMenu(tr("&Edit"), this);
    menuBar().addMenu(editMenu);
    
    editMenu.addSeparator();
    QAction configAct = new QAction(tr("&Preferences"), this);
    configAct.triggered.connect(this, "config()");
    editMenu.addAction(configAct);
  }
    
  private void setupHelpMenu() {
    QMenu helpMenu = new QMenu(tr("&Help"), this);
    menuBar().addMenu(helpMenu);
    
    QAction aboutAct = new QAction(tr("&About"), this);
    aboutAct.triggered.connect(this, "about()");
    helpMenu.addAction(aboutAct);
    
    QAction aboutQtAct = new QAction(tr("About &Qt"), this);
    aboutQtAct.triggered.connect(this, "aboutQt()");
    helpMenu.addAction(aboutQtAct);
  }

}
