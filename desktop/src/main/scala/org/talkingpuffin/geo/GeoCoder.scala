package org.talkingpuffin.geo

import java.net.URL
import _root_.scala.xml.XML
import com.google.common.collect.MapMaker
import org.talkingpuffin.ui.util.{BackgroundResourceFetcher, ResourceReady}

/**
 * Geocoding.
 */
object GeoCoder {
  private val locationCache: java.util.Map[String, String] = new MapMaker().softValues().makeMap()
  private val num = """(-?\d+\.\d*)"""
  private val latLongRegex = ("""[^-\d]*""" + num + """,\s*""" + num).r

  /**
   * From a (latitude, comma, optional spaces, longitude), produces a (latitude, comma, longitude) String,
   * or None if the pattern does not match.
   */
  def extractLatLong(location: String): Option[String] = location match {
    case latLongRegex(lat, long) => Some(formatLatLongKey(lat, long))
    case _ => None
  }
  
  def formatLatLongKey(lat: String, long: String): String = lat + "," + long 
  
  def formatLatLongKey(location: Tuple2[Double, Double]): String = 
    formatLatLongKey(location._1.toString, location._2.toString) 
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