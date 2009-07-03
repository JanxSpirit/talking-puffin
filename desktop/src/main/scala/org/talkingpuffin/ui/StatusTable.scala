package org.talkingpuffin.ui

import _root_.scala.swing.GridBagPanel._
import _root_.org.talkingpuffin.util.PopupListener
import _root_.scala.swing.event.ButtonClicked
import _root_.scala.swing.{MenuItem, Action}
import java.awt.{Desktop, Toolkit, Component, Font}

import _root_.scala.{Option}
import java.awt.event.{KeyEvent, ActionEvent, ActionListener, MouseEvent, MouseAdapter}
import java.awt.image.BufferedImage
import java.beans.PropertyChangeEvent
import java.net.{URI, URL}
import java.util.Comparator
import java.util.regex.Pattern
import javax.swing.event._
import javax.swing.table.{DefaultTableCellRenderer, TableRowSorter, TableColumnModel, TableCellRenderer, DefaultTableColumnModel}
import javax.swing.{JTable, KeyStroke, JMenu, JMenuItem, JPopupMenu, JComponent}
import jdesktop.swingx.decorator.{SortOrder, HighlighterFactory}
import org.jdesktop.swingx.event.TableColumnModelExtListener
import org.jdesktop.swingx.JXTable
import org.jdesktop.swingx.table.{TableColumnModelExt, TableColumnExt}
import state.{PrefKeys, GlobalPrefs}
import table.{EmphasizedStringCellRenderer, EmphasizedStringComparator, StatusCellRenderer}
import twitter.{TwitterStatus}
import util.{TableUtil, DesktopUtil}

/**
 * Table of statuses.
 */
