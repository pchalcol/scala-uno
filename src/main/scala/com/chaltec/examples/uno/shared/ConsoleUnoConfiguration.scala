package com.chaltec.examples.uno.shared

import java.io.File

import org.fusesource.leveldbjni.JniDBFactory._
import org.iq80.leveldb._

/**
 * @author Patrice Chalcol
 */
object ConsoleUnoConfiguration {
  // leveldb options
  val options: Options = new Options
  options.createIfMissing(true)

  // reset db
  val dbDirectory = new File("target/game")
  factory.destroy(dbDirectory, options)
  implicit val db: DB = factory.open(dbDirectory, options)

}
