package com.chaltec.examples.uno.domain.game

import Deck._
import Game.{CardPlayed, Event, GameStarted}
import org.slf4j.{Logger, LoggerFactory}

/**
 * @author Patrice Chalcol
 */
object EventHandler {
  val log: Logger = LoggerFactory.getLogger("EventHandler")
  val consoleReset = Console.RESET
  var turnCount: Int = 0

  val colors = Map(
    Some("Red") -> Console.RED,
    Some("Green") -> Console.GREEN,
    Some("Blue") -> Console.BLUE,
    Some("Yellow") -> Console.YELLOW,
    None -> Console.WHITE
  )

  def gameId = { id: GameId => id }

  def color(card: Card) = {
    val color = card match {
      case Digit(_, c) => Some(c.toString)
      case KickBack(c) => Some(c.toString)
      case _ => None
    }
    colors(color)
  }

  def handle(event: Event) = event match {
    case e: GameStarted =>
      println(s"Game ${gameId(e.gameId)} started with ${e.playerCount} players")
      val cardColor = color(e.firstCard)
      println(s"[$turnCount] First card: $cardColor${e.firstCard}$consoleReset")

    case e: CardPlayed =>
      turnCount += 1
      val cardColor = color(e.card)
      println(s"[$turnCount] Player ${e.player} played $cardColor${e.card}$consoleReset")
  }
}
