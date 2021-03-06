package org.talkingpuffin.util

import io.Source

case class WordCounter(text: String) {
  val words: List[WordCount] = calculateCounts(text)
  val frequencies: WordCounter.FreqToStringsMap = calculateBuckets(words)

  case class WordCount(word: String, count: Long) {
    def +(n: Long): WordCount = WordCount(word, count + n)
  }

  private def calculateCounts(text: String): List[WordCount] = {
    val words = text.toLowerCase.split("\\s").toList.
        withFilter(w => w.trim.length > 0 && (w(0).isLetter || w(0) == '#')).
        map(dropTrailingPunctuation) -- WordCounter.stopList
    val emptyMap = Map.empty[String, WordCount].withDefault(w => WordCount(w, 0))
    val countsMap = words.foldLeft(emptyMap) {(map, word) =>
      map(word) += 1
    }
    countsMap.values.toList.sort(_.count > _.count)
  }

  private def dropTrailingPunctuation(word: String) =
    word match {
      case w if ",.:?".contains(w.takeRight(1)) => w.take(w.length-1)
      case w => w
    }

  private def calculateBuckets(wordCounts: List[WordCount]): WordCounter.FreqToStringsMap = {
    val emptyMap = Map.empty[Long,List[String]].withDefaultValue(List[String]())
    wordCounts.foldLeft(emptyMap)((map, wordCount) =>
        map(wordCount.count) = wordCount.word :: map(wordCount.count))
  }
}

object WordCounter {
  type Frequency = Long
  type FreqToStringsMap = Map[Frequency,List[String]]

  private val stopList: List[String] = Source.fromInputStream(
    getClass.getResourceAsStream("/stoplist.csv")).getLines.mkString(",").split(",").toList

}
