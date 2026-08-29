package com.example.shadermod;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.glfw.GLFW;

@Mod(ShaderMod.MODID)
public class ShaderMod {
    public static final String MODID = "shadermod";
    
    private static ShaderMod instance;
    private ShaderManager shaderManager;
    
    // Key binding for opening shader selection screen
    // We'll use 'S' key by default (can be changed in controls)
    
    public ShaderMod() {
        instance = this;
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::commonSetup);
        modBus.addListener(this::clientSetup);
        
        // Register config
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ShaderConfig.CLIENT_SPEC);
        
        System.out.println("[ShaderMod] Constructor - Mod initialized");
    }
    
    private void commonSetup(final FMLCommonSetupEvent event) {
        System.out.println("[ShaderMod] Common setup complete");
    }
    
    private void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            System.out.println("[ShaderMod] Starting client setup...");
            shaderManager = new ShaderManager();
            shaderManager.initialize();
            System.out.println("[ShaderMod] Client setup complete - " + shaderManager.getLoadedShaderCount() + " shaders loaded");
            
            // Apply the configured shader
            applyConfiguredShader();
        });
    }
    
    private void applyConfiguredShader() {
        if (shaderManager != null) {
            ShaderConfig.ShaderType selected = ShaderConfig.getSelectedShader();
            String shaderName = convertShaderType(selected);
            
            if (ShaderConfig.areShadersEnabled() && shaderName != null && !shaderName.equals("none")) {
                shaderManager.applyShader(shaderName);
                System.out.println("[ShaderMod] Applied configured shader: " + shaderName);
            } else {
                System.out.println("[ShaderMod] No shader applied (disabled or none selected)");
            }
        }
    }
    
    private String convertShaderType(ShaderConfig.ShaderType type) {
        if (type == null) return "none";
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
