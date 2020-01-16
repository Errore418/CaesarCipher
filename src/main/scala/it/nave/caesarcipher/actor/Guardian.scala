/*
 * Copyright (C) 2019 2020 Claudio Nave
 *
 * This file is part of CaesarCipher.
 *
 * CaesarCipher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CaesarCipher is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package it.nave.caesarcipher.actor

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import it.nave.caesarcipher.actor.CharActor.{CharShift, LinkCharActor}
import it.nave.caesarcipher.gui.OutputDisplayer

object Guardian {

  trait GuardianMessage

  final case class InputString(str: String) extends GuardianMessage

  final case class ResultChar(char: Char, index: Int) extends GuardianMessage

  private val ALPHABETS = List("ABCDEFGHIJKLMNOPQRSTUVWXYZ", "abcdefghijklmnopqrstuvwxyz", "0123456789")
  private val SHIFT = 3

  def apply(encrypt: Boolean, displayer: OutputDisplayer): Behavior[GuardianMessage] = Behaviors.setup { context =>
    context.log.info(s"Guardian actor started in ${if (encrypt) "encrypt" else "decrypt"} mode")
    context.log.info(s"Setting up alphabets $ALPHABETS")
    val CHAR_ACTORS = ALPHABETS
      .flatten
      .map(char => char -> context.spawn(CharActor(char), s"CharActor-$char"))
      .toMap
    context.log.debug("Map of char actors: {}", CHAR_ACTORS)
    for (entry <- CHAR_ACTORS) {
      ALPHABETS
        .find(_.contains(entry._1))
        .map(s => if (encrypt) s else s.reverse)
        .map(s => s -> s.indexOf(entry._1))
        .filter(_._2 != -1)
        .map(tuple => tuple copy (_2 = (tuple._2 + 1) % tuple._1.length))
        .map(tuple => tuple._1.charAt(tuple._2))
        .foreach(c => entry._2 ! LinkCharActor(CHAR_ACTORS(c), context.self))
    }
    Behaviors.receiveMessage {
      case InputString(str) =>
        context.log.info("Received string \"{}\"", str)
        val (knownChars, unknownChars) = str.zipWithIndex.partition(tuple => ALPHABETS.exists(_.contains(tuple._1)))
        knownChars.foreach(tuple => CHAR_ACTORS(tuple._1) ! CharShift(SHIFT, tuple._2))
        unknownChars.foreach(tuple => context.self ! ResultChar(tuple._1, tuple._2))
        Guardian(str.length - 1, List.empty, displayer)
    }
  }

  def apply(count: Int, listOfChars: List[(Int, Char)], displayer: OutputDisplayer): Behavior[GuardianMessage] = Behaviors.receive { (context, message) =>
    context.log.debug("Guardian actor: received message {}", message)
    context.log.debug("Map of chars: {}", listOfChars)
    context.log.debug("Count: {}", count)
    message match {
      case ResultChar(char, index) if count > 0 =>
        Guardian(count - 1, index -> char :: listOfChars, displayer)
      case ResultChar(char, index) if count == 0 =>
        val outputStr = (index -> char :: listOfChars).sortBy(_._1).map(_._2).mkString
        displayer.display(s"Output string: $outputStr")
        Behaviors.stopped
    }
  }

}
