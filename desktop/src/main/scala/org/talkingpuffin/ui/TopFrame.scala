package org.talkingpuffin.ui
import _root_.scala.swing.event.{ButtonClicked, WindowClosing}
import filter.{FilterSet, TextFilter, TagUsers}
import java.awt.event.{ActionEvent, ActionListener, KeyEvent}
import java.awt.{Toolkit, Dimension, BorderLayout, Insets}
import java.util.concurrent.{Callable, Executors, Executor}
import java.util.prefs.Preferences
import javax.swing._
import javax.swing.border.{BevelBorder, EmptyBorder}
import org.apache.log4j.Logger
import org.talkingpuffin.mac.QuitHandler
import scala.swing._
import scala.xml._

import TabbedPane._
import state.PreferencesFactory
import twitter._
import ui._
import ui.util.FetchRequest

/**
 * The top-level application Swing frame window. There is one per user session.
 */
class TopFrame(username: String, password: String, twitterSession: AuthenticatedSession) extends Frame{
  val log = Logger getLogger "TopFrame"
  val tagUsers = new TagUsers(username)
  TopFrames.addFrame(this)
  val session = new Session(twitterSession)
  Globals.sessions ::= session
  iconImage = new ImageIcon(getClass.getResource("/TalkingPuffin.png")).getImage
    
  val tabbedPane = new TabbedPane() {
    preferredSize = new Dimension(900, 600)
  }
  session.windows.tabbedPane = tabbedPane

  val mainToolBar = new MainToolBar
  session.progress = mainToolBar
  val streams = new Streams(twitterSession, session, tagUsers, username, password)
  session.windows.streams = streams
  mainToolBar.init(streams)
    
  title = Main.title + " - " + username
  menuBar = new MainMenuBar

  contents = new GridBagPanel {
    val userPic = new Label
    val picFetcher = new PictureFetcher(None, (imageReady: PictureFetcher.ImageReady) => {
      if (imageReady.resource.image.getIconHeight <= Thumbnail.THUMBNAIL_SIZE) {
        userPic.icon = imageReady.resource.image 
      }
    })
    picFetcher.requestItem(new FetchRequest(twitterSession.getUserDetail().profileImageURL, null))
    add(userPic, new Constraints { grid = (0,0); gridheight=2})
    add(session.status, new Constraints {
      grid = (1,0); anchor=GridBagPanel.Anchor.West; fill = GridBagPanel.Fill.Horizontal; weightx = 1;  
      })
    peer.add(mainToolBar, new Constraints {grid = (1,1); anchor=GridBagPanel.Anchor.West}.peer)
    add(tabbedPane, new Constraints {
      grid = (0,2); fill = GridBagPanel.Fill.Both; weightx = 1; weighty = 1; gridwidth=2})
  }

  reactions += {
    case WindowClosing(_) => {
      Globals.sessions = Globals.sessions remove(s => s == session) // TODO is this best way?
      saveState
      TopFrames.removeFrame(this)
    }
  }

  peer.setLocationRelativeTo(null)
  createPeoplePane

  def saveState {
    val highFol = streams.tweetsProvider.getHighestId
    val highMen = streams.mentionsProvider.getHighestId
    log info("Saving last seen IDs for " + username + ". Following: " + highFol + ", mentions: " + highMen)
    val prefs = PreferencesFactory.prefsForUser(username)
    highFol match {
      case Some(i) => prefs.put("highestId", i.toString())
      case _ => // noop
    }
    highMen match {
      case Some(i) => prefs.put("highestMentionId", i.toString())
      case _ => //noop
    }
    tagUsers.save
  }
  
  private def createPeoplePane: Unit = {
    mainToolBar.startOperation
    val pool = Executors.newFixedThreadPool(2)
    val friendsFuture = pool.submit(new Callable[List[TwitterUser]] {
      def call = twitterSession.loadAll(twitterSession.getFriends)
    })
    val followersFuture = pool.submit(new Callable[List[TwitterUser]] {
      def call = twitterSession.loadAll(twitterSession.getFollowers)
    })

    new SwingWorker[Tuple2[List[TwitterUser],List[TwitterUser]], Object] {
      def doInBackground = (friendsFuture.get, followersFuture.get)

      override def done = {
        val (friends, followers) = get 
              
        streams.usersTableModel.friends = friends
        streams.usersTableModel.followers = followers
        streams.usersTableModel.usersChanged
 
        streams setFollowerIds (followers map (u => u.id.toString()))
              
        val paneTitle = "People (" + friends.length + ", " + followers.length + ")"
        val pane = new PeoplePane(session, streams.usersTableModel, friends, followers)
        tabbedPane.pages += new TabbedPane.Page(paneTitle, pane)
        mainToolBar.stopOperation
      }
    }.execute
  }
}
  
