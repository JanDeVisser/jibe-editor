/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.devisser.jibe;

import com.trolltech.qt.gui.QPaintEvent;

/**
 *
 * @author jan
 */
public interface TextEditListener {
  public void paintEvent(QPaintEvent event, JibeTextEdit te);
}
