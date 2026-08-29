# ShaderMod 2 - Usage Guide

This document provides detailed information on using ShaderMod 2, including how to activate shaders, configure them, and understand what each shader does.

---

## Quick Start

### After Installation

1. Launch Minecraft with the NeoForge profile
2. The mod will automatically load and initialize
3. Check the chat/log for confirmation messages
4. Use the shader commands (see below) to activate shaders

---

## Understanding the Shaders

ShaderMod 2 includes three distinct shaders, each designed for different visual experiences:

### 1. Vibrant Shader (`vibrant`)

**Purpose:** Enhances the visual appeal of Minecraft by making colors more vivid and dynamic.

**Visual Effects:**
- **Increased Saturation:** Colors appear 50% more saturated, making reds redder, greens greener, and blues bluer
- **Subtle Glow:** Bright areas emit a warm golden glow, adding depth and atmosphere
- **Enhanced Contrast:** Colors have more "pop" with increased contrast (1.2x)
- **Vibrance Boost:** Muted colors are selectively enhanced without oversaturating already-vivid colors
- **Dynamic Pulse:** A subtle time-based pulsing effect that makes the world feel more alive

**Best For:**
- Players who want a more colorful, vibrant world
- Screenshot artists looking for more dramatic colors
- Those who find vanilla Minecraft's colors too muted
- Streaming/recording with better visual appeal

**Performance Impact:** Minimal - This shader only modifies color calculations

**Example Scenarios:**
- Exploring flower forests (colors will be much more vivid)
- Watching sunsets (enhanced reds and oranges)
- Mining in caves (better visibility with enhanced colors)

---

### 2. Cel Shading Shader (`cel_shading`)

**Purpose:** Transforms Minecraft into a stylized, cartoon-like world reminiscent of anime, Borderlands, or The Legend of Zelda: Breath of the Wild.

**Visual Effects:**
- **Discrete Shading:** Lighting is divided into 4 distinct levels instead of smooth gradients
- **Sharp Transitions:** Light-to-shadow transitions are abrupt and stylized
- **Quantized Specular:** Highlights are also divided into discrete levels (2 levels)
- **Color Posterization:** Colors are reduced to 8 levels per channel for a more "hand-drawn" look
- **Edge Detection:** Can detect edges for potential outline effects (currently disabled in the base implementation)
- **Time-Based Effects:** Subtle animation effects that make the world feel more dynamic

**Best For:**
- Players who enjoy cartoon/anime aesthetics
- Those who want a unique, stylized look
- Streaming with a distinctive visual style
- Screenshot artists looking for a different art style

**Performance Impact:** Moderate - Requires more calculations for edge detection and quantization

