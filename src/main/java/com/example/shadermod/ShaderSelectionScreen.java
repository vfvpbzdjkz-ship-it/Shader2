package com.example.shadermod;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Arrays;
import java.util.List;

public class ShaderSelectionScreen extends Screen {
    
    private final Screen parentScreen;
    private final ShaderManager shaderManager;
    private Button enableToggleButton;
    private Button doneButton;
    
    private static final List<ShaderInfo> SHADERS = Arrays.asList(
        new ShaderInfo("None", "none", "Disable all shaders"),
        new ShaderInfo("Vibrant", "vibrant", "Enhanced colors with saturation and glow"),
        new ShaderInfo("Cel Shading", "cel_shading", "Cartoon/anime style rendering"),
        new ShaderInfo("PBR Basic", "pbr_basic", "Standard physically-based rendering"),
        new ShaderInfo("Ultra Realistic", "ultra_realistic", "Advanced PBR with GI and atmospheric effects")
    );
    
    private static class ShaderInfo {
        final String displayName;
        final String internalName;
        final String description;
        
        ShaderInfo(String displayName, String internalName, String description) {
            this.displayName = displayName;
            this.internalName = internalName;
            this.description = description;
        }
    }
    
    public ShaderSelectionScreen(Screen parentScreen) {
        super(Component.literal("Shader Selection"));
        this.parentScreen = parentScreen;
        this.shaderManager = ShaderMod.getInstance() != null ? ShaderMod.getInstance().getShaderManager() : null;
    }
    
    @Override
    protected void init() {
        int centerX = this.width / 2;
        int startY = this.height / 4;
        int buttonWidth = 200;
        int buttonHeight = 20;
        int spacing = 25;
        
        // Title
        // We'll use a simple text draw instead of StringWidget for compatibility
        
        // Enable/Disable toggle
        boolean enabled = ShaderConfig.areShadersEnabled();
        enableToggleButton = this.addRenderableWidget(Button.builder(
            Component.literal(enabled ? "Shaders: ON" : "Shaders: OFF"),
            button -> {
                boolean newEnabled = !ShaderConfig.areShadersEnabled();
                ShaderConfig.setShadersEnabled(newEnabled);
                enableToggleButton.setMessage(Component.literal(newEnabled ? "Shaders: ON" : "Shaders: OFF"));
                
                // Apply immediately
                if (shaderManager != null) {
                    if (newEnabled) {
                        ShaderConfig.ShaderType selected = ShaderConfig.getSelectedShader();
                        String shaderName = convertToInternalName(selected);
                        if (shaderName != null && !shaderName.equals("none")) {
                            shaderManager.applyShader(shaderName);
                        }
                    } else {
                        shaderManager.releaseShader();
                    }
                }
            }
        ).pos(centerX - buttonWidth / 2, startY - 30).size(buttonWidth, buttonHeight).build());
        
        // Shader buttons
        int yPos = startY;
        for (ShaderInfo shader : SHADERS) {
            final String shaderName = shader.internalName;
            Button button = this.addRenderableWidget(Button.builder(
                Component.literal(shader.displayName),
                btn -> {
                    // Select this shader
                    ShaderConfig.ShaderType selectedType = convertToShaderType(shaderName);
                    ShaderConfig.setSelectedShader(selectedType);
                    
                    // Apply immediately if shaders are enabled
                    if (ShaderConfig.areShadersEnabled() && shaderManager != null) {
                        if (shaderName.equals("none")) {
                            shaderManager.releaseShader();
                        } else {
                            shaderManager.applyShader(shaderName);
                        }
                    }
                }
            ).pos(centerX - buttonWidth / 2, yPos).size(buttonWidth, buttonHeight).build());
            
            yPos += spacing;
        }
        
        // Done button
        doneButton = this.addRenderableWidget(Button.builder(
            Component.literal("Done"),
            button -> this.onClose()
        ).pos(centerX - buttonWidth / 2, yPos + 20).size(buttonWidth, buttonHeight).build());
    }
    
    private ShaderConfig.ShaderType convertToShaderType(String shaderName) {
        switch (shaderName) {
            case "vibrant": return ShaderConfig.ShaderType.VIBRANT;
            case "cel_shading": return ShaderConfig.ShaderType.CEL_SHADING;
            case "pbr_basic": return ShaderConfig.ShaderType.PBR_BASIC;
            case "ultra_realistic": return ShaderConfig.ShaderType.ULTRA_REALISTIC;
            default: return ShaderConfig.ShaderType.NONE;
        }
    }
    
    private String convertToInternalName(ShaderConfig.ShaderType type) {
        if (type == null) return "none";
        switch (type) {
            case VIBRANT: return "vibrant";
            case CEL_SHADING: return "cel_shading";
            case PBR_BASIC: return "pbr_basic";
            case ULTRA_REALISTIC: return "ultra_realistic";
            default: return "none";
        }
    }
    
    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(parentScreen);
        }
    }
    
    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        // Render background
        this.renderBackground(poseStack);
        
        // Render buttons
        super.render(poseStack, mouseX, mouseY, partialTicks);
        
        // Draw title
        int centerX = this.width / 2;
        int startY = this.height / 4;
        this.font.draw(poseStack, "ShaderMod 2 - Shader Selection", centerX - 100, startY - 50, 0xFFFFFF);
        
        // Draw descriptions when hovering over shader buttons
        for (int i = 0; i < SHADERS.size(); i++) {
            ShaderInfo shader = SHADERS.get(i);
            int buttonY = startY + i * 25;
            
            // Check if mouse is over this button area
            if (mouseX >= centerX - 100 && mouseX <= centerX + 100 &&
                mouseY >= buttonY && mouseY <= buttonY + 20) {
                this.font.draw(poseStack, shader.description, centerX - 150, buttonY + 25, 0xAAAAAA);
            }
        }
        
        // Draw current selection info
        ShaderConfig.ShaderType selected = ShaderConfig.getSelectedShader();
        String selectedName = selected.toString().charAt(0) + selected.toString().substring(1).toLowerCase().replace("_", " ");
        this.font.draw(poseStack, "Selected: " + selectedName, centerX - 100, startY + 120, 0xFFFFFF);
        
        // Draw shader count
        if (shaderManager != null) {
            this.font.draw(poseStack, "Loaded: " + shaderManager.getLoadedShaderCount() + " shaders", centerX - 100, startY + 140, 0xAAAAAA);
        }
    }
}
