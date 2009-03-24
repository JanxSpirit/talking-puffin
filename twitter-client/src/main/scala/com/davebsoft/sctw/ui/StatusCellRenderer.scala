package com.davebsoft.sctw.ui


import java.awt.{Component, Color}
import javax.swing.table.{DefaultTableCellRenderer, TableCellRenderer}
import javax.swing.{JTextArea, JTable}

/**
 * 
 * @author Dave Briccetti
 */

class StatusCellRenderer extends JTextArea with TableCellRenderer {
  setLineWrap(true)
  setWrapStyleWord(true)
  val renderer = new DefaultTableCellRenderer
  
  override def getTableCellRendererComponent(table: JTable, value: Any, 
      isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component = {

    renderer.getTableCellRendererComponent(table, value, 
            isSelected, hasFocus, row, column) 
    setForeground(renderer.getForeground) 
    setBackground(renderer.getBackground) 
    setBorder(renderer.getBorder) 
    setFont(renderer.getFont) 
    setText(renderer.getText) 
    setSize(table.getColumnModel.getColumn(2).getWidth, 0)
    if (! isSelected) setBackground(if (row % 2 == 0) Color.WHITE else ZebraStriping.VERY_LIGHT_GRAY)
    return this
  }
  
}