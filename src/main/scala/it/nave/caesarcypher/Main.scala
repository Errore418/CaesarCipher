/*
 * Copyright (C) 2019 Claudio Nave
 *
 * This file is part of CaesarCypher.
 *
 * CaesarCypher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CaesarCypher is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package it.nave.caesarcypher

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import it.nave.caesarcypher.CharActor.CharShift
import it.nave.caesarcypher.Guardian.{GuardianMessage, InputString, ResultChar}

import scala.io.StdIn

object Main extends App {

  val ENCRYPT_CHOICE = "1"
  val DECRYPT_CHOICE = "2"

  val response = StdIn.readLine(s"############## WELCOME TO CAESER CIPHER AKKA BASED ##############\nPress ($ENCRYPT_CHOICE) to encrypt or ($DECRYPT_CHOICE) to decrypt: ")
  if (ENCRYPT_CHOICE == response || DECRYPT_CHOICE == response) {
    val inputStr = StdIn.readLine("Insert a string to elaborate: ")
    ActorSystem(Guardian(ENCRYPT_CHOICE == response), "GuardianActor") ! InputString(inputStr)
  } else {
    println(s""" "${response}" is not a valid choice """.trim) // https://stackoverflow.com/questions/21086263/how-to-insert-double-quotes-into-string-with-interpolation-in-scala
  }

}

object Guardian {

  sealed trait GuardianMessage

  final case class InputString(str: String) extends GuardianMessage

  final case class ResultChar(char: Char, index: Int) extends GuardianMessage

  private val ALPHABETS = List("ABCDEFGHIJKLMNOPQRSTUVWXYZ", "abcdefghijklmnopqrstuvwxyz", "0123456789")
  private val SHIFT = 3

  def apply(encrypt: Boolean): Behavior[GuardianMessage] = Behaviors.setup { context =>
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
        .foreach(c => entry._2 ! CharActor.LinkCharActor(CHAR_ACTORS(c), context.self))
    }
    Behaviors.receiveMessage {
      case InputString(str) =>
        context.log.info("Received string \"{}\"", str)
        val (knownChars, unknownChars) = str
          .zipWithIndex
          .partition(tuple => ALPHABETS.exists(_.contains(tuple._1)))
        knownChars.foreach(tuple => CHAR_ACTORS(tuple._1) ! CharShift(SHIFT, tuple._2))
        unknownChars.foreach(tuple => context.self ! ResultChar(tuple._1, tuple._2))
        Guardian(str.length - 1, List.empty)
    }
  }

  def apply(count: Int, listOfChars: List[(Int, Char)]): Behavior[GuardianMessage] = Behaviors.receive { (context, message) =>
    context.log.info("Guardian actor: received message {}", message)
    context.log.debug("Map of chars: {}", listOfChars)
    context.log.debug("Count: {}", count)
    message match {
      case ResultChar(char, index) if count > 0 =>
        Guardian(count - 1, index -> char :: listOfChars)
      case ResultChar(char, index) if count == 0 =>
        val outputStr = (index -> char :: listOfChars).sortBy(_._1).map(_._2).mkString
        println(s"Output string: $outputStr")
        Behaviors.stopped
    }
  }

}

object CharActor {

  sealed trait CharMessage

  final case class CharShift(shift: Int, index: Int) extends CharMessage

  final case class LinkCharActor(nextActorChar: ActorRef[CharMessage], replyTo: ActorRef[GuardianMessage]) extends CharMessage

  def apply(char: Char, nextActorChar: ActorRef[CharMessage], replyTo: ActorRef[GuardianMessage]): Behavior[CharMessage] = Behaviors.receive { (context, message) =>
    context.log.info("Actor of char {}: received message {}", char, message)
    message match {
      case CharShift(shift, index) if shift > 0 => nextActorChar ! CharShift(shift - 1, index)
      case CharShift(shift, index) if shift == 0 => replyTo ! ResultChar(char, index)
    }
    Behaviors.same
  }

  def apply(char: Char): Behavior[CharMessage] = Behaviors.receive { (context, message) =>
    context.log.debug("Actor of char {}: received message {}", char, message)
    message match {
      case LinkCharActor(nextActorChar, replyTo) => CharActor(char, nextActorChar, replyTo)
    }
  }

}