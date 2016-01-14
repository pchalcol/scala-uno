package com.chaltec.examples.uno.domain.game

import Deck.{Red, Digit, Card}
import org.slf4j.{LoggerFactory, Logger}

/**
 * @author Patrice Chalcol
 */
object Game {
  val log: Logger = LoggerFactory.getLogger("Game")

  abstract class Command
  abstract class Event

  // Commands
  case class StartGame(gameId: GameId, playerCount: Int, firstCard: Card) extends Command
  case class PlayCard(gameId: GameId, player: Int, card: Card) extends Command

  // Events
  case class GameStarted(gameId: GameId, playerCount: Int, firstCard: Card) extends Event
  case class CardPlayed(gameId: GameId, player: Int, card: Card) extends Event

  case object Turn {
    def empty: Turn = (0, 1)
    def start(count: Int): Turn = (0, count)
    def next(player: Turn): Turn = ((player._1 + 1) % player._2, player._2) // ((player + 1) % count, count)
    def isNot(p: Int)(current: Turn): Boolean = p != current._1
  }

  case class State(gameAlreadyStarted: Boolean, player: Turn, topCard: Card)

  case object State {
    val initial = State(gameAlreadyStarted = false, Turn.empty, Digit(0, Red))
  }

  /**
   * Start game command.
   * @param command
   * @return
   */
  def startGame(command: StartGame): (State => Event) = { state: State =>
    if (command.playerCount <= 2)
      throw new IllegalArgumentException("Invalid playerCount value. You should be at least 3 players")

    if (state.gameAlreadyStarted)
      throw new IllegalStateException("You cannot start game twice")

    GameStarted(gameId = command.gameId, playerCount = command.playerCount, firstCard = command.firstCard)
  }

  /**
   *
   * @param command
   * @return
   */
  def playCard(command: PlayCard): (State => Event) = { state: State =>
    if (Turn.isNot(command.player)(state.player))
      throw new IllegalStateException("Player should play at his turn")

    (command.card, state.topCard) match {
      case (Digit(n1, color1), Digit(n2, color2)) if n1.equals(n2) || color1.toString.equals(color2.toString) =>
        CardPlayed(gameId = command.gameId, player = command.player, card = command.card)

      case _ => throw new IllegalStateException("Play same color or same value !")
    }
  }

  /**
   *
   * @return
   */
  def handle(c: Command): (State => Event) = c match {
    case command: StartGame => startGame(command)
    case command: PlayCard => playCard(command)
    case command: Any => throw new IllegalArgumentException(s"Command $command is Not supported")
  }

  /**
   *
   * @param state
   * @param e
   * @return
   */
  def evolve(state: State, e: Event): State = e match {
    case e: GameStarted =>
      val st = State(gameAlreadyStarted = true, player = Turn.start(e.playerCount), topCard = e.firstCard)
      log.debug(s"state = ${Console.RED}$st${Console.RESET}")
      st
    case e: CardPlayed =>
      val st = state.copy(player = Turn.next(state.player), topCard = e.card)
      log.debug(s"state = ${Console.RED}$st${Console.RESET}")
      st
    case e: Any => throw new IllegalArgumentException(s"Event $e is Not supported")
  }
}
