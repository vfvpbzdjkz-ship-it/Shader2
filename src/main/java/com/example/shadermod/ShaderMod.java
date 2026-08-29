package com.example.shadermod;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
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
        
        // Register config
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ShaderConfig.CLIENT_SPEC);
    }
    
    private void commonSetup(final FMLCommonSetupEvent event) {
        System.out.println("[ShaderMod] Common setup complete");
    }
    
    private void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            shaderManager = new ShaderManager();
            shaderManager.initialize();
            System.out.println("[ShaderMod] Client setup complete - shaders loaded");
            
            // Apply the configured shader
            applyConfiguredShader();
        });
    }
    
    private void applyConfiguredShader() {
        if (shaderManager != null && ShaderConfig.areShadersEnabled()) {
            ShaderConfig.ShaderType selected = ShaderConfig.getSelectedShader();
            String shaderName = convertShaderType(selected);
            if (shaderName != null && !shaderName.equals("none")) {
                shaderManager.applyShader(shaderName);
                System.out.println("[ShaderMod] Applied shader: " + shaderName);
            }
        }
    }
    
    private String convertShaderType(ShaderConfig.ShaderType type) {
        switch (type) {
            case VIBRANT: return "vibrant";
            case CEL_SHADING: return "cel_shading";
            case PBR_BASIC: return "pbr_basic";
            case ULTRA_REALISTIC: return "ultra_realistic";
            default: return "none";
        }
    }
    
    public static ShaderMod getInstance() {
        return instance;
    }
    
    public ShaderManager getShaderManager() {
        return shaderManager;
    }
}
