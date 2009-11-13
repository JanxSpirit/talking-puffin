package org.talkingpuffin.ui

import javax.swing.KeyStroke
import java.awt.event.KeyEvent
import java.awt.Toolkit
import scala.swing.{MenuItem, MenuBar, Menu, CheckMenuItem, Action}
import swing.event.{Event, ButtonClicked}
import org.talkingpuffin.state.{GlobalPrefs, PrefKeys}
import org.talkingpuffin.filter.TagUsers
import org.talkingpuffin.util.{TwitterListUtils, Loggable}
import org.talkingpuffin.{Session, Main}
import util.{eventDistributor, AppEvent}

/**
 * Main menu bar
 */
class MainMenuBar(session: Session, dataProviders: DataProviders, 
                  tagUsers: TagUsers) extends MenuBar with Loggable {
  val tsess = session.twitterSession
  val prefs = GlobalPrefs.prefs

  val shortcutKeyMask = Toolkit.getDefaultToolkit.getMenuShortcutKeyMask
  
  contents += new Menu("File") {
    contents += new MenuItem(new Action("New Session...") {
      accelerator = Some(KeyStroke.getKeyStroke(KeyEvent.VK_N, shortcutKeyMask))
      def apply = Main.launchSession 
    })
    contents += new MenuItem(new Action("Close Window") {
      accelerator = Some(KeyStroke.getKeyStroke(KeyEvent.VK_W, shortcutKeyMask))
      def apply = TopFrames.closeCurrentWindow()
    })
  }
  
  contents += new Menu("Views") {
    contents += new Menu("New") {
      def newItem(name: String, event: Event) = new MenuItem(new Action(name) {
        toolTip = "Creates a new " + name + " view"
        def apply = eventDistributor.publish(event)
      })
      dataProviders.providers.foreach(provider => {
        contents += newItem(provider.providerName, NewViewEvent(session, provider, None))
      })
      contents += newItem("People", NewPeoplePaneEvent(session))
    }
    contents += new MenuItem(Action("Tile full height") {eventDistributor.publish(TileViewsEvent(session, 1D))})
    contents += new MenuItem(Action("Tile half height") {eventDistributor.publish(TileViewsEvent(session, .5D))})
  }
  
  contents += new Menu("Lists") {
    contents += new Menu("Export tag to list") {
      tagUsers.getTags.foreach(tag => {
        contents += new MenuItem(new Action(tag) {
          def apply = {
            SwingInvoke.execSwingWorker({TwitterListUtils.export(tsess, tag, tagUsers.usersForTag(tag))
            }, (_: Unit) => {debug("Tag exported to list")})
          }
        })
      })
    }
    contents += new MenuItem(new Action("Display your lists") {
      def apply = {
        TwitterListsDisplayer.viewLists(session, 
          List(session.twitterSession.user), MenuPos(MainMenuBar.this.peer, 0, 0))
      }
    })
    contents += new MenuItem(new Action("Display lists you are in") {
      def apply = {
        TwitterListsDisplayer.viewListsContaining(session, 
          List(session.twitterSession.user), MenuPos(MainMenuBar.this.peer, 0, 0))
      }
    })
    contents += new MenuItem(new Action("Display statuses from your lists") {
      def apply = {
        TwitterListsDisplayer.viewListsStatuses(session, 
          List(session.twitterSession.user), MenuPos(MainMenuBar.this.peer, 0, 0))
      }
    })
  }
  
  contents += new Menu("Options") {
    object ItemFactory {
      def apply(title: String, tooltip: String, prefKey: String, onByDefault: Boolean): MenuItem = {
        val item = new CheckMenuItem(title) {
          this.tooltip = tooltip; 
          selected = prefs.getBoolean(prefKey, onByDefault)
        }
        listenTo(item)
        reactions += {
          case r: ButtonClicked => if (r.source == item) GlobalPrefs.put(prefKey, item.selected)
          case _ =>
        }
        item
      }
    }
    contents += ItemFactory("Use animations", "Enables simple, useful animations", 
      PrefKeys.USE_ANIMATIONS, false)
    contents += ItemFactory("Use real names", "Display real names", PrefKeys.USE_REAL_NAMES, true)
    contents += ItemFactory("Notify tweets", "Notify when tweets arrive", PrefKeys.NOTIFY_TWEETS, true)
    contents += ItemFactory("Look up locations", "Enables lookup of locations from latitude and longitude",
      PrefKeys.LOOK_UP_LOCATIONS, false)
    contents += ItemFactory("Expand URLs", "Enables fetching original URL from shortened form", 
      PrefKeys.EXPAND_URLS, false)
    contents += ItemFactory("Show tweet date as age", "Shows the tweet date as an age", 
      PrefKeys.SHOW_TWEET_DATE_AS_AGE, false)
    contents += ItemFactory("New after Clear", "Performs a “Load New” after “Clear” and “Clear All”", 
      PrefKeys.NEW_AFTER_CLEAR, false)
  }
  
}

case class NewViewEvent(override val session: Session, val provider: DataProvider, include: Option[String]) 
    extends AppEvent(session)
case class NewFollowingViewEvent(override val session: Session, include: Option[String]) extends AppEvent(session)
case class NewPeoplePaneEvent(override val session: Session) extends AppEvent(session)
case class TileViewsEvent(override val session: Session, val heightFactor: Double) extends AppEvent(session)
