package com.chaltec.examples.uno.domain.game

import Game._
import org.slf4j.{Logger, LoggerFactory}

import scala.annotation.tailrec

/**
 * @author Patrice Chalcol
 */
object CommandHandler {

  def apply(readStream: (String, Int, Int) => (Seq[Event], Int, Option[Int]),
            appendToStream: (String, Int, Seq[Event]) => Boolean) =
    new CommandHandler(readStream, appendToStream)
}

class CommandHandler(readStream: (String, Int, Int) => (Seq[Event], Int, Option[Int]),
                     appendToStream: (String, Int, Seq[Event]) => Boolean) {
  val log: Logger = LoggerFactory.getLogger("CommandHandler")

  def gameId(command: Command) = command match {
    case StartGame(id, _, _) => id
    case PlayCard(id, _, _) => id
  }

  def streamId(gameId: GameId) = s"Game-$gameId"

  def load(gameId: GameId): (Int, State) = {
    @tailrec
    def fold(state: State, version: Int): (Int, State) = {
      log.debug(s"fold.state = $state")
      val (events, lastEvent, nextEvent) = readStream(streamId(gameId), version, 500)
      val st: State = events.foldLeft(state)(evolve)
      log.debug(s"fold.st = $st")
      nextEvent match {
        case None => (lastEvent, st)
        case Some(n) => fold(st, n)
      }
    }
    fold(State.initial, 0)
  }

  def save(gameId: GameId, expectedVersion: Int, events: Seq[Event]) = {
    appendToStream(streamId(gameId), expectedVersion, events)
  }

  @inline
  def mapsnd[S, V, E](f:(S => E))(v: V, s: S) = (v, f(s))

  def handle: (Command => Boolean) = { command =>
    val id = gameId(command)
    val state: (Int, State) = load(id)
    val resultedEvent: (/*expectedVersion*/Int, /*resultedEvent*/Event) =
      mapsnd[State, Int, Event](Game.handle(command))(state._1, state._2)
    save(id, resultedEvent._1, List(resultedEvent._2))
  }
}
