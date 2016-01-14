package com.chaltec.examples.uno.port.adapter

import com.chaltec.examples.uno.domain.game.Game.{CardPlayed, GameStarted, Event}
import com.chaltec.examples.uno.port.adapter.serialization.json.Mapper
import com.chaltec.examples.uno.port.adapter.serialization.json.Mapper._
import com.chaltec.examples.uno.shared.ConsoleUnoConfiguration._

import org.iq80.leveldb._
import org.fusesource.leveldbjni.JniDBFactory._
import org.slf4j.LoggerFactory
import scala.util.{Failure, Success, Try}

object EventStore {
  val log = LoggerFactory.getLogger("com.chaltec.examples.uno.port.adapter.EventStore")

  val delegate: InternalEventStore = InternalEventStore()

  def apply() = delegate

  /**
   *
   * @param streamId
   * @param expectedVersion
   * @param newEvents
   * @return
   */
  def appendToStream(streamId: String, expectedVersion: Int, newEvents: Seq[Event]): Boolean = {
    Try(delegate.doInsertEvents(streamId, expectedVersion, newEvents)) match {
      case Success(_) =>
        delegate.subscribers foreach { _(newEvents.head) }
        true
      case Failure(e) => false
    }
  }

  /**
   *
   * @param streamId
   * @param version
   * @param count
   * @return
   */
  def readStream(streamId: String, version: Int, count: Int): (Seq[Event], Int, Option[Int]) = {
    val data = Try(delegate.doReadEvents(streamId, version, count)) match {
      case Success((events, lastEventNumber, nextEventNumber)) => (events, lastEventNumber, nextEventNumber)
      case Failure(e) =>
        log.error(e.getMessage, e)
        throw e
    }

    data match {
      case (events, lastEventNumber, nextEventNumber) =>
        (events.asInstanceOf[Seq[Event]],
          lastEventNumber.asInstanceOf[Int],
          nextEventNumber.asInstanceOf[Option[Int]])
    }
  }
}

object InternalEventStore {
  def apply() = new InternalEventStore(List())
}

/**
 *
 * @param subscribers
 */
class InternalEventStore(var subscribers: List[Event => Unit]) {
  val log = LoggerFactory.getLogger(classOf[InternalEventStore])

  /**
   *
   * @param projection
   */
  def subscribe(projection: Event => Unit) = {
    subscribers = projection +: subscribers
    this
  }

  private[adapter] def doInsertEvents(streamId: String, expectedVersion: Int, newEvents: Seq[Event]) = {
    val serializedEvents: Seq[String] = newEvents map serialize

    log.debug(s"${Console.RED}Serialized events: $serializedEvents${Console.RESET}")
    log.debug(s"About to insert ${serializedEvents.head} with expected version $expectedVersion in leveldb with key $streamId")

    db.put(bytes(s"$streamId::$expectedVersion"), bytes(s"${serializedEvents.head}::$expectedVersion"))

    log.debug(s"${serializedEvents.head} with expected version $expectedVersion inserted in leveldb with key $streamId")
  }

  private[adapter] def doReadEvents(streamId: String, version: Int, count: Int) = {
    def deserialize(event: String): Event = {
      if (event contains "firstCard") Mapper.deserialize(event, classOf[GameStarted])
      else Mapper.deserialize(event, classOf[CardPlayed])
    }

    val iterator: DBIterator = db.iterator()
    var events = List[Event]()
    var lastEventNumber: Int = 0
    var nextEventNumber: Option[Int] = None

    try {
      iterator.seekToFirst()
      while (iterator.hasNext) {
        val elem = iterator.peekNext()

        val key = asString(elem.getKey)
        val value = asString(elem.getValue)
        log.debug(s"$key = $value")

        val strArray = value.split("::")
        val event = strArray(0)
        val eventNumber = strArray(1)

        if (key startsWith streamId) { // filtre sur les clefs
          events = deserialize(event) +: events
          lastEventNumber = eventNumber.toInt + 1
          if (lastEventNumber < version + count)
            nextEventNumber = None
          else
            nextEventNumber = Some(lastEventNumber)
        }
        iterator.next
      }
      log.debug(s"${Console.RED}Deserialized events: $events${Console.RESET}")

      (events.reverse, lastEventNumber, nextEventNumber)
    }
    finally {
      iterator.close()
    }
  }
}
