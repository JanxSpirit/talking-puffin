package org.talkingpuffin.twitter

import scala.collection.JavaConversions._
import twitter4j._
import org.talkingpuffin.util.Loggable

/**
 * Allow fetching all TwitterResponse objects, without having to page.
 */
object PageHandler extends Loggable {

  def friendsStatuses(tw: Twitter, screenName: String)(cursor: Long) = tw.getFriendsStatuses(screenName, cursor)

  def followersStatuses(tw: Twitter, screenName: String)(cursor: Long) = tw.getFollowersStatuses(screenName, cursor)

  def userLists(tw: Twitter, listOwnerScreenName: String)(cursor: Long) = tw.getUserLists(listOwnerScreenName, cursor)

  def userListMembers(tw: Twitter, listOwnerScreenName: String, listId: Int)(cursor: Long) =
    tw.getUserListMembers(listOwnerScreenName, listId, cursor)

  def userListMemberships(tw: Twitter, listMemberScreenName: String)(cursor: Long) =
    tw.getUserListMemberships(listMemberScreenName, cursor)

  def allPages[T <: TwitterResponse](fn: (Long) => PagableResponseList[T], cursor: Long = -1): List[T] = cursor match {
    case 0 => Nil
    case c =>
      var resp = fn(c)
      resp.toList ::: allPages(fn, resp.getNextCursor)
  }

  def allPages(fn: (Paging) => ResponseList[Status], paging: Paging): List[Status] = {
    val resp = fn(paging).toList
    debug("Called Twitter for page " + paging.getPage + ". " + resp.size + " results.")
    resp.size match {
      case 0 => resp
      case n => resp ::: allPages(fn, {paging.setPage(paging.getPage + 1); paging})
    }
  }
}