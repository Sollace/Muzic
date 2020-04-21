package com.sollace.muzic;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class Main implements ModInitializer {

    public static final MuzicDiscItem DISC = Registry.register(Registry.ITEM, new Identifier("muzic", "disc"), new MuzicDiscItem());

    @Override
    public void onInitialize() {
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(MuzicDiscRegistry.INSTANCE.serverData);
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(MuzicDiscRegistry.INSTANCE.clientResources);
    }
}
