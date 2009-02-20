package com.davebsoft.sctw.ui

import _root_.com.davebsoft.sctw.util.PopupListener
import java.awt.event.{ActionListener, ActionEvent}
import javax.swing.{JMenu, JMenuItem, JPopupMenu}
import scala.swing._
import filter.TagsRepos

/**
 * Displays friend and public statuses
 */
class StatusPane(statusTableModel: StatusTableModel) extends GridBagPanel {
  var table: Table = null
  var unmuteButton: Button = null
  
  add(new ScrollPane {
    table = new Table() {
      model = statusTableModel
      val colModel = peer.getColumnModel
      colModel.getColumn(0).setPreferredWidth(100)
      colModel.getColumn(0).setMaxWidth(200)
      colModel.getColumn(1).setPreferredWidth(50)
      colModel.getColumn(1).setMaxWidth(100)
      colModel.getColumn(2).setPreferredWidth(600)
    }
    // TODO convert this to scala.swing way
    table.peer.addMouseListener(new PopupListener(table.peer, getPopupMenu));
    contents = table
  }, new Constraints{
    gridx = 0; gridy = 0; fill = GridBagPanel.Fill.Both; weightx = 1; weighty = 1; 
  })
  
  add(new FlowPanel {
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
    
    val lastSetButton = new Button("Last 200") {
      tooltip = "Loads the last 200 of your “following” tweets"
    }
    lastSetButton.peer.addActionListener(new ActionListener() {
      def actionPerformed(e: ActionEvent) = {
        statusTableModel.loadLastSet
      }
    })
    contents += lastSetButton
    
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
  }, new Constraints{
    gridx = 0; gridy = 1; fill = GridBagPanel.Fill.Horizontal;
  })

  def getPopupMenu: JPopupMenu = {
    val menu = new JPopupMenu()

    val mi = new JMenuItem("Mute")
    mi.addActionListener(new ActionListener() {
      def actionPerformed(e: ActionEvent) = {
        val rows = table.peer.getSelectedRows
        statusTableModel.muteSelectedUsers(rows)
        unmuteButton.enabled = true
      }
    })
    menu.add(mi)

    val tagAl = new ActionListener() {
      def actionPerformed(e: ActionEvent) = {
        val rows = table.peer.getSelectedRows
        statusTableModel.tagSelectedUsers(rows, e.getActionCommand)
      }
    }
    
    val tagMi = new JMenu("Tag Friend With")
    for (tag <- TagsRepos.get) {
      val tagSmi = new JMenuItem(tag)
      tagSmi.addActionListener(tagAl)
      tagMi.add(tagSmi)
    }
    menu.add(tagMi)

    menu
  }

}