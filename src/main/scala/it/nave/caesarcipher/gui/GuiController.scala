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

package it.nave.caesarcipher.gui

import akka.actor.typed.ActorSystem
import it.nave.caesarcipher.actor.Guardian
import it.nave.caesarcipher.actor.Guardian.InputString
import scalafx.event.ActionEvent
import scalafx.scene.control.TextField
import scalafxml.core.macros.sfxml

@sfxml
class GuiController(private val input: TextField,
                    private val output: TextField) {

  def encrypt(event: ActionEvent): Unit = {
    setUpEnvironment(Guardian.ENCRYPT, input.text(), output)
  }

  def decrypt(event: ActionEvent): Unit = {
    setUpEnvironment(Guardian.DECRYPT, output.text(), input)
  }

  def clean(event: ActionEvent): Unit = {
    input.text.value = ""
    output.text.value = ""
  }

  private def setUpEnvironment(encrypt: Boolean, inputStr: String, outputTextField: TextField): Unit = {
    if (!inputStr.isBlank) {
      ActorSystem(Guardian(encrypt, new TextFieldDisplayer(outputTextField)), "GuardianActor") ! InputString(inputStr)
    }
  }

}
