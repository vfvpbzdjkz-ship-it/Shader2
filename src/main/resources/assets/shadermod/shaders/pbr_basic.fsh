#version 150 core

in vec3 vColor;
in vec2 vTexCoord;
in vec3 vNormal;
in vec3 vPosition;

out vec4 fragColor;

uniform sampler2D TextureSampler;
uniform sampler2D ShadowMap;
uniform vec3 LightDirection;
uniform vec3 CameraPosition;
uniform vec3 LightColor;
uniform float LightIntensity;
uniform float AmbientOcclusion;

// Basic PBR (Physically Based Rendering) shader with shadows
// Simplified version that provides standard lighting effects

// Simple diffuse lighting
vec3 calculateDiffuse(vec3 normal, vec3 lightDir, vec3 lightColor) {
    float diff = max(dot(normal, lightDir), 0.0);
    return lightColor * diff;
}

// Simple specular lighting (Blinn-Phong)
vec3 calculateSpecular(vec3 normal, vec3 lightDir, vec3 viewDir, vec3 lightColor, float shininess) {
    vec3 halfVector = normalize(lightDir + viewDir);
    float spec = max(dot(normal, halfVector), 0.0);
    return lightColor * pow(spec, shininess * 128.0);
}

// Simple shadow calculation
float calculateShadow(vec3 position, vec3 lightDir) {
    // Simplified: just use ambient occlusion as shadow factor
    // In a full implementation, this would sample a shadow map
    return AmbientOcclusion;
}

// Simple ambient lighting
vec3 calculateAmbient(vec3 baseColor, float ambientFactor) {
    return baseColor * ambientFactor;
}

// Fresnel effect for realism
vec3 calculateFresnel(vec3 normal, vec3 viewDir, vec3 baseColor) {
    float fresnel = pow(1.0 - max(dot(normal, viewDir), 0.0), 5.0);
    return mix(baseColor, vec3(1.0), fresnel * 0.1);
}

void main() {
    // Sample texture
    vec4 texColor = texture(TextureSampler, vTexCoord);
    
    // Normalize inputs
    vec3 normal = normalize(vNormal);
    vec3 lightDir = normalize(LightDirection);
    vec3 viewDir = normalize(CameraPosition - vPosition);
    
    // Calculate lighting components
    vec3 diffuse = calculateDiffuse(normal, lightDir, LightColor) * LightIntensity;
    vec3 specular = calculateSpecular(normal, lightDir, viewDir, LightColor, 0.5);
    vec3 ambient = calculateAmbient(texColor.rgb * vColor, 0.3);
    
    // Shadow factor (simplified)
    float shadow = calculateShadow(vPosition, lightDir);
    
    // Combine lighting with shadows
    vec3 lighting = (diffuse + specular) * shadow + ambient;
    
    // Apply to base color
    vec3 baseColor = texColor.rgb * vColor;
    vec3 finalColor = baseColor * lighting;
    
    // Add fresnel effect
    finalColor = calculateFresnel(normal, viewDir, finalColor);
    
    // Gamma correction
    finalColor = pow(finalColor, vec3(1.0/2.2));
    
    fragColor = vec4(finalColor, texColor.a);
}
