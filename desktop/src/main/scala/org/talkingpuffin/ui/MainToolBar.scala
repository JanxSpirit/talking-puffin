package org.talkingpuffin.ui

import java.awt.Dimension
import java.awt.event.{ActionEvent, ActionListener}

import java.util.concurrent.atomic.AtomicInteger
import javax.swing.JToolBar
import swing.{ProgressBar, Label, ComboBox, Action}
import time.TimeFormatter
import twitter.{LongOpListener, TweetsProvider}

/**
 * The main ToolBar
 */
class MainToolBar extends JToolBar with LongOpListener {
  val progressBar = new ProgressBar {
    val s = new Dimension(50, 0)
    preferredSize = s
    minimumSize = s
  }
  val operationsInProgress = new AtomicInteger
  val remaining = new Label

  setFloatable(false)

  def init(streams: Streams) = {
    add(new Label("Following: ").peer)
    addSourceControls(streams.tweetsProvider, streams.createFollowingView)

    addSeparator

    add(new Label("Mentions: ").peer)
    addSourceControls(streams.mentionsProvider, streams.createRepliesView)

    /*  TODO finish adding rate limiting status. How often and from where to invoke?
    addSeparator
  
    add(new Label("Rem: ") {tooltip = "The number of requests remaining"}.peer)
    add(remaining.peer)
    */

    addSeparator

    add(progressBar.peer)
  }
  
  def startOperation = if (operationsInProgress.incrementAndGet == 1) progressBar.indeterminate = true;
  
  def stopOperation = if (operationsInProgress.decrementAndGet == 0) progressBar.indeterminate = false;
  
  private def addSourceControls(provider: TweetsProvider, createStream: => Unit) {
    val newViewAction = new Action("New View") {
      toolTip = "Creates a new view"
      def apply = createStream
    }
    add(newViewAction.peer)

    val tenThruFiftySecs = List.range(10, 50, 10)
    val oneThruNineMins = List.range(60, 600, 60)
    val tenThruSixtyMins = List.range(10 * 60, 60 * 60 + 1, 10 * 60)
    val assortedTimes = tenThruFiftySecs ::: oneThruNineMins ::: tenThruSixtyMins 

    add(new RefreshCombo(provider, assortedTimes).peer)
  }
  
  case class DisplayTime(val seconds: Int) {
    override def toString = TimeFormatter(seconds).longForm
  }
  
  class RefreshCombo(provider: TweetsProvider, times: List[Int]) extends ComboBox(times map(DisplayTime(_))) {
    peer.setToolTipText("How often to load new items")
    var defaultRefresh = DisplayTime(120)
    peer.setSelectedItem(defaultRefresh)
    provider.setUpdateFrequency(defaultRefresh.seconds)
    peer.addActionListener(new ActionListener(){
      def actionPerformed(e: ActionEvent) = {  // Couldn’t get to work with reactions
        provider.setUpdateFrequency(selection.item.seconds)
      }
    })
    
  }
}

