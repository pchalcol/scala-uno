package com.chaltec.examples.uno

import com.chaltec.examples.uno.domain.game.{Deck, EventHandler}
import Deck._
import com.chaltec.examples.uno.domain.game.EventHandler
import com.chaltec.examples.uno.domain.game.Game.{CardPlayed, GameStarted}
import com.chaltec.examples.uno.port.adapter.serialization.json.Mapper
import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.junit.JUnitRunner
import org.slf4j.{Logger, LoggerFactory}

/**
 * @author Patrice Chalcol
 */
@RunWith(classOf[JUnitRunner])
class ColorTest extends FlatSpec {
  val log: Logger = LoggerFactory.getLogger("ColorTest")
  val cards = List(Digit(3, Red), Digit(3, Blue), Digit(8, Blue), Digit(8, Yellow), Digit(4, Yellow), Digit(4, Green))

  it should "display the adequate color" in {

    cards foreach { card =>
      val cardColor = EventHandler.color(card)
      println(s"$cardColor$card${Console.RESET}")
    }

  }
}

