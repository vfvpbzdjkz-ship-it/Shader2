#version 150 core

in vec3 vColor;
in vec2 vTexCoord;
in vec3 vNormal;
in vec3 vPosition;

out vec4 fragColor;

uniform sampler2D TextureSampler;
uniform vec3 LightDirection;
uniform float Time;

// Vibrant color enhancement shader
// Boosts saturation and adds subtle glow effects

vec3 vibrantColor(vec3 color) {
    // Increase saturation by 50%
    float brightness = dot(color, vec3(0.2126, 0.7152, 0.0722));
    vec3 saturated = mix(vec3(brightness), color, 1.5);
    
    // Add subtle glow based on brightness
    float glow = smoothstep(0.5, 1.0, brightness) * 0.3;
    vec3 glowColor = vec3(1.0, 0.8, 0.6) * glow;
    
    // Combine with original color
    return clamp(saturated + glowColor, 0.0, 1.0);
}

vec3 applyContrast(vec3 color, float contrast) {
    return clamp((color - 0.5) * contrast + 0.5, 0.0, 1.0);
}

vec3 applyVibrance(vec3 color, float vibrance) {
    float avg = (color.r + color.g + color.b) / 3.0;
    float mx = max(color.r, max(color.g, color.b));
    float amt = (mx - avg) * (-3.0 * vibrance);
    
    color.r = clamp(color.r + (color.r - avg) * amt, 0.0, 1.0);
    color.g = clamp(color.g + (color.g - avg) * amt, 0.0, 1.0);
    color.b = clamp(color.b + (color.b - avg) * amt, 0.0, 1.0);
    
    return color;
}

void main() {
    // Sample texture
    vec4 texColor = texture(TextureSampler, vTexCoord);
    
    // Apply lighting
    vec3 normal = normalize(vNormal);
    float light = max(dot(normal, normalize(LightDirection)), 0.2);
    vec3 baseColor = texColor.rgb * vColor * light;
    
    // Apply vibrant effects
    vec3 vibrant = vibrantColor(baseColor);
    vec3 contrasted = applyContrast(vibrant, 1.2);
    vec3 finalColor = applyVibrance(contrasted, 0.5);
    
    // Add subtle time-based pulse for dynamic feel
    float pulse = sin(Time * 0.5) * 0.05 + 0.95;
    finalColor *= pulse;
    
    fragColor = vec4(finalColor, texColor.a);
}
