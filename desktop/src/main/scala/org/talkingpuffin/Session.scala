package org.talkingpuffin

import org.talkingpuffin.filter.{TagUsers}
import java.util.prefs.Preferences
import swing.Label
import ui._
import twitter4j.Twitter
import util.Loggable

class Session(val serviceName: String, val twitter: Twitter) extends Loggable {
  val desktopPane = new DesktopPane(this)
  val windows = new Windows
  val statusMsgLabel = new Label(" ")
  var progress: LongOpListener = null
  var dataProviders: DataProviders = _
  def userPrefs: Preferences = windows.streams.prefs
  def tagUsers: TagUsers = windows.streams.tagUsers

  /**
   * Records an error message for display to the user.
   */
  def addMessage(msg: String): Unit = {
    // TODO  expand this into a feature that presents all accumulated error messages
    info(msg)
    SwingInvoke.later(statusMsgLabel.text = msg)
  }
  
  def clearMessage(): Unit = {
    SwingInvoke.later(statusMsgLabel.text = " ")
  }
}

