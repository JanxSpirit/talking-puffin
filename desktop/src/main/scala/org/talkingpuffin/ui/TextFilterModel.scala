package org.talkingpuffin.ui

import filter.TextFilters
import javax.swing.table.AbstractTableModel

/**
 * Model for table of TextFilters 
 */
class TextFilterModel(textFilters: TextFilters) extends AbstractTableModel {
  private val colNames = List("Text", "Regex")
  
  override def getColumnName(column: Int) = colNames(column)
  def getColumnCount = 2
  def getRowCount = textFilters.list.length
  override def getColumnClass(columnIndex: Int) = classOf[String] 

  override def getValueAt(rowIndex: Int, columnIndex: Int): Object = {
    val row = textFilters.list(rowIndex)
    columnIndex match {
      case 0 => row.text
      case 1 => if (row.isRegEx) "✓" else ""
    }
  }
}