package org.talkingpuffin.ui

import org.talkingpuffin.mac.QuitHandler
import java.awt.Frame
import org.talkingpuffin.util.Loggable

/**
 * Keeps track of top-level frames.
 */
object TopFrames extends Loggable {
  private var frames = List[TopFrame]()

  QuitHandler register TopFrames.closeAll

  def closeCurrentWindow(){
    frames.find(_.peer.isFocused) match{
      case Some(frame) => frame.close
      case _ => closeOtherFrame()
    }
  }

  def closeOtherFrame(){
    val frames = Frame.getFrames()
    frames.find(_.isFocused) match {
      case Some(frame) => frame.dispose
      case _ => // noop
    }
  }
  def addFrame(f: TopFrame){
    frames = f :: frames
    debug("Frame added. Number of frames is " + frames.size + ".")
  }

  def removeFrame(f: TopFrame){
    frames -= f
    debug ("Frame removed. Number of frames is " + frames.size + ".")
    exitIfNoFrames
  }

  def exitIfNoFrames =
    if(frames == Nil){
      debug("No more frames. Exiting.")
      // it's kinda ugly to put the exit logic here, but not sure where
      // else to put it.'
      System.exit(0)
    }
  
  def numFrames = frames.size

  def closeAll: Unit = closeAll(frames)

  def closeAll(frames: List[TopFrame])= frames.foreach(_.close)
}
  
 