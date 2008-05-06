/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.devisser.jibe;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptableObject;

/**
 *
 * @author jan
 */
public class JibeJSBridge extends ScriptableObject {
  
  private Jibe m_jibe;
  
  public JibeJSBridge(Jibe jibe) {
    m_jibe = jibe;
    Context cx = Context.enter();
    try {
      cx.initStandardObjects(this);
      
    } finally {
      cx.exit();
    }
  }
  
  @Override
  public String getClassName() {
    return "jibe";
  }
  

  

}
