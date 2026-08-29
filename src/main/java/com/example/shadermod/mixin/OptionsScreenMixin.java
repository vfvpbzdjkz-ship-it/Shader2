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
        // Position it below the other buttons
        
        // Find a good position - we'll add it at the bottom
        int buttonWidth = 200;
        int buttonHeight = 20;
        int x = this.width / 2 - buttonWidth / 2;
        int y = this.height - 40; // Near the bottom
        
        this.addRenderableWidget(Button.builder(
            Component.literal("Shader Settings"),
            button -> {
                // Open the shader selection screen
                if (this.minecraft != null) {
                    this.minecraft.setScreen(new ShaderSelectionScreen((Screen) (Object) this));
                }
            }
        ).pos(x, y).size(buttonWidth, buttonHeight).build());
    }
}
