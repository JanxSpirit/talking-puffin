package com.davebsoft.sctw.ui

import java.awt.{Frame, Cursor}
import SwingInvoke._

/**
 * Whenever a long running operation is performed the UI thread should be released and a spinner should be shown. This object has
 * functions for simplyfying this tasks. Every functions spins of a thread, and calls a callback function when job is finished.
 * @author Alf Kristian Støyle
 */
object LongRunningSpinner {

  private type hasCursorType = { def setCursor(c: Cursor) }
  
  /**
   * Handles several functions after each other on the same thread, and the function returns true if the next function should be
   * executed, false otherwise. The callback funtion is called when the functions have finished
   */
  def run[T <: hasCursorType](frame: T, callback: (Status) => Unit, functions: () => Boolean*) {
    execSwingWorker({
      try {
        invokeLater(frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)))
      
        functions.find(f => !f())
        
        invokeLater(frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)))
        Successful
      }
      catch {
        case e: Exception => new Failure(e)
      }
    }, (status: Status) =>
      if(callback != null) callback(status)
    )
  }
  
  abstract sealed case class Status()
  case object Successful extends Status
  case class Failure(val e:Exception) extends Status
  
}
