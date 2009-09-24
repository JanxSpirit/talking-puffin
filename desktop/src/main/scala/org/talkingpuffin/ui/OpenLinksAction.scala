package org.talkingpuffin.ui

import _root_.scala.swing.{Action,MenuItem}
import java.awt.event.KeyEvent
import javax.swing.{JTable, KeyStroke, JPopupMenu}
import org.talkingpuffin.twitter.{TwitterStatus}

/**
 * Shows a menu of links, or launches the browser on the link if there is only one. 
 */
abstract class OpenLinksAction(getSelectedStatus: => Option[TwitterStatus], table: JTable,
    browse: (String) => Unit, title: String) extends Action(title) {
  
  def apply {
    def addMenuItem(menu: JPopupMenu, title: String, accelIndex: Int, action: => Unit) = {
      val a1 = Action(title) {action}
      a1.accelerator = Some(KeyStroke.getKeyStroke(KeyEvent.VK_1 + accelIndex, 0)) 
      menu.add(new MenuItem(a1).peer)
    }
    
    def browseUrls(urls: List[String]) {
      for (url <- urls) 
        browse(url)
    }
    
    def stripHttpProtocolString(url: String): String = {
      val prefix = "http://"
      if (url startsWith prefix) url substring prefix.length else url
    }

    getSelectedStatus match {
      case Some(status) =>
        val urls = LinkExtractor.getLinks(status, users, pages)
  
        if (urls.length == 1) {
          browse(urls(0)._2)
        } else if (urls.length > 1) {
          val menu = new JPopupMenu
          var index = 0
    
          for (url <- urls) {
            addMenuItem(menu, stripHttpProtocolString(url._1), index, browse(url._2))
            index += 1
          }
          
          if (urls.length > 1) {
            val a1 = Action(if (urls.length == 2) "Both" else "All") {browseUrls(urls.map(url => url._2))}
            a1.accelerator = Some(KeyStroke.getKeyStroke(KeyEvent.VK_A, 0)) 
            menu.add(new MenuItem(a1).peer)
          }
          val menuLoc = table.getCellRect(table.getSelectedRow, 0, true).getLocation
          menu.show(table, menuLoc.getX().asInstanceOf[Int], menuLoc.getY().asInstanceOf[Int])
        }
      case None =>
    }
  }
  
  def users: Boolean
  def pages: Boolean
}

class OpenPageLinksAction(getSelectedStatus: => Option[TwitterStatus], table: JTable,
    browse: (String) => Unit) extends OpenLinksAction(getSelectedStatus, table, browse,
    "Open Links…") {
  def users = false
  def pages = true
}

class OpenTwitterUserLinksAction(getSelectedStatus: => Option[TwitterStatus], table: JTable,
    browse: (String) => Unit) extends OpenLinksAction(getSelectedStatus, table, browse,  
    "Open User Links…") {
  def users = true
  def pages = false
}
