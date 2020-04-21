package com.sollace.muzic;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;

public class Client implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ColorProviderRegistry.ITEM.register((stack, layer) -> Main.DISC.getColor(stack, layer), Main.DISC);
    }
}
