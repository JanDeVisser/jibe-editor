/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.devisser.jibe.lineguide;

import com.trolltech.qt.gui.QColor;
import com.trolltech.qt.gui.QFont;
import com.trolltech.qt.gui.QFontMetrics;
import com.trolltech.qt.gui.QPaintEvent;
import com.trolltech.qt.gui.QPainter;
import net.devisser.jibe.Config;
import net.devisser.jibe.Jibe;
import net.devisser.jibe.JibeTextEdit;
import net.devisser.jibe.Plugin;
import net.devisser.jibe.PluginConfig;
import net.devisser.jibe.TextEditListener;

/**
 *
 * @author jan
 */
public class LineGuide implements Plugin, TextEditListener {

  private int m_guidepx = 0;
  private QColor m_color = null;
  
  public void initialize(Jibe jibe, PluginConfig config) {
    QFont font = new QFont();
    font.setFamily(jibe.getConfig().getProperty("jibe.editor.font"));
    font.setFixedPitch(true);
    font.setPointSize(jibe.getConfig().getIntProperty("jibe.editor.fontsize"));
    
    int guideat = jibe.getConfig().getIntProperty("jibe.editor.guide.column");
    if (guideat > 0) {
      StringBuffer sb = new StringBuffer();
      while (sb.length() < guideat) sb.append('W');
      QFontMetrics fm = new QFontMetrics(font);
      m_guidepx = fm.width(sb.toString()) + 2;
    } else {
      m_guidepx = 0;
    }
    m_color = new QColor(Config.getInstance().getProperty("jibe.editor.guide.color"));
    jibe.getBufferManager().registerTextEditListener(this);
  }
  
  public void unload(Jibe jibe) {
    jibe.getBufferManager().removeTextEditListener(this);    
  }

  public void paintEvent(QPaintEvent event, JibeTextEdit te) {
    if (m_guidepx > 0) {
      QPainter p = new QPainter();
      p.begin(te.viewport());
      p.setPen(new QColor(m_color));
      p.drawLine(m_guidepx, 0, m_guidepx, te.height());
      p.end();
    }
  }
}

