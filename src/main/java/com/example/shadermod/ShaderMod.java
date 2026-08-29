package com.example.shadermod;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ShaderMod.MODID)
public class ShaderMod {
    public static final String MODID = "shadermod";
    
    @OnlyIn(Dist.CLIENT)
    private static ShaderMod instance;
    
    @OnlyIn(Dist.CLIENT)
    private static ShaderManager shaderManager;
    
    public ShaderMod() {
        instance = this;
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::commonSetup);
        
        // Register config for client-side only
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ShaderConfig.CLIENT_SPEC);
        
        System.out.println("[ShaderMod] >>> MOD CONSTRUCTOR CALLED - Mod ID: " + MODID);
        System.out.println("[ShaderMod] >>> This means the mod JAR is being loaded!");
    }
    
    private void commonSetup(final FMLCommonSetupEvent event) {
        System.out.println("[ShaderMod] >>> COMMON SETUP CALLED");
        event.enqueueWork(() -> {
            System.out.println("[ShaderMod] >>> Common setup enqueued work");
        });
    }
    
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onClientSetup(FMLClientSetupEvent event) {
        System.out.println("[ShaderMod] >>> CLIENT SETUP CALLED - THIS IS THE KEY!");
        
        try {
            shaderManager = new ShaderManager();
            shaderManager.initialize();
            System.out.println("[ShaderMod] >>> ShaderManager created and initialized");
            System.out.println("[ShaderMod] >>> Loaded " + shaderManager.getLoadedShaderCount() + " shaders");
            
            // Apply configured shader
            applyConfiguredShader();
        } catch (Exception e) {
            System.err.println("[ShaderMod] >>> ERROR IN CLIENT SETUP: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @OnlyIn(Dist.CLIENT)
    private void applyConfiguredShader() {
        if (shaderManager != null) {
            ShaderConfig.ShaderType selected = ShaderConfig.getSelectedShader();
            String shaderName = convertShaderType(selected);
            
            System.out.println("[ShaderMod] >>> Applying configured shader: " + shaderName);
            
            if (ShaderConfig.areShadersEnabled() && shaderName != null && !shaderName.equals("none")) {
                shaderManager.applyShader(shaderName);
                System.out.println("[ShaderMod] >>> Shader applied successfully: " + shaderName);
            } else {
                System.out.println("[ShaderMod] >>> Shaders disabled or none selected");
            }
        }
    }
    
    @OnlyIn(Dist.CLIENT)
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
    
    @OnlyIn(Dist.CLIENT)
    public static ShaderMod getInstance() {
        return instance;
    }
    
    @OnlyIn(Dist.CLIENT)
    public static ShaderManager getShaderManager() {
        return shaderManager;
    }
}
