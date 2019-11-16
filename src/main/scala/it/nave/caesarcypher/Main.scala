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
import akka.actor.typed.{ActorRef, Behavior}

import scala.io.StdIn

object Main extends App {

  val ENCRYPT_CHOICE = "1"
  val DECRYPT_CHOICE = "2"

  println("############## WELCOME TO CAESER CIPHER AKKA BASED ##############")
  print(s"Press ($ENCRYPT_CHOICE) to encrypt or ($DECRYPT_CHOICE) to decrypt: ")
  val response = StdIn.readLine()
  if (ENCRYPT_CHOICE == response || DECRYPT_CHOICE == response) {
    print("Insert a string to elaborate: ")
    val str = StdIn.readLine()
    println(s"String scelta : $str") // TODO Avviare elaborazione
  } else {
    println(s""" "${response}" is not a valid choice """.trim) // https://stackoverflow.com/questions/21086263/how-to-insert-double-quotes-into-string-with-interpolation-in-scala
  }
}

object Guardian {

  trait Command

  def apply(): Behavior[Command] =
    Behaviors.setup { context =>
      ??? // TODO Configurazione dell'ambiente
    }

}

object CharActor {

  trait CharMessage

  final case class CharShift(shift: Int, index: Int) extends CharMessage {
    def decrement: CharShift = CharShift(shift - 1, index)
  }

  final case class LinkCharActor(nextActorChar: ActorRef[CharMessage]) extends CharMessage

  def apply(char: Char, nextActorChar: ActorRef[CharMessage]): Behavior[CharMessage] = {
    Behaviors.receive { (context, message) =>
      context.log.info("Actor of char {}: received message {}", char, message)
      message match {
        case charShift: CharShift if (charShift.shift > 0) => nextActorChar ! charShift.decrement
        case charShift: CharShift if (charShift.shift == 0) => ??? // TODO Implementare raccolta e stampa dei caratteri finali
        case _ => context.log.error("Received unknown message")
      }
      Behaviors.same
    }
  }

  def apply(char: Char): Behavior[CharMessage] = {
    Behaviors.receive { (context, message) =>
      context.log.info("Actor of char {}: received message {}", char, message)
      message match {
        case LinkCharActor(nextActorChar) => CharActor(char, nextActorChar)
        case _ =>
          context.log.error("Received unknown message")
          Behaviors.stopped
      }
    }
  }

}