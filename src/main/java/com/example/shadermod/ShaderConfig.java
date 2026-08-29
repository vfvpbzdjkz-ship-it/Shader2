package com.example.shadermod;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ShaderConfig {
    public static final ModConfigSpec CLIENT_SPEC;
    public static final Client CLIENT;
    
    public static class Client {
        public final ModConfigSpec.BooleanValue enableShaders;
        public final ModConfigSpec.EnumValue<ShaderType> selectedShader;
        
        public Client(ModConfigSpec.Builder builder) {
            builder.push("Shader Settings");
            
            enableShaders = builder
                .comment("Enable or disable shaders globally")
                .define("enableShaders", true);
            
            selectedShader = builder
                .comment("Select which shader to use")
                .defineEnum("selectedShader", ShaderType.NONE);
            
            builder.pop();
        }
    }
    
    public enum ShaderType {
        NONE,
        VIBRANT,
        CEL_SHADING,
        PBR_BASIC,
        ULTRA_REALISTIC
    }
    
    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        CLIENT = new Client(builder);
        CLIENT_SPEC = builder.build();
    }
    
    public static ShaderType getSelectedShader() {
        return CLIENT.selectedShader.get();
    }
    
    public static boolean areShadersEnabled() {
        return CLIENT.enableShaders.get();
    }
    
    public static void setSelectedShader(ShaderType shader) {
        CLIENT.selectedShader.set(shader);
        CLIENT_SPEC.save();
    }
    
    public static void setShadersEnabled(boolean enabled) {
        CLIENT.enableShaders.set(enabled);
        CLIENT_SPEC.save();
    }
}
