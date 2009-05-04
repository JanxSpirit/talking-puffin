package org.talkingpuffin.twitter

import java.io.{InputStreamReader, BufferedInputStream, BufferedReader, OutputStreamWriter, InputStream}
import java.net.URL
import org.apache.commons.codec.binary.Base64
import org.apache.commons.httpclient.auth.AuthScope
import org.apache.commons.httpclient.cookie.CookiePolicy
import org.apache.commons.httpclient.methods.{PostMethod, GetMethod}
import org.apache.commons.httpclient.{UsernamePasswordCredentials, HttpMethodBase, HttpMethod, HttpClient}
import org.apache.log4j.Logger

object StreamUtil {
  def streamToString(stream: InputStream): String = {
    val buf = new StringBuilder
    val br = new BufferedReader(new InputStreamReader(stream))
    var line: String = null
    var eof = false
    while (! eof) {
      line = br.readLine()
      if (line == null) eof = true else 
      buf.append(line).append("\n")
    }
    buf.toString
  }  
}

/**
 * Generalises Http handling.
 * @author Alf Kristian Støyle  
 * @author Dave Briccetti  
 */
protected trait HttpHandler {
  private val log = Logger.getLogger(getClass)
  val httpClient = new HttpClient()
  var username: String = _
  var password: String = _

  /**
   * Fetches the requested resource and returns a tuple of the status code
   * and the response body.
   */
  def doGet(urlString: String) = {
    log.info("Fetching " + urlString)
    val url = new URL(urlString)
    val conn = url.openConnection
    conn.addRequestProperty("Authorization", 
      "Basic " + new String(new Base64().encode((username + ":" + password).getBytes)))
    
    val headers = conn.getHeaderFields
    //TODO test or get this working on Google App Engine. Things may be different.
    val status = headers.get("Status") // Looking for "200 OK"
    val statusCode = if (status == null || status.size == 0) 200 else 
      Integer.parseInt(status.get(0).split(" ")(0))
    (statusCode, StreamUtil.streamToString(conn.getInputStream))
  }
  
  def doPost(url: String) = {
    val method = new PostMethod(url)
    handleCommonMethodSetup(method)
    val result = httpClient.executeMethod(method)
    val responseBody = method.getResponseBodyAsString()
    method.releaseConnection
    (method, result, responseBody)
  }

  private def handleCommonMethodSetup(method: HttpMethod) {
    // Since every call is authenticated we do currently not use cookies.
    method.getParams.setCookiePolicy(CookiePolicy.IGNORE_COOKIES)
  }

  def setCredentials(username: String, password: String) {
    this.username = username
    this.password = password
    httpClient.getState().setCredentials(new AuthScope("twitter.com", 80, AuthScope.ANY_REALM), 
      new UsernamePasswordCredentials(username, password))
    httpClient.getParams.setAuthenticationPreemptive(true)
  }
  
}
