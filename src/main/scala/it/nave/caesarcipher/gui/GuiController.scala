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
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control.{Alert, TextArea, TextField}
import scalafxml.core.macros.sfxml

/**
 * Classe che viene associata a un file fxml e sovrintende l’esecuzione della rispettiva schermata.
 * Riceve in costruzione gli oggetti grafici che presentano id coincidenti con i nomi dei parametri ed espone metodi che
 * possono venire invocati a seguito di particolari eventi nella schermata (es. click di un bottone).
 *
 * @param input  [[scalafx.scene.control.TextField TextField]] contenente testo da criptare
 * @param output [[scalafx.scene.control.TextField TextField]] contentente testo da decriptare
 */
@sfxml
class GuiController(private val input: TextField,
                    private val output: TextField) {

  /**
   * Metodo invocato al click del bottone per criptare.
   *
   * @param event l'evento lanciato
   */
  def encrypt(event: ActionEvent): Unit = {
    setUpEnvironment(Guardian.ENCRYPT, input.text().trim, output)
  }

  /**
   * Metodo invocato al click del bottone per decriptare.
   *
   * @param event l'evento lanciato
   */
  def decrypt(event: ActionEvent): Unit = {
    setUpEnvironment(Guardian.DECRYPT, output.text().trim, input)
  }

  /**
   * Metodo invocato al click del bottone per pulire i campi.
   *
   * @param event l'evento lanciato
   */
  def clean(event: ActionEvent): Unit = {
    input.text.value = ""
    output.text.value = ""
  }

  /**
   * Metodo invocato al click del bottone per la schermata di about.
   * Setta e mostra un [[scalafx.scene.control.Alert Alert]] con il testo della licenza.
   *
   * @param event l'evento lanciato
   */
  def info(event: ActionEvent): Unit = {
    val alert = new Alert(AlertType.Information)
    alert.title = "About"
    alert.headerText = "License and credits"
    val textArea = new TextArea(
      """Copyright (C) 2019 2020  Claudio Nave
        |
        |CaesarCipher is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
        |
        |CaesarCipher is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
        |
        |You should have received a copy of the GNU General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>.
        |
        |Startup icon (Augustus Of Prima Porta) made by Freepik from www.flaticon.com""".stripMargin)
    textArea.editable = false
    textArea.wrapText = true
    alert.getDialogPane.setContent(textArea)
    alert.show()
  }

  /**
   * Metodo per far partire l'ambiente di Akka e criptare/decriptare un testo.
   * Dopo aver fatto fatto partire l'attore [[it.nave.caesarcipher.actor.Guardian Guardian]] con i parametri specificati
   * gli invia un messaggio con la stringa di input.
   *
   * @param encrypt         specifica se l'ambiente va avviato per criptare (true) o per decriptare (false)
   * @param inputStr        la stringa da criptare/descriptare
   * @param outputTextField TextField su cui mostrare il risultato dell'elaborazione
   */
  def setUpEnvironment(encrypt: Boolean, inputStr: String, outputTextField: TextField): Unit = {
    if (!inputStr.isEmpty) {
      ActorSystem(Guardian(encrypt, new TextFieldDisplayer(outputTextField)), "GuardianActor") ! InputString(inputStr)
    }
  }

}
