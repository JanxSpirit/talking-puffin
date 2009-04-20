package com.davebsoft.sctw.ui

import javax.swing.table.{AbstractTableModel}
import javax.swing.{Icon, ImageIcon}

/**
 * A JTable cell with an asynchronously-loaded image in it.
 * @author Dave Briccetti
 */

class PictureCell(model: AbstractTableModel, column: Int) {
  val picFetcher = new PictureFetcher(None, (imageReady: PictureFetcher.ImageReady) => {
    if (imageReady.resource.image.getIconHeight <= Thumbnail.THUMBNAIL_SIZE) {
      val row = imageReady.id.asInstanceOf[Int]
      model.fireTableCellUpdated(row, column)
    }
  })
    
  def request(picUrl: String, rowIndex: Int): Icon = {
    picFetcher.getCachedObject(picUrl) match {
      case Some(imageWithScaled) => imageWithScaled.image
      case None => {
        picFetcher.requestItem (picFetcher.FetchImageRequest(picUrl, rowIndex.asInstanceOf[Object]))
        Thumbnail.transparentThumbnail
      }
    }
  }
}
