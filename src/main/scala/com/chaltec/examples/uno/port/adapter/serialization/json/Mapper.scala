package com.chaltec.examples.uno.port.adapter.serialization.json

import com.chaltec.examples.uno.domain.game.Game.Event
import org.boon.json.{JsonParserFactory, JsonFactory, JsonSerializerFactory}

/**
 * @author Patrice Chalcol
 */
object Mapper {
  val parserFactory: JsonParserFactory = new JsonParserFactory()
  val serializerFactory = new JsonSerializerFactory()

  // on peut customiser le comportement des parsers
  parserFactory.lax()

  // et des serializers
  serializerFactory.useFieldsOnly()

  def serialize(event: Event) = {
    val mapper = JsonFactory.create(parserFactory, serializerFactory)
    mapper.toJson(event)
  }

  def deserialize[T](eventStr: String, targetType: Class[T]) = {
    val mapper = JsonFactory.create(parserFactory, serializerFactory)
    mapper.fromJson(eventStr, targetType)
  }
}
