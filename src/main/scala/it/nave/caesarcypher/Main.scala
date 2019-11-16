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
import it.nave.caesarcypher.Guardian.{CaesarMessage, LinkCharActor}

import scala.io.StdIn

object Main extends App {
  print("Inserisci una stringa da criptare: ")
  StdIn
    .readLine()
    .view
    .zipWithIndex
    .foreach(println(_))
}

object Guardian {

  trait CaesarMessage

  final case class LinkCharActor(nextActorChar: ActorRef[CaesarMessage]) extends CaesarMessage

  def apply(): Behavior[CaesarMessage] =
    Behaviors.setup { context =>
      ???
    }

}

object Character {

  final case class CharShift(shift: Int, index: Int) extends CaesarMessage {
    def decrement: CharShift = CharShift(shift - 1, index)
  }

  def apply(char: Char, nextActorChar: ActorRef[CaesarMessage]): Behavior[CaesarMessage] = {
    Behaviors.receive { (context, message) =>
      context.log.info("Actor of char {}: received message {}", char, message)
      message match {
        case charShift: CharShift if (charShift.shift > 0) => nextActorChar ! charShift.decrement
        case charShift: CharShift if (charShift.shift == 0) => ??? // TODO Implementare raccolta e stampa dei caratteri finali
        case unknown => context.log.error("Received unknown message {}", unknown)
      }
      Behaviors.same
    }
  }

  def apply(char: Char): Behavior[CaesarMessage] = {
    Behaviors.receive { (context, message) =>
      message match {
        case LinkCharActor(nextActorChar) =>
          context.log.info("Actor of char {}: received a link message to the actor {}", char, nextActorChar)
          Character(char, nextActorChar)
        case unknown =>
          context.log.error("Received unknown message {}", unknown)
          Behaviors.stopped
      }
    }
  }

}