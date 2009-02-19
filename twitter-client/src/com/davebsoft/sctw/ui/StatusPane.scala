package com.davebsoft.sctw.ui

import _root_.com.davebsoft.sctw.util.PopupListener
import java.awt.event.{ActionListener, ActionEvent}
import javax.swing.{JMenuItem, JPopupMenu}

import scala.swing._

/**
 * Displays friend and public statuses
 */
class StatusPane(statusTableModel: StatusTableModel) extends BoxPanel(Orientation.Vertical) {
  var table: Table = null
  var unmuteButton: Button = null
  
  contents += new ScrollPane {
    table = new Table() {
      model = statusTableModel
      val colModel = peer.getColumnModel
      colModel.getColumn(0).setPreferredWidth(100)
      colModel.getColumn(1).setPreferredWidth(600)
    }
    // TODO convert this to scala.swing way
    table.peer.addMouseListener(new PopupListener(table.peer, getPopupMenu));
    contents = table
  }
  
  contents += new FlowPanel {
    contents += new Label("Refresh (secs)")
    val comboBox = new ComboBox(List.range(0, 50, 10) ::: List.range(60, 600, 60))
    var defaultRefresh = 120
    comboBox.peer.setSelectedItem(defaultRefresh)
    statusTableModel.setUpdateFrequency(defaultRefresh)
    comboBox.peer.addActionListener(new ActionListener(){
      def actionPerformed(e: ActionEvent) = {  // Couldn’t get to work with reactions
        statusTableModel.setUpdateFrequency(comboBox.selection.item)
      }
    });
    contents += comboBox
    
    val clearButton = new Button("Clear")
    clearButton.peer.addActionListener(new ActionListener() {
      def actionPerformed(e: ActionEvent) = {
        statusTableModel.clear
      }
    })
    contents += clearButton
    
    unmuteButton = new Button("Unmute All")
    unmuteButton.peer.addActionListener(new ActionListener() {
      def actionPerformed(e: ActionEvent) = {
        statusTableModel.unMuteAll
        unmuteButton.enabled = false
      }
    })
    unmuteButton.enabled = false
    contents += unmuteButton
  }

  def getPopupMenu: JPopupMenu = {
    val menu = new JPopupMenu()
    val mi = new JMenuItem("Mute")
    mi.addActionListener(new ActionListener() {
      def actionPerformed(e: ActionEvent) = {
        println("Mute")
        val rows = table.peer.getSelectedRows
        statusTableModel.muteSelectedUsers(rows)
        unmuteButton.enabled = true
      }
    })
    menu.add(mi)
    menu
  }

}