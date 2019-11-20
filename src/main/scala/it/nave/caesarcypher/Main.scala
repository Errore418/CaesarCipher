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

import akka.actor.typed.ActorSystem
import it.nave.caesarcypher.actor.Guardian
import it.nave.caesarcypher.actor.Guardian.InputString

import scala.io.StdIn

object Main extends App {

  val ENCRYPT_CHOICE = "1"
  val DECRYPT_CHOICE = "2"

  val response = StdIn.readLine(s"############## WELCOME TO CAESER CIPHER AKKA BASED ##############\nPress ($ENCRYPT_CHOICE) to encrypt or ($DECRYPT_CHOICE) to decrypt: ")
  if (ENCRYPT_CHOICE == response || DECRYPT_CHOICE == response) {
    val inputStr = StdIn.readLine("Insert a string to elaborate: ")
    ActorSystem(Guardian(ENCRYPT_CHOICE == response), "GuardianActor") ! InputString(inputStr)
  } else {
    println(s""" "${response}" is not a valid choice """.trim)
  }

}
