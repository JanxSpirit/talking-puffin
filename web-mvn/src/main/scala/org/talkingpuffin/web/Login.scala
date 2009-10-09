package org.talkingpuffin.web

import java.io.Serializable

/**
 * Login managed bean.
 */
class Login extends Serializable {
  var user = ""
  var password = ""
  def getUser = user
  def setUser(user: String) = this.user = user
  def getPassword = password
  def setPassword(password: String) {
    this.password = password
  }
  
  def logIn: String = {
    return "OK"
  }
}