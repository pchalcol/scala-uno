package com.chaltec.examples.uno

import com.chaltec.examples.uno.domain.game.Deck
import Deck.{Red, Green, Digit}
import com.chaltec.examples.uno.domain.game.Game.{CardPlayed, GameStarted}
import com.chaltec.examples.uno.port.adapter.serialization.json.Mapper
import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.junit.JUnitRunner
import org.slf4j.{LoggerFactory, Logger}

/**
 * @author Patrice Chalcol
 */
@RunWith(classOf[JUnitRunner])
class MapperTest extends FlatSpec {
  val log: Logger = LoggerFactory.getLogger("MapperTest")

  it should "marshall any event" in {
    val gameStarted = GameStarted(gameId = 1, playerCount = 2, firstCard = Digit(1, Green))
    val json = Mapper.serialize(gameStarted)
    log.debug(s"gameStarted = $json")

    val cardPlayed = CardPlayed(gameId = 1, player = 1, card = Digit(1, Red))
    val json2 = Mapper.serialize(cardPlayed)
    log.debug(s"cardPlayed = $json2")
  }

  it should "unmarshall a json string" in {
    val gameStartedStr =
      """
        {
          "gameId":1,
          "playerCount":2,
          "firstCard":
          {
            "class":"com.chaltec.examples.uno.domain.deck.Model$Digit",
            "value":1,
            "color": { "class":"com.chaltec.examples.uno.domain.deck.Model$Green$" }
          }
        }
      """.stripMargin
    val gameStarted = Mapper.deserialize(gameStartedStr, classOf[GameStarted])
    log.debug(s"gameStarted = $gameStarted")

    val cardPlayedStr =
      """
        {"gameId":1,"player":1,"card":{"class":"com.chaltec.examples.uno.domain.deck.Model$Digit","value":1,"color":{"class":"com.chaltec.examples.uno.domain.deck.Model$Red$"}}
      """.stripMargin
    val cardPlayed = Mapper.deserialize(cardPlayedStr, classOf[CardPlayed])
    log.debug(s"cardPlayed = $cardPlayed")
  }
}