class StatusTable(session: Session, tableModel: StatusTableModel, showBigPicture: => Unit)
    extends JXTable(tableModel) {

  setColumnControlVisible(true)
  setHighlighters(HighlighterFactory.createSimpleStriping)
  setRowHeight(Thumbnail.THUMBNAIL_SIZE + 2)
  
  setDefaultRenderer(classOf[String], new DefaultTableCellRenderer)

  var ageCol:   TableColumnExt = _
  var imageCol: TableColumnExt = _
  var nameCol:  TableColumnExt = _
  var toCol:    TableColumnExt = _
  configureColumns

  val ap = new ActionPrep(this)
  buildActions

  addMouseListener(new PopupListener(this, new PopupMenu))
  addMouseListener(new MouseAdapter {
    override def mouseClicked(e: MouseEvent) = if (e.getClickCount == 2) reply
  })
  
  private def viewSelected {
    getSelectedStatuses.foreach(status => {
      var uri = "http://twitter.com/" +
          status.user.screenName + "/statuses/" + status.id
      DesktopUtil.browse(uri)
    })
  }
  
  def reply {
    val statuses = getSelectedStatuses
    val recipients = statuses.map(status => ("@" + status.user.screenName)).mkString(" ")
    createSendMsgDialog(statuses(0), Some(recipients), None).visible = true
  }
  
  def retweet {
    val statuses = getSelectedStatuses
    if (statuses.length == 1 )  {
      val status = statuses(0) 
      val name = "@" + status.user.screenName
      createSendMsgDialog(status, Some(name), Some(status.text)).visible = true
    }
  }
  
  private def createSendMsgDialog(status: TwitterStatus, names: Option[String], retweetMsg: Option[String]) =
    new SendMsgDialog(session, null, names, 
        Some(status.id.toString()), retweetMsg)
  
  private def unfollow = getSelectedScreenNames foreach session.twitterSession.destroyFriendship
  private def block = getSelectedScreenNames foreach session.twitterSession.blockUser
  private def unblock = getSelectedScreenNames foreach session.twitterSession.unblockUser

  def getSelectedScreenNames = getSelectedStatuses.map(s => s.user.screenName)
  def getSelectedStatuses = tableModel.getStatuses(TableUtil.getSelectedModelIndexes(this))

  def getSelectedStatus: Option[TwitterStatus] = {
    val row = getSelectedRow
    if (row == -1) None else Some(tableModel.getStatusAt(convertRowIndexToModel(row)))
  }

  class PopupMenu extends JPopupMenu {
    for (action <- ap.actions.reverse) 
      add(new MenuItem(action).peer)
  }
  
  private def configureColumns {
    val colModel = getColumnModel
    
    ageCol = colModel.getColumn(0).asInstanceOf[TableColumnExt]
    ageCol.setPreferredWidth(60)
    ageCol.setMaxWidth(100)
    ageCol.setCellRenderer(new AgeCellRenderer)
    
    imageCol = colModel.getColumn(1).asInstanceOf[TableColumnExt]
    imageCol.setMaxWidth(Thumbnail.THUMBNAIL_SIZE)
    imageCol.setSortable(false)
    
    nameCol = colModel.getColumn(2).asInstanceOf[TableColumnExt]
    nameCol.setPreferredWidth(100)
    nameCol.setMaxWidth(200)
    nameCol.setCellRenderer(new EmphasizedStringCellRenderer)
    nameCol.setComparator(EmphasizedStringComparator)
    
    toCol = colModel.getColumn(3).asInstanceOf[TableColumnExt]
    toCol.setPreferredWidth(100)
    toCol.setMaxWidth(200)
    toCol.setCellRenderer(new EmphasizedStringCellRenderer)
    toCol.setComparator(EmphasizedStringComparator)
    
    val statusCol = colModel.getColumn(4).asInstanceOf[TableColumnExt]
    statusCol.setPreferredWidth(600)
    statusCol.setCellRenderer(new StatusCellRenderer)
    statusCol.setSortable(false)

    // Set column visibility from preferences
    val colAndKeys = List(ageCol, imageCol, nameCol, toCol) zip
        List(PrefKeys.AGE, PrefKeys.IMAGE, PrefKeys.FROM, PrefKeys.TO)
    
    for ((col, key) <- colAndKeys) {
      col.setVisible(GlobalPrefs.isColumnShowing(key))
      updateTableModelOptions(col)
    }

    colModel.addColumnModelListener(new TableColumnModelExtListener {
      def columnPropertyChange(event: PropertyChangeEvent) = 
        if (event.getPropertyName.equals("visible")) {
          // Save changes into preferences.
          val source = event.getSource
          updateTableModelOptions(source)

          for ((col, key) <- colAndKeys; if (source == col)) GlobalPrefs.showColumn(key, col.isVisible)
        }

      def columnSelectionChanged(e: ListSelectionEvent) = {}
      def columnRemoved(e: TableColumnModelEvent) = {}
      def columnMoved(e: TableColumnModelEvent) = {}
      def columnMarginChanged(e: ChangeEvent) = {}
      def columnAdded(e: TableColumnModelEvent) = {}
    })
  }

  /**
   * Table model needs to know if certain cols are hidden, to put their contents in the status if so.
   */
  private def updateTableModelOptions(source: Object) {
    val op = tableModel.options
    if      (source == ageCol)  op.showAgeColumn  = ageCol .isVisible
    else if (source == nameCol) op.showNameColumn = nameCol.isVisible
    else if (source == toCol)   op.showToColumn   = toCol  .isVisible
  }
  
  def showColumn(index: Int, show: Boolean) {
    getColumnModel.getColumn(index).asInstanceOf[TableColumnExt].setVisible(show)
  }
  
  protected def buildActions = {
    ap add(Action("View in Browser") {viewSelected}, Actions.ks(KeyEvent.VK_V))
    ap add(new OpenPageLinksAction(getSelectedStatus, this, DesktopUtil.browse), Actions.ks(KeyEvent.VK_L))
    ap add(new OpenTwitterUserLinksAction(getSelectedStatus, this, DesktopUtil.browse), Actions.ks(KeyEvent.VK_U))
    ap add(Action("Mute") {tableModel.muteSelectedUsers(TableUtil.getSelectedModelIndexes(this))}, 
      Actions.ks(KeyEvent.VK_M))
    ap add new NextTAction(this)
    ap add new PrevTAction(this)
    ap add(new TagAction(this, tableModel), Actions.ks(KeyEvent.VK_T))
    ap add(Action("Show Larger Image") { showBigPicture }, Actions.ks(KeyEvent.VK_I))
    ap add(Action("Reply…") { reply }, Actions.ks(KeyEvent.VK_R))
    ap add(Action("Retweet") { retweet }, Actions.ks(KeyEvent.VK_E))
    ap add(Action("Unfollow") { unfollow}, KeyStroke.getKeyStroke(KeyEvent.VK_U,
      Toolkit.getDefaultToolkit.getMenuShortcutKeyMask))
    ap add(Action("Block") { block }, KeyStroke.getKeyStroke(KeyEvent.VK_B,
      Toolkit.getDefaultToolkit.getMenuShortcutKeyMask))
    
    ap add(Action("Delete selected tweets") {
      tableModel removeStatuses TableUtil.getSelectedModelIndexes(this) 
    }, KeyStroke.getKeyStroke(KeyEvent.VK_D, Toolkit.getDefaultToolkit.getMenuShortcutKeyMask),
      Actions.ks(KeyEvent.VK_DELETE), Actions.ks(KeyEvent.VK_BACK_SPACE))

    ap add(Action("Delete all tweets from all selected users") {
      tableModel removeStatusesFrom getSelectedScreenNames 
    }, KeyStroke.getKeyStroke(KeyEvent.VK_D, Toolkit.getDefaultToolkit.getMenuShortcutKeyMask | 
      java.awt.event.InputEvent.SHIFT_DOWN_MASK))  
  }
}

