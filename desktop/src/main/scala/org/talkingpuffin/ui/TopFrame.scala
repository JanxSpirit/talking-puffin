package org.talkingpuffin.ui

import java.awt.{Point, Dimension}
import java.text.NumberFormat
import javax.swing.{ImageIcon}
import scala.swing.event.{WindowClosing}
import swing.{Reactor, Frame, Label, GridBagPanel}
import org.talkingpuffin.{Main, Globals, Session, Constants}
import org.talkingpuffin.filter.TagUsers
import org.talkingpuffin.twitter.{RateLimitStatusEvent, TwitterUser, AuthenticatedSession}
import org.talkingpuffin.util.{FetchRequest, Loggable}
import org.talkingpuffin.state.{GlobalPrefs, StateSaver}
import util.{AppEvent, eventDistributor}

/**
 * The top-level application Swing frame window. There is one per user session.
 */
class TopFrame(service: String, twitterSession: AuthenticatedSession) extends Frame with Loggable 
    with PeoplePaneCreator with Reactor {
  val tagUsers = new TagUsers(service, twitterSession.user)
  TopFrames.addFrame(this)
  val session = new Session(service, twitterSession)
  Globals.sessions ::= session
  iconImage = new ImageIcon(getClass.getResource("/TalkingPuffin.png")).getImage
    
  session.windows.peoplePaneCreator = this
  private var peoplePane: PeoplePane = _

  val mainToolBar = new MainToolBar
  session.progress = mainToolBar
  setUpUserStatusReactor
  
  val rels = new Relationships()
  
  val prefs = GlobalPrefs.prefsForUser(service, twitterSession.user)
  val providers = new DataProviders(twitterSession, prefs, session.progress)
  menuBar = new MainMenuBar(session, providers, tagUsers)
  val streams = new Streams(service, twitterSession, prefs, providers, session, tagUsers, rels)
  session.windows.streams = streams
  mainToolBar.init(streams)
    
  title = Main.title + " - " + service + " " + twitterSession.user
  reactions += {
    case e: AppEvent if e.session != session => 
    case e: NewViewEvent => 
      streams.createView(e.provider, None, None)
      e.provider.loadContinually()
    case e: NewPeoplePaneEvent => createPeoplePane 
  }
  listenTo(eventDistributor)

  contents = new GridBagPanel {
    val userPic = new Label
    val picFetcher = new PictureFetcher("Frame picture " + hashCode, None)
    picFetcher.requestItem(new FetchRequest(twitterSession.getUserDetail().profileImageURL, null, 
      (imageReady: PictureFetcher.ImageReady) => {
        if (imageReady.resource.image.getIconHeight <= Thumbnail.THUMBNAIL_SIZE) {
          userPic.icon = imageReady.resource.image 
        }
      }))
    add(userPic, new Constraints { grid = (0,0); gridheight=2})
    add(session.statusMsgLabel, new Constraints {
      grid = (1,0); anchor=GridBagPanel.Anchor.West; fill = GridBagPanel.Fill.Horizontal; weightx = 1;  
      })
    peer.add(mainToolBar, new Constraints {grid = (1,1); anchor=GridBagPanel.Anchor.West}.peer)
  }

  reactions += {
    case WindowClosing(_) => close
  }

  peer.setLocationRelativeTo(null)
  listenTo(rels)
  reactions += {
    case ic: IdsChanged => 
      if (peoplePane == null && 
              (rels.followerIds.length + rels.friendIds.length < Constants.MaxPeopleForAutoPaneCreation)) {
        debug("Not too many people, so automatically creating people pane")
        createPeoplePane
      } else {
        debug("Too many people, so not automatically creating people pane")
      }
  }
  rels.getIds(twitterSession, mainToolBar)

  def setFocus = streams.views.last.pane.requestFocusForTable
  
  def close {
    streams.stop
    deafTo(twitterSession.httpPublisher)
    Globals.sessions -= session
    dispose
    StateSaver.save(streams, session.userPrefs, tagUsers)
    TopFrames.removeFrame(this)
  }

  type Users = List[TwitterUser]
  
  def createPeoplePane(longTitle: String, otherRels: Option[Relationships], users: Option[Users], 
        updatePeople: Option[() => Unit], selectPane: Boolean, location: Option[Point]): PeoplePane = {
    def getRels = if (otherRels.isDefined) otherRels.get else rels
    val model = 
      if (users.isDefined || otherRels.isDefined) 
        new UsersTableModel(users, tagUsers, getRels) 
      else 
        streams.usersTableModel
    val customRels = if (users.isDefined) {
      new Relationships {
        friends = rels.friends intersect users.get
        friendIds = friends map(_.id)
        followers = rels.followers intersect users.get
        followerIds = followers map(_.id)
      }
    } else getRels
    val peoplePane = new PeoplePane(session, model, customRels, updatePeople)
    new Frame {
      title = longTitle
      menuBar = new MainMenuBar(session, providers, tagUsers)
      contents = peoplePane
      visible = true
      peer.setLocationRelativeTo(null)
    }
    peoplePane
  }

  private def updatePeople = rels.getUsers(twitterSession, twitterSession.user, mainToolBar)
          
  private def createPeoplePane: Unit = {
    updatePeople
    peoplePane = createPeoplePane("People You Follow and People Who Follow You", None, None, 
        Some(updatePeople _), false, None)
  }
  
  private def setUpUserStatusReactor {
    reactions += {
      case e: RateLimitStatusEvent => SwingInvoke.later {
        mainToolBar.remaining.text = NumberFormat.getIntegerInstance.format(e.status.remainingHits)
      }
    }
    listenTo(twitterSession.httpPublisher)
  }

  private def getProviders = streams.providers
}

  
