package org.talkingpuffin.twitter

import scala.xml.Node

class TwitterList(val xml: Node) {
  val name = (xml \ "name").text
  val shortName = name
  val longName = shortName + " from " + (xml \ "user" \ "name").text
  val owner = TwitterUser((xml \ "user")(0))
  val slug = (xml \ "slug").text
  val subscriberCount = (xml \ "subscriber_count").text.toLong
  val memberCount = (xml \ "member_count").text.toLong
}

object TwitterList {
  def apply(xml: Node) = new TwitterList(xml)
}