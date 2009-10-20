package org.talkingpuffin.ui.util
import java.util.concurrent.{Executors, LinkedBlockingQueue}
import com.google.common.collect.MapMaker
import java.util.{Collections, HashSet}
import org.talkingpuffin.ui.SwingInvoke
import org.talkingpuffin.util.Loggable
import org.talkingpuffin.cache.Cache

/**
 * Fetches resources in the background, and calls a function in the Swing event thread when ready.
 */
abstract class BackgroundResourceFetcher[K,V](processResource: (ResourceReady[K,V]) => Unit) 
    extends Cache[K,V] with Loggable {
  val cache: java.util.Map[K,V] = new MapMaker().softValues().makeMap()
  val requestQueue = new LinkedBlockingQueue[FetchRequest[K]]
  val inProgress = Collections.synchronizedSet(new HashSet[K])
  val threadPool = Executors.newFixedThreadPool(15)
  var running = true

  val thread = new Thread(new Runnable {
    def run = {
      while (running)
        processNextRequestWithPoolThread
    }
  }, "BGFetcher " + hashCode)
  thread.start

  /**
   * Returns the object if it exists in the cache, otherwise None.
   */
  def getCachedObject(key: K): Option[V] = {
    val obj = cache.get(key)
    if (obj != null) Some(obj) else None
  }

  /**
   * Requests that an item be fetched in a background thread. If the key is already in the 
   * cache, the request is ignored. 
   */
  def requestItem(request: FetchRequest[K]) = 
    if (! cache.containsKey(request.key) && 
        ! requestQueue.contains(request) && ! inProgress.contains(request.key)) 
      requestQueue.put(request)
  
  def stop = {
    if (running) {
      running = false
      threadPool.shutdownNow
      thread.interrupt
    }
  }
  
  private def processNextRequestWithPoolThread {
    try {
      val fetchRequest = requestQueue.take
      val key = fetchRequest.key
      inProgress.add(key)

      threadPool.execute(new Runnable {
        def run {
          try {
            var resource = cache.get(key)
            if (resource == null) {
              resource = getResourceFromSource(key)
              store(cache, key, resource)
            }

            SwingInvoke.later({processResource(new ResourceReady[K,V](key, fetchRequest.userData, resource))})
          } catch {
            case e: NoSuchResource => // Do nothing
          }
          inProgress.remove(key)
        }
      })
    } catch {
      case e: InterruptedException => // This is how the thread is ended
    }
  }
  
  protected def getResourceFromSource(key: K): V
}

case class FetchRequest[K](val key: K, val userData: Object)

class ResourceReady[K,V](val key: K, val userData: Object, val resource: V)

case class NoSuchResource(resource: String) extends Exception
