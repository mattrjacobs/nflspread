package com.mattrjacobs.nfl

import java.io.File

object Boot {
  def main(args: Array[String]) = {
    val files = new File("./files")
    val parsedGamesByYear: List[(File, List[ParsedGame])] =
      files.listFiles().map(f => (f, Parser.parse(f))).toList
    parsedGamesByYear.foreach {
      case (file, parsedList) =>
        println(s"File ${file} has ${parsedList.size} games")
    }
    val allParsedGames = parsedGamesByYear.map(_._2).flatten
    println(s"Parsed ${allParsedGames.size} total games")
    allParsedGames.take(5).foreach(println)
  }
}
