package org.oxygen.inputfixosx

import java.io.{File, FileOutputStream}

import org.apache.logging.log4j.LogManager
import org.lwjgl.input.Keyboard

object InputFix
{
    val logger = LogManager.getLogger("InputFix-OSX")

    @native def patchKeyEvents()
    @native def enableInputMethod()
    @native def disableInputMethod()

    def initialize(): Unit =
    {
        val file = File.createTempFile("libinputfix-", ".dylib")
        val input = getClass.getResourceAsStream("/libinputfix.dylib")
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
        patchKeyEvents()
    }

    /* these methods below would be invoked through JNI */

    def insertText(text: String): Unit = HookHandlers.injectString(text)
    def executeCommand(command: String): Unit = command match
    {
        case "moveUp:"                                      => HookHandlers.injectKeyCode(Keyboard.KEY_UP)
        case "moveDown:"                                    => HookHandlers.injectKeyCode(Keyboard.KEY_DOWN)
        case "moveLeft:"                                    => HookHandlers.injectKeyCode(Keyboard.KEY_LEFT)
        case "moveRight:"                                   => HookHandlers.injectKeyCode(Keyboard.KEY_RIGHT)

        case "deleteForward:"                               => HookHandlers.injectKeyCode(Keyboard.KEY_DELETE)
        case "deleteBackward:"                              => HookHandlers.injectKeyCode(Keyboard.KEY_BACK)

        case "insertNewline:"                               => HookHandlers.injectKeyCode(Keyboard.KEY_RETURN)
        case "cancelOperation:"                             => HookHandlers.injectKeyCode(Keyboard.KEY_ESCAPE)

        case "scrollPageUp:"                                => HookHandlers.injectKeyCode(Keyboard.KEY_PRIOR)
        case "scrollPageDown:"                              => HookHandlers.injectKeyCode(Keyboard.KEY_NEXT)
        case "scrollToEndOfDocument:"                       => HookHandlers.injectKeyCode(Keyboard.KEY_END)
        case "scrollToBeginningOfDocument:"                 => HookHandlers.injectKeyCode(Keyboard.KEY_HOME)

        case "pageUpAndModifySelection:"                    => HookHandlers.injectKeyCodeWithShift(Keyboard.KEY_PRIOR)
        case "pageDownAndModifySelection:"                  => HookHandlers.injectKeyCodeWithShift(Keyboard.KEY_NEXT)

        case "moveUpAndModifySelection:"                    => HookHandlers.injectKeyCodeWithShift(Keyboard.KEY_UP)
        case "moveDownAndModifySelection:"                  => HookHandlers.injectKeyCodeWithShift(Keyboard.KEY_DOWN)
        case "moveLeftAndModifySelection:"                  => HookHandlers.injectKeyCodeWithShift(Keyboard.KEY_LEFT)
        case "moveRightAndModifySelection:"                 => HookHandlers.injectKeyCodeWithShift(Keyboard.KEY_RIGHT)
        case "moveToEndOfDocumentAndModifySelection:"       => HookHandlers.injectKeyCodeWithShift(Keyboard.KEY_END)
        case "moveToBeginningOfDocumentAndModifySelection:" => HookHandlers.injectKeyCodeWithShift(Keyboard.KEY_HOME)

        case _                                              => logger.error("Unknown command: " + command)
    }
}
