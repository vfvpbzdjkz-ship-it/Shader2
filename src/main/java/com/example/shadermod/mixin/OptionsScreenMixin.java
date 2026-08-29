package com.example.shadermod.mixin;

import com.example.shadermod.ShaderMod;
import com.example.shadermod.ShaderSelectionScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(OptionsScreen.class)
public abstract class OptionsScreenMixin extends Screen {
    
    protected OptionsScreenMixin(Component component) {
        super(component);
    }
    
    @Inject(
        method = "init",
        at = @At("RETURN")
    )
    private void onInit(CallbackInfo ci) {
        System.out.println("[ShaderMod] >>> OptionsScreen init called - adding shader button");
        
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc == null) {
                System.err.println("[ShaderMod] >>> ERROR: Minecraft instance is null in OptionsScreenMixin");
                return;
            }
            
            // Calculate position - we want to add it near the bottom
            int buttonWidth = 150;
            int buttonHeight = 20;
            int x = this.width / 2 - buttonWidth / 2;
            int y = this.height - 65; // Above the Done button
            
            // Add the shader settings button
            this.addRenderableWidget(Button.builder(
                Component.literal("Shader Settings"),
                button -> {
                    System.out.println("[ShaderMod] >>> Shader Settings button clicked!");
                    if (mc != null) {
                        mc.setScreen(new ShaderSelectionScreen((Screen) (Object) this));
                    }
                }
            ).pos(x, y).size(buttonWidth, buttonHeight).build());
            
            System.out.println("[ShaderMod] >>> Shader Settings button added at (" + x + "," + y + ")");
        } catch (Exception e) {
            System.err.println("[ShaderMod] >>> ERROR adding shader button: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
