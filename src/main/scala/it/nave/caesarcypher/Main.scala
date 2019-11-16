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
  print("Inserisci una stringa da criptare: ")
  StdIn
    .readLine()
    .view
    .zipWithIndex
    .foreach(println(_))
}

object Character {

  sealed trait Message {
    val shift: Int
    val index: Int

    abstract def decrement(): Message
  }

  final case class Crypt(shift: Int, index: Int) extends Message {
    override def decrement() = Crypt(shift - 1, index)
  }

  final case class Decrypt(shift: Int, index: Int) extends Message {
    override def decrement() = Decrypt(shift - 1, index)
  }

  def apply(char: Char, nextChar: ActorRef[Message], previousChar: ActorRef[Message]): Behavior[Message] = {
    Behaviors.receive { (context, message) =>
      message match {
        case crypt: Crypt =>
          context.log.info("Actor of char {}: received crypt message {}", char, crypt)
          handleMessage(crypt, nextChar)
        case decrypt: Decrypt =>
          context.log.info("Actor of char {}: received decrypt message {}", char, decrypt)
          handleMessage(decrypt, previousChar)
      }
      Behaviors.same
    }
  }

  private def handleMessage(message: Message, replyTo: ActorRef[Message]): Unit = {
    if (message.shift > 0) replyTo ! message.decrement else println("TODO Implementare raccolta e stampa dei caratteri finali") // TODO
  }

}