package org.talkingpuffin.ui

import _root_.scala.swing.{ComboBox, CheckBox, Label, Button, Frame, PasswordField, Publisher, 
FlowPanel, GridBagPanel, TextField}
import _root_.scala.swing.GridBagPanel.Anchor.West
import ComboBox._
import _root_.scala.xml.Node
import collection.mutable.Subscriber
import java.awt.event.{ActionEvent, ActionListener}
import java.awt.Color
import java.util.prefs.Preferences
import javax.swing.JDialog
import javax.swing.SpringLayout.Constraints
import org.talkingpuffin.twitter.{TwitterSession,AuthenticatedSession,API}
import LongRunningSpinner._
import state.{Accounts, GlobalPrefs, PreferencesFactory}
import swing.event.{SelectionChanged, ButtonClicked, EditDone, Event}
import talkingpuffin.util.Loggable
import util.Cancelable

class LoginDialog(cancelPressed: => Unit, 
    startup: (String, String, String, AuthenticatedSession) => Unit)
    extends Frame with Cancelable with Loggable {
  
  title = "TalkingPuffin - Log In"
  def username = usernameTextField.text
  def password = new String(passwordTextField.password)
  def accountName = accountNameTextField.text
  def apiUrl = apiUrlTextField.text
  private var ok = false

  private val logInButton = new Button("Log In")
  private val logInAllButton = new Button("Log In All")
  private val cancelButton = new Button("Cancel")
  private val saveUserInfoCheckBox = new CheckBox("Remember me (saves password unencrypted)")
  private val infoLabel = new Label(" ")
  private val up = new Accounts()

  private val comboBox = if (up.users.length > 0) new ComboBox(up.users) else null
  private val usernameTextField = new TextField() {columns=20}
  private val passwordTextField = new PasswordField() {columns=20}
  private val accountNameTextField = new TextField(API.defaultService) {
    columns=20; tooltip="A short name you associate with this account"}
  private val apiUrlTextField = new TextField(API.defaultURL) {columns=40}
  
  private val enterReaction: PartialFunction[Event, Unit] = { case EditDone(f) => logInButton.peer.doClick() }
  
  setUpEnterClick(true)
  
  def storeUserInfoIfSet() {
    if(saveUserInfoCheckBox.peer.isSelected) up.save(accountName, apiUrl, username, password)
    else up.remove(apiUrl, username)
    up.save()
  }

  saveUserInfoCheckBox.peer.setSelected(true)
  
  private def setUpEnterClick(enable: Boolean) {
    var fields = List[Publisher](usernameTextField, passwordTextField, accountNameTextField, apiUrlTextField)
    if (comboBox != null) fields ::= comboBox
    fields foreach(t => if (enable) t.reactions += enterReaction else t.reactions -= enterReaction)
  }
  
  contents = new GridBagPanel {
    border = scala.swing.Swing.EmptyBorder(5, 5, 5, 5)
    add(if (comboBox != null) comboBox else usernameTextField, 
      new Constraints {grid = (0, 0); gridwidth=2; anchor=West})
    add(new Label("Account Name"),
                                new Constraints {grid=(0,1); anchor=West})
    add(accountNameTextField,   new Constraints {grid=(1,1); anchor=West})
    add(new Label("User Name"), new Constraints {grid=(0,2); anchor=West})
    add(usernameTextField,      new Constraints {grid=(1,2); anchor=West})
    add(new Label("Password"),  new Constraints {grid=(0,3); anchor=West})
    add(passwordTextField,      new Constraints {grid=(1,3); anchor=West})
    add(new Label("URL"),       new Constraints {grid=(0,4); anchor=West})
    add(apiUrlTextField,        new Constraints {grid=(1,4); anchor=West})
    
    add(new FlowPanel {
      contents += logInButton
      // TODO  if (up.users.length > 1) contents += logInAllButton
      contents += cancelButton
      contents += saveUserInfoCheckBox
    }, new Constraints {grid=(0,5); gridwidth=2})
    
    add(infoLabel, new Constraints {grid=(0,6); gridwidth=2; anchor=West})

    reactions += {
      case ButtonClicked(`logInButton`) => {storeUserInfoIfSet(); handleLogin}
      case ButtonClicked(`logInAllButton`) => {storeUserInfoIfSet(); handleAllLogins}
      case ButtonClicked(`cancelButton`) =>
        LoginDialog.this.visible = false
        cancelPressed
      case SelectionChanged(`comboBox`) => showSelectedUser
    }
    if (comboBox != null) listenTo(comboBox.selection)
    listenTo(logInButton)
    listenTo(logInAllButton)
    listenTo(cancelButton)
  }

  private def showSelectedUser {
    if (comboBox != null) {
      val item = comboBox.selection.item
      val parts = item.split(" ")
      accountNameTextField.text = parts(0)
      usernameTextField.text = parts(1) 
      up.userFor(parts(0), parts(1)) match {
        case Some(u) =>
          passwordTextField.peer.setText(u.password)
          apiUrlTextField.peer.setText(u.apiUrl)
        case _ =>
      }
    }
  }
  
  showSelectedUser

  override def notifyOfCancel = cancelPressed
  
  private def handleLogin {
    enableButtons(false)
    var loggedInUser: AuthenticatedSession = null
    LongRunningSpinner.run(this, null, 
      { 
        () =>
        val sess = TwitterSession(username,password,apiUrl)
        if(sess.verifyCredentials){
            loggedInUser = sess
            true
        }else{
            infoLabel.foreground = Color.RED
            infoLabel.text = "Login failed"
            enableButtons(true)
            false
        }
      }, 
      { 
        () =>
        infoLabel.foreground = Color.BLACK
        infoLabel.text = "Login successful. Initializing…"
        startup(accountName, username, password, loggedInUser)
        visible = false
        true
      }
    )
  }
  
  private def handleAllLogins = { /* TODO */ }
  
  defaultButton = logInButton
  
  private def enableButtons(enable: Boolean) {
    cancelButton.enabled = enable
    logInButton.enabled = enable
    saveUserInfoCheckBox.enabled = enable
    setUpEnterClick(enable)
  }
  
  def display = {
    pack
    peer.setLocationRelativeTo(null)
    visible = true
  }
}
