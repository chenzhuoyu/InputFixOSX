package org.oxygen.inputfixosx

import java.util

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.MCVersion
import org.lwjgl.Sys

@MCVersion("1.8")
class InputFixOSX extends IFMLLoadingPlugin
{
    /* currently OS X only */
    assert(System.getProperty("os.name") == "Mac OS X", "This mod is for Mac OS X only.")

    /* LWJGL library must be loaded before InputFix library */
    Sys.initialize()
    InputFix.initialize()

    override def injectData(data: util.Map[String, AnyRef]): Unit = ()
    override def getSetupClass: String = null
    override def getModContainerClass: String = "org.oxygen.inputfixosx.ModContainer"
    override def getASMTransformerClass: Array[String] = Array("org.oxygen.inputfixosx.ClassTransformer")
    override def getAccessTransformerClass: String = null
}
