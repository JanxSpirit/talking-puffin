package com.davebsoft.sctw.ui

import _root_.scala.xml.Node
import javax.swing.table.AbstractTableModel
import javax.swing.Icon
import filter.TagUsers

/**
 * A model for users (followed and followers)
 * 
 * @author Dave Briccetti
 */

class UsersModel(friends: List[Node], followers: List[Node]) {
  var users: Array[Node] = _
  var arrows: Array[String] = _
  var screenNameToUserNameMap = Map[String, String]()

  def build(includeFollowing: Boolean, includeFollowers: Boolean) {
    val set = scala.collection.mutable.Set[Node]()
    if (includeFollowing) set ++ friends
    if (includeFollowers) set ++ followers
    val combinedList = set.toList.sort((a,b) => 
      ((a \ "name").text.toLowerCase compareTo (b \ "name").text.toLowerCase) < 0)
    users = combinedList.toArray
    arrows = combinedList.map(user => {
      val friend = friends.contains(user)
      val follower = followers.contains(user)
      if (friend && follower) "↔" else if (friend) "→" else "←"
    }).toArray
    screenNameToUserNameMap = 
        Map(users map {user => ((user \ "screen_name").text, (user \ "name").text)} : _*) 
    
  }
  
}

class UsersTableModel(friends: List[Node], followers: List[Node]) extends AbstractTableModel {
  private val colNames = List(" ", "Image", "Screen Name", "Name", "Tags", "Location", "Description", "Status")
  private val elementNames = List("", "", "screen_name", "name", "", "location", "description", "")
  val usersModel = new UsersModel(friends, followers)
  buildModelData(true, true)

  def buildModelData(includeFollowing: Boolean, includeFollowers: Boolean) {
    usersModel.build(includeFollowing, includeFollowers)
    fireTableDataChanged
  }
  
  def getColumnCount = 8
  def getRowCount = usersModel.users.length

  override def getColumnClass(columnIndex: Int) = {
    columnIndex match {
      case UserColumns.PICTURE => classOf[Icon]
      case _ => classOf[String] 
    }
  }

  val pcell = new PictureCell(this, 1)

  def getValueAt(rowIndex: Int, columnIndex: Int) = {
    val user = usersModel.users(rowIndex)
    def colVal = (user \ elementNames(columnIndex)).text
    columnIndex match {
      case UserColumns.PICTURE => {
        val picUrl = (user \ "profile_image_url").text
        pcell.request(picUrl, rowIndex)
      }
      case UserColumns.ARROWS => usersModel.arrows(rowIndex)
      case UserColumns.SCREEN_NAME => new EmphasizedString(Some(colVal), followers.contains(user))
      case UserColumns.TAGS => TagUsers.tagsForUser((user \ "id").text).mkString(", ")
      case UserColumns.STATUS => (user \ "status" \ "text").text
      case _ => colVal
    }
  }
  override def getColumnName(column: Int) = colNames(column)
  
  def getRowAt(rowIndex: Int) = usersModel.users(rowIndex)
}

