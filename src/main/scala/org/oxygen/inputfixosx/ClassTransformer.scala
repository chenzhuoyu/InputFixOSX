package org.oxygen.inputfixosx

import net.minecraft.launchwrapper.IClassTransformer
import org.objectweb.asm._

class ClassTransformer extends IClassTransformer
{
    private def patchMinecraft(bytes: Array[Byte]): Array[Byte] =
    {
        val reader = new ClassReader(bytes)
        val writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES)

        reader.accept(new ClassVisitor(Opcodes.ASM5, writer)
        {
            override def visitMethod(access: Int, name: String, desc: String, signature: String, exceptions: Array[String]): MethodVisitor =
            {
                val method = super.visitMethod(access, name, desc, signature, exceptions)

                if (name != "displayGuiScreen" && (name != "a" || desc != "(Lbxf;)V"))
                    return method

                method.visitIntInsn(Opcodes.ALOAD, 1)
                method.visitMethodInsn(
                    Opcodes.INVOKESTATIC,
                    "org/oxygen/inputfixosx/HookHandlers",
                    "minecraftDisplayGuiScreen",
                    "(Lnet/minecraft/client/gui/GuiScreen;)V",
                    false)

                method
            }
        }, 0)

        InputFix.logger.info("Patched class \"net.minecraft.client.Minecraft\"")
        writer.toByteArray
    }

    private def patchGuiTextField(bytes: Array[Byte]): Array[Byte] =
    {
        val reader = new ClassReader(bytes)
        val writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES)

        reader.accept(new ClassVisitor(Opcodes.ASM5, writer)
        {
            override def visitMethod(access: Int, name: String, desc: String, signature: String, exceptions: Array[String]): MethodVisitor =
            {
                val method = super.visitMethod(access, name, desc, signature, exceptions)

                if (name != "setFocused" && (name != "b" || desc != "(Z)V"))
                    return method

                method.visitIntInsn(Opcodes.ALOAD, 0)
                method.visitIntInsn(Opcodes.ILOAD, 1)
                method.visitMethodInsn(
                    Opcodes.INVOKESTATIC,
                    "org/oxygen/inputfixosx/HookHandlers",
                    "textSetFocused",
                    "(Lnet/minecraft/client/gui/GuiTextField;Z)V",
                    false)

                method
            }
        }, 0)

        InputFix.logger.info("Patched class \"net.minecraft.client.gui.GuiTextField\"")
        writer.toByteArray
    }

    override def transform(name: String, transformed: String, bytes: Array[Byte]): Array[Byte] = transformed match
    {
        case "net.minecraft.client.Minecraft"        => patchMinecraft(bytes)
        case "net.minecraft.client.gui.GuiTextField" => patchGuiTextField(bytes)
        case _                                       => bytes
    }
}
