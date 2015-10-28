package org.oxygen.inputfixosx

import java.io.{FileOutputStream, File}

import org.apache.logging.log4j.LogManager
import org.lwjgl.input.Keyboard

object InputFix
{
    val logger = LogManager.getLogger("InputFix-OSX")

    private def loadLibrary(resource: String) =
    {
        val file = File.createTempFile("libinputfix-", ".dylib")
        val input = getClass.getResourceAsStream(resource)
        val output = new FileOutputStream(file)

        val data = Array.fill[Byte](1024)(0)
        var length = input.read(data)

        while (length != -1)
        {
            output.write(data, 0, length)
            length = input.read(data)
        }

        input.close()
        output.close()
        file.deleteOnExit()
        System.load(file.getAbsolutePath)
    }

    @native def patchKeyEvents()
    @native def enableInputMethod()
    @native def disableInputMethod()

    def initialize(): Unit =
    {
        loadLibrary("/libinputfix.dylib")
        patchKeyEvents()
    }

    /* these methods below would be invoked through JNI */

    def insertText(text: String): Unit = HookHandlers.injectString(text)
    def executeCommand(command: String): Unit = command match
    {
        case "moveUp:"          => HookHandlers.injectInputEvent(0, Keyboard.KEY_UP)
        case "moveDown:"        => HookHandlers.injectInputEvent(0, Keyboard.KEY_DOWN)
        case "moveLeft:"        => HookHandlers.injectInputEvent(0, Keyboard.KEY_LEFT)
        case "moveRight:"       => HookHandlers.injectInputEvent(0, Keyboard.KEY_RIGHT)
        case "insertNewline:"   => HookHandlers.injectInputEvent(0, Keyboard.KEY_RETURN)
        case "deleteForward:"   => HookHandlers.injectInputEvent(0, Keyboard.KEY_DELETE)
        case "deleteBackward:"  => HookHandlers.injectInputEvent(0, Keyboard.KEY_BACK)
        case "cancelOperation:" => HookHandlers.injectInputEvent(0, Keyboard.KEY_ESCAPE)

        case "moveUpAndModifySelection:"    => HookHandlers.injectInputEventWithShift(0, Keyboard.KEY_UP)
        case "moveDownAndModifySelection:"  => HookHandlers.injectInputEventWithShift(0, Keyboard.KEY_DOWN)
        case "moveLeftAndModifySelection:"  => HookHandlers.injectInputEventWithShift(0, Keyboard.KEY_LEFT)
        case "moveRightAndModifySelection:" => HookHandlers.injectInputEventWithShift(0, Keyboard.KEY_RIGHT)

        case _ => logger.error("Unknown command: " + command)
    }
}
