package com.example.shadermod.mixin;

import com.example.shadermod.ShaderConfig;
import com.example.shadermod.ShaderMod;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class ShaderRendererMixin {
    
    // Track the currently applied shader to avoid redundant applications
    private static String currentShader = null;
    
    @Inject(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/GameRenderer;setupOverlayRendering()V"
        )
    )
    private void beforeOverlay(PoseStack poseStack, float partialTicks, long nanoTime, boolean renderLevel, CallbackInfo ci) {
        try {
            ShaderConfig.ShaderType selected = ShaderConfig.getSelectedShader();
            String shaderName = convertShaderType(selected);
            
            // Only apply if different from current
            if (!shaderName.equals(currentShader)) {
                if (ShaderConfig.areShadersEnabled() && !shaderName.equals("none")) {
                    ShaderMod.getShaderManager().applyShader(shaderName);
                    currentShader = shaderName;
                } else {
                    ShaderMod.getShaderManager().releaseShader();
                    currentShader = "none";
                }
            }
        } catch (Exception e) {
            System.err.println("[ShaderMod] >>> Error in shader renderer (beforeOverlay): " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Inject(
        method = "render",
        at = @At("RETURN")
    )
    private void afterRender(PoseStack poseStack, float partialTicks, long nanoTime, boolean renderLevel, CallbackInfo ci) {
        try {
            // Reset shader state at the end of rendering
            // This prevents shaders from affecting the GUI
            ShaderMod.getShaderManager().releaseShader();
            currentShader = null;
        } catch (Exception e) {
            System.err.println("[ShaderMod] >>> Error in shader renderer (afterRender): " + e.getMessage());
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
}
