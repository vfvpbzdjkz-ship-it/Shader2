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
    
    // Flag to prevent multiple applications
    private static boolean shaderApplied = false;
    
    @Inject(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/GameRenderer;setupOverlayRendering()V"
        )
    )
    private void onRender(PoseStack poseStack, float partialTicks, long nanoTime, boolean renderLevel, CallbackInfo ci) {
        // Only apply shader once per frame to avoid issues
        if (shaderApplied) return;
        shaderApplied = true;
        
        try {
            ShaderMod mod = ShaderMod.getInstance();
            if (mod != null && mod.getShaderManager() != null) {
                ShaderConfig.ShaderType selected = ShaderConfig.getSelectedShader();
                String shaderName = convertShaderType(selected);
                
                if (ShaderConfig.areShadersEnabled() && shaderName != null && !shaderName.equals("none")) {
                    mod.getShaderManager().applyShader(shaderName);
                } else {
                    mod.getShaderManager().releaseShader();
                }
            }
        } catch (Exception e) {
            System.err.println("[ShaderMod] Error in shader renderer mixin: " + e.getMessage());
            e.printStackTrace();
        } finally {
            shaderApplied = false;
        }
    }
    
    @Inject(
        method = "render",
        at = @At("RETURN")
    )
    private void onRenderEnd(PoseStack poseStack, float partialTicks, long nanoTime, boolean renderLevel, CallbackInfo ci) {
        // Release shader at the end of rendering
        try {
            ShaderMod mod = ShaderMod.getInstance();
            if (mod != null && mod.getShaderManager() != null) {
                // Only release if we're not using a shader or if rendering is done
                mod.getShaderManager().releaseShader();
            }
        } catch (Exception e) {
            System.err.println("[ShaderMod] Error releasing shader: " + e.getMessage());
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
