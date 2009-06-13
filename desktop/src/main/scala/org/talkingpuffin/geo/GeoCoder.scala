package org.talkingpuffin.geo

import com.google.common.collect.MapMaker
import java.net.URL
import _root_.scala.xml.XML
import ui.util.{ResourceReady, BackgroundResourceFetcher}

/**
 * Geocoding.
 */
object GeoCoder {
  private val locationCache: java.util.Map[String, String] = new MapMaker().softValues().makeMap()
  private val num = """(-?\d+\.\d*)"""
  private val latLongRegex = ("""[^-\d]*""" + num + """,\s*""" + num).r

  def extractLatLong(location: String): Option[String] = {
    try {
      val latLongRegex(lat, long) = location
      Some(lat + "," + long)
    } catch {
      case e: MatchError => None
    }
  }
  
}

class GeoCoder(processResults: (ResourceReady[String,String]) => Unit) 
    extends BackgroundResourceFetcher[String, String](processResults) {

  protected def getResourceFromSource(latLong: String): String = {
    val url = new URL("http://maps.google.com/maps/geo?ll=" + latLong + "&output=xml&oe=utf-8")
    val placemarks = XML.load(url.openConnection.getInputStream) \ "Response" \ "Placemark"
    placemarks.length match {
      case 0 => latLong
      case _ => (placemarks(0) \ "address").text
    }
  }
  
}