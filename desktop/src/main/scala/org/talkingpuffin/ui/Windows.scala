package org.talkingpuffin.ui

import _root_.scala.swing.{Frame, TabbedPane, Component}
import javax.swing.{JFrame, SwingUtilities}

/**
 * Window manager.
 */
class Windows {
  var tabbedPane: TabbedPane = _
  var streams: Streams = _
  var peoplePaneCreator: PeoplePaneCreator = _
    
  def undock(comp: Component) {
    new Frame {
      title = streams.componentTitle(comp)
      contents = comp
      menuBar = new MainMenuBar(streams.providers)
    }.visible = true
  }
  
  def dock(comp: Component) = {
    val frame = SwingUtilities.getAncestorOfClass(classOf[JFrame], comp.peer).asInstanceOf[JFrame]
    tabbedPane.pages += new TabbedPane.Page(streams.componentTitle(comp), comp)
    frame.dispose
  }

}
 