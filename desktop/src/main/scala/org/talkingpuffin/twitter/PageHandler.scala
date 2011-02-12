package org.talkingpuffin.twitter

import scala.collection.JavaConversions._
import twitter4j.{Twitter, TwitterResponse, PagableResponseList}

/**
 * Allow fetching all TwitterResponse objects, without having to page.
 */
object PageHandler {

  def friendsStatuses(tw: Twitter, screenName: String)(cursor: Long) = tw.getFriendsStatuses(screenName, cursor)

  def followersStatuses(tw: Twitter, screenName: String)(cursor: Long) = tw.getFollowersStatuses(screenName, cursor)

  def allPages[T <: TwitterResponse](fn: (Long) => PagableResponseList[T], cursor: Long): List[T] = cursor match {
    case 0 => Nil
    case c =>
      var resp = fn(c)
      resp.toList ::: allPages(fn, resp.getNextCursor)
  }
}