package org.oxygen.inputfixosx

import java.util

import com.google.common.eventbus.EventBus
import net.minecraftforge.fml.common.{DummyModContainer, LoadController, ModMetadata}

class ModContainer extends DummyModContainer(new ModMetadata())
{
    getMetadata.url = ""
    getMetadata.name = "InputFix OS X"
    getMetadata.modId = "inputfixosx"
    getMetadata.version = "0.1-alpha"
    getMetadata.authorList = util.Arrays.asList("Oxygen Chen")
    getMetadata.description = "Chinese Input Fix for Mac OS X"

    override def registerBus(bus: EventBus, controller: LoadController): Boolean =
    {
        bus.register(this)
        true
    }
}
