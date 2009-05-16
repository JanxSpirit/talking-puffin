package org.talkingpuffin.ui.util

import javax.swing.JTable

/**
 * Table utilities.
 */
object TableUtil {
  def getSelectedModelIndexes(table: JTable): List[Int] = {
    val tableRows = table.getSelectedRows
    var smi = List[Int]()
    for (i <- 0 to (tableRows.length - 1)) {
      smi ::= table.convertRowIndexToModel(tableRows(i))
    }
    smi
  }

}