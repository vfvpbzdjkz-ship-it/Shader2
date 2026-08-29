package com.example.shadermod.mixin;

import com.example.shadermod.ShaderSelectionScreen;
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
        // Add "Shader Settings" button to the options screen
        // We'll add it to the existing buttons list
        
        try {
            // Get the buttons field from OptionsScreen
            // This is a bit hacky but necessary since we can't directly access the buttons list
            
            // Find the "Done" button and add our button next to it
            int buttonWidth = 150;
            int buttonHeight = 20;
            int x = this.width / 2 - buttonWidth / 2;
            
            // Try to find a good position - we'll place it near the bottom but above "Done"
            int y = this.height - 65; // Above the Done button
            
            this.addRenderableWidget(Button.builder(
                Component.literal("Shader Settings"),
                button -> {
                    // Open the shader selection screen
                    if (this.minecraft != null) {
                        this.minecraft.setScreen(new ShaderSelectionScreen((Screen) (Object) this));
                    }
                }
            ).pos(x, y).size(buttonWidth, buttonHeight).build());
            
            System.out.println("[ShaderMod] Added Shader Settings button to Options screen");
        } catch (Exception e) {
            System.err.println("[ShaderMod] Error adding Shader Settings button: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
