package com.mattrjacobs.nfl

import java.io.File
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import scala.io.Source

case class ParsedGame(date: LocalDate, awayTeam: String, homeTeam: String, awayScore: Int, homeScore: Int, awayLine: Double, overUnder: Double) {
  lazy val homeCover = ((homeScore - awayScore) > awayLine)
  lazy val awayCover = ((homeScore - awayScore) < awayLine)
  lazy val push = ((homeScore - awayScore) == (awayLine.toInt))

  def awayCoverTeaser(teaserAmount: Int): Boolean = ((homeScore - awayScore - teaserAmount) < awayLine)
  def homeCoverTeaser(teaserAmount: Int): Boolean = ((homeScore - awayScore + teaserAmount) > awayLine)
}

object Parser {

  val dtf = DateTimeFormat.forPattern("MM/dd/yyyy")

  def parse(f: File): List[ParsedGame] = {
    Source.fromFile(f).getLines.toList.flatMap(l => {
      val pieces = l.split(",").toList
      pieces match {
        case dateStr :: away :: awayScore :: home :: homeScore :: awayLine :: overUnder :: Nil => try {
          val parsedDate: LocalDate = dtf.parseLocalDate(dateStr)
          val parsedAwayScore: Int = awayScore.toInt
          val parsedHomeScore: Int = homeScore.toInt
          val parsedAwayLine: Double = awayLine.toDouble
          val parsedOverUnder: Double = overUnder.toDouble
          Some(ParsedGame(parsedDate, away, home, parsedAwayScore, parsedHomeScore, parsedAwayLine, parsedOverUnder))
        } catch {
          case ex: Throwable => None
        }
        case _ => None
      }
    })
  }
}
