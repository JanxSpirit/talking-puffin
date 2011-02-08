package org.talkingpuffin.ui

import org.talkingpuffin.Session
import twitter4j.User

trait ActionProcessor {
  val session: Session
  
  def process[T](items: Seq[T], action: ((T) => Unit), actionName: String, msg: String) =
    items.foreach(item =>
      session.addMessage( 
        try {
          action(item)
          String.format(msg, item.asInstanceOf[Object])
        } catch {
          case e: Throwable => "Error " + actionName + " " + item.toString
        }
      )
    )

  def processUsers(items: Seq[String], action: ((String) => User), actionName: String, msg: String) =
    process(items, (name: String) => {action}: Unit, actionName, msg)
  
}