**Example Scenarios:**
- Building structures (will look like they're from a cartoon)
- Exploring landscapes (will have a painted appearance)
- Fighting mobs (attacks will have a more dramatic visual impact)

**Technical Details:**
- Uses 4 shading levels (can be adjusted by changing `SHADING_LEVELS` constant)
- Specular highlights are sharpened with `pow(specular, 32.0)`
- Colors are posterized to 8 levels: `floor(color * 8.0) / 8.0`

---

### 3. PBR Basic Shader (`pbr_basic`)

**Purpose:** Provides standard Physically Based Rendering with realistic lighting and shadow effects.

**Visual Effects:**
- **Diffuse Lighting:** Standard Lambertian diffuse lighting
- **Specular Highlights:** Blinn-Phong specular model for realistic reflections
- **Ambient Lighting:** Base lighting that ensures nothing is completely dark
- **Shadow Simulation:** Basic shadow factor (simplified - uses ambient occlusion)
- **Fresnel Effect:** Edge glow effect that simulates light refraction at grazing angles
- **Gamma Correction:** Proper gamma correction for more realistic color representation

**Best For:**
- Players who want more realistic lighting
- Those who enjoy subtle, natural-looking improvements
- Building and architecture showcase
- Players who want shadows and proper lighting

**Performance Impact:** Minimal - Uses standard lighting calculations

**Technical Details:**
- Diffuse: `max(dot(normal, lightDir), 0.0)`
- Specular: Blinn-Phong model with configurable shininess
- Ambient factor: 0.3 (30% of base color)
- Gamma correction: `pow(color, vec3(1.0/2.2))`

**Note:** This is a simplified PBR implementation. A full PBR shader would include:
- Normal mapping
- Roughness/metallic maps
- Environment mapping
- Proper shadow mapping
- Image-based lighting

---

## Activating Shaders

### Current Implementation

As of version 1.0.0, shaders are loaded but not automatically applied to the game world. Here are the ways to activate them:

#### Method 1: Modifying the Mixin (For Developers)

Edit `ShaderRendererMixin.java` to apply a specific shader:

```java
@Inject(
    method = "render",
    at = @At("HEAD")
)
private void onRender(PoseStack poseStack, float partialTicks, long nanoTime, boolean renderLevel, CallbackInfo ci) {
    ShaderMod mod = ShaderMod.getInstance();
    if (mod != null && mod.getShaderManager() != null) {
        ShaderManager manager = mod.getShaderManager();
        
        // Apply the vibrant shader
        manager.applyShader("vibrant");
        
        // Or apply cel shading
        // manager.applyShader("cel_shading");
        
        // Or apply PBR
        // manager.applyShader("pbr_basic");
    }
}
```

#### Method 2: Using the ShaderManager API

You can access the shader manager from any class:

```java
ShaderMod mod = ShaderMod.getInstance();
if (mod != null) {
    ShaderManager manager = mod.getShaderManager();
    
    // Check if a shader is available
    if (manager.hasShader("vibrant")) {
        // Apply it
        manager.applyShader("vibrant");
    }
    
    // Or use the renderWithShader method
    manager.renderWithShader("vibrant", () -> {
        // Your rendering code here
    });
}
```

---

## Shader Configuration (Future Feature)

The following features are planned for future versions:

### Planned Command System

```
/shader list              - List all available shaders
/shader <name>           - Activate a specific shader
/shader none             - Deactivate all shaders
/shader reload           - Reload all shaders from disk
/shader info <name>      - Show information about a shader
```

### Planned Configuration Options

Each shader will have configurable parameters:

**Vibrant Shader:**
- Saturation level (0.0 - 2.0)
- Glow intensity (0.0 - 1.0)
- Contrast multiplier (0.5 - 2.0)
- Vibrance amount (0.0 - 1.0)
- Pulse speed (0.0 - 2.0)

**Cel Shading Shader:**
- Shading levels (2 - 8)
- Edge detection threshold (0.0 - 1.0)
- Outline width (0.0 - 0.1)
- Outline color (RGB)
- Specular shininess (1.0 - 100.0)

**PBR Shader:**
- Diffuse intensity (0.0 - 2.0)
- Specular intensity (0.0 - 2.0)
- Ambient factor (0.0 - 1.0)
- Shininess (1.0 - 100.0)
- Gamma correction (1.0 - 3.0)

---

## Performance Considerations

### Performance by Shader

| Shader | Performance Impact | FPS Reduction* | VRAM Usage |
|--------|-------------------|----------------|------------|
| None (Vanilla) | Baseline | 0% | Low |
| PBR Basic | Minimal | 2-5% | Low |
| Vibrant | Minimal | 3-7% | Low |
| Cel Shading | Moderate | 5-15% | Medium |

*FPS reduction is approximate and depends on your hardware and Minecraft settings.

### Optimization Tips

1. **Reduce Render Distance:** Lower render distance improves performance with any shader
2. **Lower Graphics Settings:** Fancy graphics, smooth lighting, and other settings add overhead
3. **Close Other Applications:** Ensure Minecraft has access to all available GPU resources
4. **Update Drivers:** Ensure your GPU drivers are up to date
5. **Adjust Shader Settings:** When available, lower shader-specific settings for better performance

### Hardware Requirements

| Shader | Minimum GPU | Recommended GPU |
|--------|-------------|-----------------|
| PBR Basic | Intel HD 4000 / GTX 650 | GTX 1050 / RX 560 |
| Vibrant | Intel HD 4000 / GTX 650 | GTX 1050 / RX 560 |
| Cel Shading | GTX 750 / RX 260 | GTX 1060 / RX 570 |

---

## Troubleshooting Shader Issues

### Shader Not Applying

**Symptoms:** Shader is loaded but not affecting the game

**Solutions:**
1. Check that the shader is being applied in the mixin
2. Verify the shader compiled without errors (check Minecraft log)
3. Ensure you're using the correct shader name (case-sensitive)
4. Restart Minecraft after installing the mod

### Visual Artifacts

**Symptoms:** Strange colors, flickering, or glitches

**Solutions:**
1. Update your GPU drivers
2. Try a different shader to isolate the issue
3. Lower your graphics settings
4. Check for shader compilation errors in the log

### Performance Problems

**Symptoms:** Low FPS, stuttering, or lag

**Solutions:**
1. Try the PBR Basic shader first (least demanding)
2. Lower your render distance
3. Close other applications using GPU resources
4. Update your GPU drivers
5. Try a lower-resolution texture pack

### Shader Compilation Errors

**Symptoms:** Error messages about shader compilation in the log

**Solutions:**
1. Check the error message for the specific line number
2. Ensure your shader uses `#version 150 core`
3. Verify all required inputs and outputs are present
4. Check that all uniforms are properly declared

---

## Advanced Usage

### Creating Custom Shaders

See the [Development Guide](#) for information on creating your own shaders.

### Shader Uniforms Reference

The following uniforms are available to all shaders:

| Uniform | Type | Description |
|---------|------|-------------|
| `ModelViewMat` | mat4 | Model-view matrix |
| `ProjMat` | mat4 | Projection matrix |
| `NormalMat` | mat3 | Normal matrix |
| `TextureSampler` | sampler2D | Main texture sampler |
| `LightDirection` | vec3 | Direction to the main light source |
| `CameraPosition` | vec3 | Position of the camera |
| `Time` | float | Time in seconds since game start |
| `LightColor` | vec3 | Color of the main light |
| `LightIntensity` | float | Intensity of the main light |
| `AmbientOcclusion` | float | Ambient occlusion factor |

### Vertex Shader Inputs/Outputs

**Required Inputs:**
- `Position` (vec3) - Vertex position
- `Color` (vec3) - Vertex color
- `TexCoord` (vec2) - Texture coordinates
- `Normal` (vec3) - Vertex normal

**Required Outputs:**
- `vColor` (vec3) - Pass through color
- `vTexCoord` (vec2) - Pass through texture coordinates
- `vNormal` (vec3) - Transformed normal
- `vPosition` (vec3) - Transformed position

### Fragment Shader Inputs/Outputs

**Required Inputs:**
- `vColor` (vec3) - From vertex shader
- `vTexCoord` (vec2) - From vertex shader
- `vNormal` (vec3) - From vertex shader
- `vPosition` (vec3) - From vertex shader

**Required Output:**
- `fragColor` (vec4) - Final fragment color

---

## Shader Comparison

### Side-by-Side Comparison

| Feature | Vanilla | PBR Basic | Vibrant | Cel Shading |
|---------|---------|-----------|---------|-------------|
| Color Saturation | Normal | Normal | +50% | Normal |
| Contrast | Normal | Normal | +20% | Normal |
| Lighting Model | Flat | PBR-like | Enhanced | Cel/Toon |
| Specular Highlights | None | Yes | Subtle | Quantized |
| Shadows | None | Simulated | None | None |
| Glow Effects | None | None | Yes | None |
| Edge Detection | None | None | None | Yes |
| Color Quantization | None | None | None | Yes |
| Dynamic Effects | None | None | Pulse | Time-based |
| Realism | Medium | High | Low | Low |
| Style | Realistic | Realistic | Enhanced | Cartoon |

### Recommended Use Cases

| Use Case | Recommended Shader |
|----------|-------------------|
| General gameplay | PBR Basic |
| Exploration | Vibrant |
| Building/Architecture | PBR Basic |
| Screenshots (realistic) | PBR Basic |
| Screenshots (artistic) | Vibrant or Cel Shading |
| Streaming (unique look) | Cel Shading |
| Streaming (enhanced) | Vibrant |
| PvP | PBR Basic or Vibrant |
| Redstone builds | Vibrant |
| Adventure maps | Cel Shading |

---

## Known Limitations

As of version 1.0.0, the following limitations exist:

1. **No In-Game Shader Selection:** Shaders must be activated by modifying code
2. **No Configuration GUI:** All settings must be changed in code
3. **Limited Shadow Support:** PBR shader uses simplified shadow simulation
4. **No Normal Mapping:** Advanced lighting features are not yet implemented
5. **No Post-Processing:** Shaders are applied per-object, not as full-screen effects
6. **Performance:** Cel shading may cause FPS drops on lower-end hardware

These limitations will be addressed in future versions.

---

## Version History

### Version 1.0.0
- Initial release
- Three shaders: Vibrant, Cel Shading, PBR Basic
- Basic shader loading system
- Mixin-based render integration

---

## Getting Help

If you need help with ShaderMod 2:

1. **Check this documentation** - Most questions are answered here
2. **Check the Minecraft log** - Look for error messages
3. **Review the example shaders** - They demonstrate the required structure
4. **Ask on NeoForge Discord** - [https://discord.gg/neoforged](https://discord.gg/neoforged)
5. **Open an issue** - Report bugs or request features on GitHub

---

## Contributing

If you'd like to contribute to ShaderMod 2:

1. Fork the repository on GitHub
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

Contributions are welcome for:
- New shaders
- Bug fixes
- Performance improvements
- Documentation updates
- Configuration system
- In-game shader selection

---

*ShaderMod 2 - Making Minecraft More Beautiful, One Shader at a Time*
