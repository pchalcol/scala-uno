package com.chaltec.examples.uno

import com.chaltec.examples.uno.domain.game.{Deck, EventHandler, CommandHandler}
import com.chaltec.examples.uno.domain.game.Game.{PlayCard, StartGame}
import com.chaltec.examples.uno.port.adapter.EventStore
import com.chaltec.examples.uno.shared.ConsoleUnoConfiguration
import Deck._
import EventStore._

/**
 * @author Patrice Chalcol
 */
object ConsoleUno extends App {
  import ConsoleUnoConfiguration._

  EventStore().subscribe(EventHandler.handle)

  val handle = CommandHandler(readStream, appendToStream).handle

  try {
    handle (StartGame(gameId = 1, playerCount = 4, firstCard = Digit(3, Red)))
    handle (PlayCard(gameId = 1, player = 0, card = Digit(3, Blue)))
    handle (PlayCard(gameId = 1, player = 1, card = Digit(8, Blue)))
    handle (PlayCard(gameId = 1, player = 2, card = Digit(8, Yellow)))
    handle (PlayCard(gameId = 1, player = 3, card = Digit(4, Yellow)))
    handle (PlayCard(gameId = 1, player = 0, card = Digit(4, Green)))
    handle (PlayCard(gameId = 1, player = 1, card = Digit(5, Green)))
    handle (PlayCard(gameId = 1, player = 2, card = Digit(6, Green)))
    handle (PlayCard(gameId = 1, player = 3, card = Digit(6, Red)))

  } catch {
    case e: Throwable => println(s"Exception caught: $e")

  } finally {
    db.close()
  }

}
