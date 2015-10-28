package org.oxygen.inputfixosx

import java.nio.ByteBuffer

import net.minecraft.client.gui.inventory.GuiEditSign
import net.minecraft.client.gui.{GuiScreenBook, GuiScreen, GuiTextField}
import org.lwjgl.input.Keyboard

import scala.collection.mutable

object HookHandlers
{
    private def getPrivateField(cls: Class[_], name: String) =
    {
        val field = cls.getDeclaredField(name)

        field.setAccessible(true)
        field
    }

    private val textFields = mutable.HashSet[GuiTextField]()
    private val eventBuffer = getPrivateField(classOf[Keyboard], "readBuffer")

    private def eventEnd() = eventBuffer.get(null).asInstanceOf[ByteBuffer].flip()
    private def eventStart(count: Int) =
    {
        val size = count * Keyboard.EVENT_SIZE
        var buffer = eventBuffer.get(null).asInstanceOf[ByteBuffer]

        if (size > buffer.capacity())
        {
            val newBuffer = ByteBuffer.allocate(size)

            newBuffer.put(buffer)
            eventBuffer.set(null, newBuffer)
            buffer = newBuffer
        }

        buffer.limit(buffer.capacity())
    }

    private def addKeyEvent(char: Char, keyCode: Int, isDown: Boolean) =
    {
        val buffer = eventBuffer.get(null).asInstanceOf[ByteBuffer]

        buffer.putInt(keyCode)
        buffer.put((if (isDown) 1 else 0).toByte)
        buffer.putInt(char)
        buffer.putLong(System.nanoTime())
        buffer.put(0.toByte)
    }

    def textSetFocused(text: GuiTextField, focused: Boolean) =
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

    def minecraftDisplayGuiScreen(screen: GuiScreen) = screen match
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

    def injectString(text: String) =
    {
        eventStart(text.length * 2)

        for (ch <- text)
        {
            addKeyEvent(ch, 0, isDown = true)
            addKeyEvent(ch, 0, isDown = false)
        }

        eventEnd()
    }

    def injectInputEvent(char: Char, keyCode: Int) =
    {
        eventStart(2)
        addKeyEvent(char, keyCode, isDown = true)
        addKeyEvent(char, keyCode, isDown = false)
        eventEnd()
    }

    def injectInputEventWithShift(char: Char, keyCode: Int) =
    {
        eventStart(4)
        addKeyEvent(0, Keyboard.KEY_LSHIFT, isDown = true)
        addKeyEvent(char, keyCode, isDown = true)
        addKeyEvent(char, keyCode, isDown = false)
        addKeyEvent(0, Keyboard.KEY_LSHIFT, isDown = false)
        eventEnd()
    }
}
