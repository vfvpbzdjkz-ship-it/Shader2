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
    
    @Inject(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/GameRenderer;setupOverlayRendering()V"
        ),
        require = 1
    )
    private void onRender(PoseStack poseStack, float partialTicks, long nanoTime, boolean renderLevel, CallbackInfo ci) {
        // Apply shaders during rendering based on configuration
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
}
