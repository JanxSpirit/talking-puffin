package com.davebsoft.sctw.ui.web

/**
 * Login managed bean.
 * 
 * @author Dave Briccetti
 */

class Login {
  var user = ""
  var password = ""
  def getUser = user
  def setUser(user: String) = this.user = user
  def getPassword = password
  def setPassword(password: String) = this.password = password
  
  def logIn: String = {
    return "OK"
  }
}