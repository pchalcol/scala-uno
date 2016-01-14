package com.chaltec.examples.uno.domain.game

/**
 * @author Patrice Chalcol
 */
object Deck {
  abstract class Color {
    def equals(other: Color) = other != null &&
      other.toString.equals(this.toString)
  }
  case object Red extends Color
  case object Green extends Color
  case object Blue extends Color
  case object Yellow extends Color

  abstract class Direction
  case object ClockWise extends Direction
  case object CounterClockWise extends Direction

  abstract class Card
  case class Digit(value: Int, color: Color) extends Card
  case class KickBack(color: Color) extends Card
}
