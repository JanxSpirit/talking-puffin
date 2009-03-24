package com.davebsoft.sctw.ui

import javax.swing.Icon
import javax.swing.table.{AbstractTableModel}

/**
 * A JTable cell with an asynchronously-loaded image in it.
 * @author Dave Briccetti
 */

class PictureCell(model: AbstractTableModel) {
  val picFetcher = new PictureFetcher((imageReady: ImageReady) => {
    if (imageReady.imageIcon.getIconHeight <= Thumbnail.THUMBNAIL_SIZE) {
      val row = imageReady.id.asInstanceOf[Int]
      model.fireTableCellUpdated(row, 0)
    }
  }, true)
    
  def request(picUrl: String, rowIndex: Int): Icon = {
    PictureFetcher.imageCache.get(picUrl) match { 
      case Some(imageIcon) => {
        imageIcon
      }
      case None => {
        picFetcher ! new FetchImage(picUrl, rowIndex.asInstanceOf[Object])
        Thumbnail.transparentPic
      }
    }
  }
}
