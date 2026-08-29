package com.example.shadermod;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ShaderMod.MODID)
public class ShaderMod {
    public static final String MODID = "shadermod";
    
    private static ShaderMod instance;
    private ShaderManager shaderManager;
    
    public ShaderMod() {
        instance = this;
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::commonSetup);
        modBus.addListener(this::clientSetup);
    }
    
    private void commonSetup(final FMLCommonSetupEvent event) {
        // Common setup for both client and server
        System.out.println("[ShaderMod] Common setup complete");
    }
    
    private void clientSetup(final FMLClientSetupEvent event) {
        // Client-specific setup
        event.enqueueWork(() -> {
            shaderManager = new ShaderManager();
            shaderManager.initialize();
            System.out.println("[ShaderMod] Client setup complete - shaders loaded");
        });
    }
    
    public static ShaderMod getInstance() {
        return instance;
    }
    
    public ShaderManager getShaderManager() {
        return shaderManager;
    }
}
