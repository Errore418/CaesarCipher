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

package it.nave.caesarcipher

import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene
import scalafx.scene.image.Image
import scalafxml.core.{FXMLView, NoDependencyResolver}

/**
 * Classe di partenza del programma.
 * Estende [[scalafx.application.JFXApp JFXApp]], un trait di ScalaFX utile ad avviare il flusso di JavaFX e
 * specificarne la prima schermata.
 */
object GuiMain extends JFXApp {
  stage = new PrimaryStage() {
    title = "CaesarCipher"
    scene = new Scene(FXMLView(getClass getResource "/gui.fxml", NoDependencyResolver))
    icons add new Image(getClass getResourceAsStream "/images/caesar.png")
  }
}
