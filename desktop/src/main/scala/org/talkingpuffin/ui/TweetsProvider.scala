package org.talkingpuffin.ui

import scala.collection.JavaConversions._
import scala.swing.event.Event
import org.talkingpuffin.util.Loggable
import org.talkingpuffin.Session
import org.talkingpuffin.twitter.PageHandler._
import twitter4j.{UserList, Status, ResponseList, Paging}

case class NewTwitterDataEvent(data: List[AnyRef], clear: Boolean) extends Event

abstract class TweetsProvider(session: Session, startingId: Option[Long], 
    providerName: String, longOpListener: LongOpListener) extends
    DataProvider(session, startingId, providerName, longOpListener) with Loggable {

  override def getResponseId(response: Status): Long = response.getId
}

class CommonTweetsProvider(title: String, session: Session, startingId: Option[Long], longOpListener: LongOpListener,
    twFunc: (Paging) => ResponseList[Status], val statusTableModelCust: Option[StatusTableModelCust.Value] = None)
    extends TweetsProvider(session, startingId, title, longOpListener) {

  override def updateFunc(paging: Paging): List[Status] = allPages(twFunc, paging)
}

class FavoritesProvider(session: Session, id: String, startingId: Option[Long],
    longOpListener: LongOpListener)
    extends TweetsProvider(session, startingId, id + " Favorites", longOpListener) {
  override def updateFunc(paging: Paging) = tw.getFavorites.toList
}

class ListStatusesProvider(session: Session, list: UserList,
    startingId: Option[Long], longOpListener: LongOpListener)
    extends TweetsProvider(session, startingId, list.getUser.getName + " " + list.getName + " List", longOpListener) {
  override def updateFunc(paging: Paging) =
    tw.getUserListStatuses(list.getUser.getScreenName, list.getId, paging).toList
}

/* todo class DmsReceivedProvider(session: Session, startingId: Option[Long], longOpListener: LongOpListener)
    extends DataProvider(session, startingId, "DMs Rcvd", longOpListener) {
  override def getResponseId(response: Status): Long = response.getId
  override def updateFunc(args: TwitterArgs) = tw.getDirectMessages(paging(args.since))
}

class DmsSentProvider(session: Session, startingId: Option[Long], longOpListener: LongOpListener)
    extends DataProvider(session, startingId, "DMs Sent", longOpListener) {
  override def getResponseId(response: Status): Long = response.getId
  override def updateFunc(args: TwitterArgs) = tw.getSentDirectMessages(paging(args.since))
}*/
