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

package it.nave.caesarcypher.actor

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import it.nave.caesarcypher.actor.Guardian.{GuardianMessage, ResultChar}

object CharActor {

  trait CharMessage

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
