package com.mattrjacobs.nfl

import java.io.File
import scala.collection.SortedMap

object Boot {
  def main(args: Array[String]) = {
    val files = new File("./files")
    val parsedGamesByYear: List[(File, List[ParsedGame])] =
      files.listFiles().map(f => (f, Parser.parse(f))).toList
    parsedGamesByYear.foreach {
      case (file, parsedList) => {
        //println(s"File ${file} has ${parsedList.size} games")
        val year = parsedList.head.date.getYear

        println(s"======== $year =============")

        printHomeAway(parsedList)
        printCoverTeaserBySpread(parsedList, 14)
        printCoverTeaserByOverUnder(parsedList, 14)
      }
    }
    val allParsedGames = parsedGamesByYear.map(_._2).flatten

    println("========= OVERALL ===============")

    printHomeAway(allParsedGames)
    printCoverTeaserBySpread(allParsedGames, 14)
    printCoverTeaserByOverUnder(allParsedGames, 14)
  }

  def printHomeAway(l: List[ParsedGame]) = {
    val straightUp: (Int, Int, Int) = l.foldLeft((0, 0, 0)) {
      (tuple: (Int, Int, Int), game: ParsedGame) =>
        {
          if (game.homeCover) (tuple._1 + 1, tuple._2, tuple._3)
          else if (game.awayCover) (tuple._1, tuple._2 + 1, tuple._3)
          else if (game.push) (tuple._1, tuple._2, tuple._3 + 1)
          else (tuple._1, tuple._2, tuple._3)
        }
    }
    println(s"Home: ${straightUp._1} Away: ${straightUp._2} Push: ${straightUp._3}")
  }

  def printCoverTeaserBySpread(l: List[ParsedGame], teaserAmount: Int) = {
    //represent key as spread * 10 to get integer and ordering
    //div by 10 when printing
    val coverTeaser: SortedMap[Int, (Int, Int)] =
      l.foldLeft(SortedMap.empty[Int, (Int, Int)]) {
        (m: SortedMap[Int, (Int, Int)], game: ParsedGame) =>
          {
            val awaySpreadKey: Int = (game.awayLine * 10).toInt
            val homeSpreadKey = -1 * awaySpreadKey
            val awayTuple = m.get(awaySpreadKey) match {
              case Some(t) => t
              case None    => (0, 0)
            }
            val updatedTuple = if (game.awayCoverTeaser(teaserAmount)) {
              (awayTuple._1 + 1, awayTuple._2)
            } else {
              (awayTuple._1, awayTuple._2 + 1)
            }
            val updatedMap = m + (awaySpreadKey -> updatedTuple)

            val homeTuple = updatedMap.get(homeSpreadKey) match {
              case Some(t) => t
              case None    => (0, 0)
            }
            val updatedTuple2 = if (game.homeCoverTeaser(teaserAmount)) {
              (homeTuple._1 + 1, homeTuple._2)
            } else {
              (homeTuple._1, homeTuple._2 + 1)
            }
            updatedMap + (homeSpreadKey -> updatedTuple2)
          }
      }

    coverTeaser.foreach {
      case (k, v) => println(s"Spread : ${k / 10.0}: Cover : ${v._1}, Lose: ${v._2}, W% : ${round(v._1 / (v._1 + v._2 + 0.0), 2)}")
    }
  }

  def printCoverTeaserByOverUnder(l: List[ParsedGame], teaserAmount: Int) = {
    //represent key as over/under * 10 to get integer and ordering
    //div by 10 when printing
    val coverTeaser: SortedMap[Int, (Int, Int, Int, Int)] =
      l.foldLeft(SortedMap.empty[Int, (Int, Int, Int, Int)]) {
        (m: SortedMap[Int, (Int, Int, Int, Int)], game: ParsedGame) =>
          {
            val overUnderKey: Int = (game.overUnder * 10).toInt

            val tuple = m.get(overUnderKey) match {
              case Some(t) => t
              case None    => (0, 0, 0, 0)
            }

            val uCover = game.underdogCoverTeaser(teaserAmount)
            val fCover = game.favoriteCoverTeaser(teaserAmount)

            val updatedTuple = (if (fCover) tuple._1 + 1 else tuple._1,
              if (fCover) tuple._2 else tuple._2 + 1,
              if (uCover) tuple._3 + 1 else tuple._3,
              if (uCover) tuple._4 else tuple._4 + 1)

            m + (overUnderKey -> updatedTuple)
          }
      }

    coverTeaser.foreach {
      case (k, v) => {
        println(s"O/U : ${k / 10.0}: Favorite W : ${v._1}, L: ${v._2}, W% : ${round(v._1 / (v._1 + v._2 + 0.0), 2)}")
        println(s"O/U : ${k / 10.0}: Underdog W : ${v._3}, L: ${v._4}, W% : ${round(v._3 / (v._3 + v._4 + 0.0), 2)}")
        println(s"O/U : ${k / 10.0}: Overall W : ${v._1 + v._3}, L: ${v._2 + v._4}, W% : ${round((v._1 + v._3) / (v._1 + v._2 + v._3 + v._4 + 0.0), 2)}")
      }
    }
  }

  def round(v: Double, places: Int): Double = {
    val factor = Math.pow(10, places)
    val newValue = v * factor * 100
    val tmp = Math.round(newValue)
    (tmp / factor)
  }
}
