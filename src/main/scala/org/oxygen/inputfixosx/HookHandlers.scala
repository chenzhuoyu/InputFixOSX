package org.oxygen.inputfixosx

import java.lang.reflect.Method

import net.minecraft.client.gui.inventory.GuiEditSign
import net.minecraft.client.gui.{GuiScreen, GuiScreenBook, GuiTextField}
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.Display

import scala.collection.mutable
import scala.collection.immutable

object HookHandlers
{
    private def getPrivateField(cls: Class[_], name: String) =
    {
        val field = cls.getDeclaredField(name)

        field.setAccessible(true)
        field
    }

    private var keyboard: Any = null
    private var putKeyboardEvent: Method = null

    private val textFields = mutable.HashSet[GuiTextField]()
    private val displayImpl = getPrivateField(classOf[Display], "display_impl").get(null)

    private val MacOSXDisplay = Class.forName("org.lwjgl.opengl.MacOSXDisplay")
    private val MacOSXNativeKeyboard = Class.forName("org.lwjgl.opengl.MacOSXNativeKeyboard")

    private def addKeyEvent(char: Char, keyCode: Int, isDown: Boolean) = isDown match
    {
        case true => putKeyboardEvent.invoke(keyboard,
            keyCode: java.lang.Integer,
            1.toByte: java.lang.Byte,
            char.toInt: java.lang.Integer,
            System.nanoTime(): java.lang.Long,
            false: java.lang.Boolean)

        case false => putKeyboardEvent.invoke(keyboard,
            keyCode: java.lang.Integer,
            0.toByte: java.lang.Byte,
            char.toInt: java.lang.Integer,
            System.nanoTime(): java.lang.Long,
            false: java.lang.Boolean)
    }

    def textSetFocused(text: GuiTextField, focused: Boolean): Unit =
    {
        if (focused)
            textFields += text
        else
            textFields -= text

        if (textFields.nonEmpty)
        {
            InputFix.enableInputMethod()
            InputFix.logger.info("Input methods enabled")
        }
        else
        {
            InputFix.disableInputMethod()
            InputFix.logger.info("Input methods disabled")
        }
    }

    def minecraftInitStream(): Unit =
    {
        keyboard = getPrivateField(MacOSXDisplay, "keyboard").get(displayImpl)
        putKeyboardEvent = MacOSXNativeKeyboard.getMethod(
            "putKeyboardEvent",
            classOf[Int],
            classOf[Byte],
            classOf[Int],
            classOf[Long],
            classOf[Boolean])

        putKeyboardEvent.setAccessible(true)
    }

    def minecraftDisplayGuiScreen(screen: GuiScreen): Unit = screen match
    {
        case null =>
            textFields.clear()
            InputFix.disableInputMethod()
            InputFix.logger.info("Input methods disabled")

        case sign: GuiEditSign =>
            textFields.clear()
            InputFix.enableInputMethod()
            InputFix.logger.info("Input methods enabled for GuiEditSign")

        case book: GuiScreenBook =>
            textFields.clear()
            InputFix.enableInputMethod()
            InputFix.logger.info("Input methods enabled for GuiScreenBook")

        case _ =>
            textFields.clear()
    }

    def injectString(text: String) = for (ch <- text)
    {
        addKeyEvent(ch, -1, isDown = true)
        addKeyEvent(ch, -1, isDown = false)
    }

    def injectKeyCode(keyCode: Int) =
    {
        addKeyEvent(Keyboard.CHAR_NONE.toChar, keyCode, isDown = true)
        addKeyEvent(Keyboard.CHAR_NONE.toChar, keyCode, isDown = false)
    }

    def injectKeyCodeWithShift(keyCode: Int) =
    {
        addKeyEvent(Keyboard.CHAR_NONE.toChar, Keyboard.KEY_LSHIFT, isDown = true)
        addKeyEvent(Keyboard.CHAR_NONE.toChar, keyCode            , isDown = true)
        addKeyEvent(Keyboard.CHAR_NONE.toChar, keyCode            , isDown = false)
        addKeyEvent(Keyboard.CHAR_NONE.toChar, Keyboard.KEY_LSHIFT, isDown = false)
    }
}
