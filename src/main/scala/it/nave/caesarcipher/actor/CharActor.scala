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

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import it.nave.caesarcipher.actor.Guardian.{GuardianMessage, ResultChar}

/**
 * Object contenente metodi utili alla creazione di attori rappresentanti un singolo carattere.
 */
object CharActor {

  /**
   * Trait base per i messaggi.
   */
  trait CharMessage

  /**
   * Messaggio contenente lo shift di un carattere ancora da eseguire e il suo relativo indice.
   *
   * @param shift lo shift del carattere ancora da eseguire
   * @param index l'indice del carattere
   */
  final case class CharShift(shift: Int, index: Int) extends CharMessage

  /**
   * Messaggio contenente il prossimo attore a cui redirigere i messaggi e l'attore a cui rispondere quando lo shifting
   * è terminato.
   *
   * @param nextActorChar il prossimo attore
   * @param replyTo       l'attore a cui rispondere quando lo shifting è terminato
   */
  final case class LinkCharActor(nextActorChar: ActorRef[CharMessage], replyTo: ActorRef[GuardianMessage]) extends CharMessage

  /**
   * Metodo per costruire un [[akka.actor.typed.Behavior Behavior]] per aspettare il messaggio
   * [[it.nave.caesarcipher.actor.CharActor.LinkCharActor LinkCharActor]].
   *
   * @param c il carattere assegnato all'attore
   * @return il [[akka.actor.typed.Behavior Behavior]] da usare per il prossimo messaggio
   */
  def apply(c: Char): Behavior[CharMessage] = Behaviors.receive { (context, message) =>
    context.log.debug("Actor of char {}: received message {}", c, message)
    message match {
      case LinkCharActor(nextActorChar, replyTo) => CharActor(c, nextActorChar, replyTo)
    }
  }

  /**
   * Metodo per costruire un [[akka.actor.typed.Behavior Behavior]] per aspettare messaggi
   * [[it.nave.caesarcipher.actor.CharActor.CharShift CharShift]]. Se lo shift è positivo, il messaggio viene inoltrato
   * all'attore successivo decrementando lo shift. Se lo shift è nullo viene mandato un
   * [[it.nave.caesarcipher.actor.Guardian.ResultChar ResultChar]] a replyTo.
   *
   * @param c             il carattere assegnato all'attore
   * @param nextActorChar il prossimo attore a cui mandare [[it.nave.caesarcipher.actor.CharActor.CharShift CharShift]]
   * @param replyTo       l'attore a cui mandare [[it.nave.caesarcipher.actor.Guardian.ResultChar ResultChar]]
   * @return il [[akka.actor.typed.Behavior Behavior]] da usare per il prossimo messaggio
   */
  def apply(c: Char, nextActorChar: ActorRef[CharMessage], replyTo: ActorRef[GuardianMessage]): Behavior[CharMessage] = Behaviors.receive { (context, message) =>
    context.log.debug("Actor of char {}: received message {}", c, message)
    message match {
      case CharShift(shift, index) if shift > 0 => nextActorChar ! CharShift(shift - 1, index)
      case CharShift(shift, index) if shift == 0 => replyTo ! ResultChar(c, index)
    }
    Behaviors.same
  }

}
