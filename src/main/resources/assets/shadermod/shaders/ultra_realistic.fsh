#version 150 core

// Ultra-Realistic PBR Shader for Minecraft
// Combines: Physically Based Rendering, Screen Space Reflections,
// Ambient Occlusion, Fresnel effects, and advanced lighting

in vec3 vColor;
in vec2 vTexCoord;
in vec3 vNormal;
in vec3 vPosition;
in vec3 vWorldPosition;
in vec3 vViewDir;
in vec3 vLightDir;

out vec4 fragColor;

// Uniforms
uniform sampler2D TextureSampler;
uniform sampler2D NormalMap;
uniform sampler2D SpecularMap;
uniform sampler2D DepthMap;
uniform sampler2D ShadowMap;

// Light parameters
uniform vec3 LightDirection;
uniform vec3 LightColor;
uniform float LightIntensity;
uniform vec3 CameraPosition;

// Material parameters
uniform vec3 Albedo;
uniform float Metallic;
uniform float Roughness;
uniform float AmbientOcclusion;

// Environment
uniform vec3 SkyColor;
uniform vec3 GroundColor;
uniform vec3 AmbientLight;
uniform float Time;

// Screen dimensions
uniform vec2 ScreenSize;

// ===================================================================
// PHYSICALLY BASED RENDERING FUNCTIONS
// ===================================================================

// Constants
const float PI = 3.14159265359;
const float INV_PI = 0.31830988618;
const float INV_TWOPI = 0.15915494309;

// Fresnel-Schlick approximation
vec3 fresnelSchlick(float cosTheta, vec3 F0) {
    return F0 + (1.0 - F0) * pow(1.0 - cosTheta, 5.0);
}

// Normal Distribution Function (Trowbridge-Reitz GGX)
float DistributionGGX(vec3 N, vec3 H, float roughness) {
    float a = roughness * roughness;
    float a2 = a * a;
    float NdotH = max(dot(N, H), 0.0);
    float NdotH2 = NdotH * NdotH;
    
    float nom = a2;
    float denom = (NdotH2 * (a2 - 1.0) + 1.0);
    denom = PI * denom * denom;
    
    return nom / denom;
}

// Geometry Function (Schlick GGX)
float GeometrySchlickGGX(float NdotV, float roughness) {
    float r = (roughness + 1.0);
    float k = (r * r) / 8.0;
    
    float nom = NdotV;
    float denom = NdotV * (1.0 - k) + k;
    
    return nom / denom;
}

// Smith's method
float GeometrySmith(vec3 N, vec3 V, vec3 L, float roughness) {
    float NdotV = max(dot(N, V), 0.0);
    float NdotL = max(dot(N, L), 0.0);
    float ggx2 = GeometrySchlickGGX(NdotV, roughness);
    float ggx1 = GeometrySchlickGGX(NdotL, roughness);
    
    return ggx1 * ggx2;
}

// Fresnel Function
vec3 fresnel(vec3 F0, float cosTheta) {
    return F0 + (max(vec3(1.0 - roughness), F0) - F0) * pow(1.0 - cosTheta, 5.0);
}

// ===================================================================
// LIGHTING CALCULATIONS
// ===================================================================

// Calculate diffuse component using Lambertian model
vec3 calculateDiffuse(vec3 N, vec3 L, vec3 albedo) {
    float NdotL = max(dot(N, L), 0.0);
    return albedo * NdotL * INV_PI;
}

// Calculate specular component using Cook-Torrance BRDF
vec3 calculateSpecular(vec3 N, vec3 V, vec3 L, vec3 F0, float roughness) {
    vec3 H = normalize(V + L);
    float NdotV = max(dot(N, V), 0.0);
    float NdotL = max(dot(N, L), 0.0);
    
    if (NdotL > 0.0) {
        vec3 F = fresnelSchlick(max(dot(H, V), 0.0), F0);
        float D = DistributionGGX(N, H, roughness);
        float G = GeometrySmith(N, V, L, roughness);
        
        vec3 numerator = D * F * G;
        float denominator = 4.0 * max(NdotV, 0.001) * max(NdotL, 0.001) + 0.001;
        return numerator / denominator;
    }
    return vec3(0.0);
}

// Calculate direct lighting from main light source
vec3 calculateDirectLighting(vec3 N, vec3 V, vec3 L, vec3 albedo, float metallic, float roughness, float shadowFactor) {
    vec3 F0 = vec3(0.04);
    F0 = mix(F0, albedo, metallic);
    
    vec3 diffuse = calculateDiffuse(N, L, albedo);
    vec3 specular = calculateSpecular(N, V, L, F0, roughness);
    
    vec3 Lo = (diffuse + specular) * LightColor * LightIntensity * shadowFactor;
    return Lo;
}

// ===================================================================
// ENVIRONMENT LIGHTING
// ===================================================================

// Simple environment lighting approximation
vec3 calculateEnvironmentLighting(vec3 N, vec3 V, vec3 albedo, float metallic, float roughness) {
    // Sky and ground ambient
    vec3 skyAmbient = SkyColor * 0.5;
    vec3 groundAmbient = GroundColor * 0.3;
    
    // Simple hemisphere lighting based on normal
    float skyFactor = 0.5 * (1.0 + N.y);
    vec3 ambient = mix(groundAmbient, skyAmbient, skyFactor) * AmbientLight;
    
    // Add ambient occlusion
    ambient *= AmbientOcclusion;
    
    // For metallic surfaces, reduce diffuse ambient
    if (metallic > 0.5) {
        ambient *= (1.0 - metallic * 0.7);
    }
    
    return ambient * albedo;
}

// ===================================================================
// SCREEN SPACE EFFECTS
// ===================================================================

// Sample depth buffer for screen space effects
float sampleDepth(vec2 texCoord) {
    // In a real implementation, this would sample the depth texture
    // For this shader, we'll use a simplified approximation
    return texture(DepthMap, texCoord).r;
}

// Calculate screen space reflection (simplified)
vec3 calculateScreenSpaceReflection(vec3 V, vec3 N, vec2 texCoord, float roughness) {
    vec3 R = reflect(-V, N);
    
    // Convert reflection vector to screen space
    vec2 refTexCoord = texCoord + R.xy * 0.01 * (1.0 - roughness);
    
    // Sample the scene at the reflection point
    vec3 reflectionColor = texture(TextureSampler, refTexCoord).rgb;
    
    // Blur based on roughness
    if (roughness > 0.3) {
        float blurAmount = roughness * 0.02;
        reflectionColor += texture(TextureSampler, texCoord + vec2(blurAmount, 0.0)).rgb;
        reflectionColor += texture(TextureSampler, texCoord + vec2(-blurAmount, 0.0)).rgb;
        reflectionColor += texture(TextureSampler, texCoord + vec2(0.0, blurAmount)).rgb;
        reflectionColor += texture(TextureSampler, texCoord + vec2(0.0, -blurAmount)).rgb;
        reflectionColor /= 5.0;
    }
    
    return reflectionColor * 0.5;
}

// ===================================================================
// SHADOW CALCULATION
// ===================================================================

// Calculate shadow factor (simplified for this implementation)
float calculateShadowFactor(vec3 position, vec3 lightDir, vec3 normal) {
    // Basic shadow calculation using ambient occlusion as proxy
    float shadow = AmbientOcclusion;
    
    // Add some variation based on position and time for dynamic shadows
    float shadowVariation = sin(dot(position, vec3(0.1, 0.2, 0.3)) + Time * 0.5) * 0.1 + 0.9;
    
    // Soft shadows based on light angle
    float lightAngle = max(dot(normal, lightDir), 0.0);
    float softShadow = smoothstep(0.0, 0.5, lightAngle);
    
    return shadow * shadowVariation * softShadow;
}

// ===================================================================
// SUB-SURFACE SCATTERING
// ===================================================================

// Simple sub-surface scattering approximation
vec3 calculateSubSurfaceScattering(vec3 albedo, vec3 N, vec3 V, vec3 L, float thickness) {
    // Only apply to non-metallic materials
    if (thickness <= 0.0) return vec3(0.0);
    
    float NdotL = max(dot(N, L), 0.0);
    float NdotV = max(dot(N, V), 0.0);
    
    // Simple approximation
    vec3 sssColor = albedo * 0.5;
    float sssFactor = (1.0 - NdotL) * (1.0 - NdotV) * thickness * 2.0;
    
    return sssColor * sssFactor;
}

// ===================================================================
// ATMOSPHERIC EFFECTS
// ===================================================================

// Calculate atmospheric scattering (simplified)
vec3 calculateAtmosphericScattering(vec3 V, vec3 worldPos, float distance) {
    // Distance fog
    float fogFactor = exp(-distance * 0.001);
    vec3 fogColor = mix(SkyColor, vec3(0.7, 0.8, 1.0), 0.5);
    
    // Height-based fog
    float heightFog = smoothstep(60.0, 120.0, worldPos.y) * 0.3;
    fogColor = mix(fogColor, vec3(0.8, 0.9, 1.0), heightFog);
    
    return fogColor * (1.0 - fogFactor);
}

// ===================================================================
// MAIN SHADER
// ===================================================================

void main() {
    // Sample base texture
    vec4 texColor = texture(TextureSampler, vTexCoord);
    
    // Material properties (simplified for Minecraft)
    vec3 albedo = texColor.rgb * vColor;
    float metallic = 0.0;
    float roughness = 0.5;
    float thickness = 0.1;
    
    // Detect metallic blocks (gold, iron, diamond, etc.)
    if (albedo.r > 0.8 && albedo.g > 0.7 && albedo.b < 0.5) {
        metallic = 0.8;
        roughness = 0.1;
    } else if (albedo.r > 0.7 && albedo.g > 0.7 && albedo.b > 0.7) {
        metallic = 0.5;
        roughness = 0.3;
    }
    
    // Normalize inputs
    vec3 N = normalize(vNormal);
    vec3 V = normalize(vViewDir);
    vec3 L = normalize(vLightDir);
    
    // Calculate shadow factor
    float shadowFactor = calculateShadowFactor(vWorldPosition, L, N);
    
    // Calculate direct lighting
    vec3 directLighting = calculateDirectLighting(N, V, L, albedo, metallic, roughness, shadowFactor);
    
    // Calculate environment lighting
    vec3 environmentLighting = calculateEnvironmentLighting(N, V, albedo, metallic, roughness);
    
    // Calculate screen space reflection
    vec3 reflection = calculateScreenSpaceReflection(V, N, vTexCoord, roughness);
    
    // Calculate sub-surface scattering
    vec3 sss = calculateSubSurfaceScattering(albedo, N, V, L, thickness);
    
    // Calculate atmospheric effects
    float distance = length(vWorldPosition - CameraPosition);
    vec3 atmospheric = calculateAtmosphericScattering(V, vWorldPosition, distance);
    
    // Combine all lighting components
    vec3 color = vec3(0.0);
    
    // Diffuse and specular from direct light
    color += directLighting;
    
    // Environment lighting (IBL approximation)
    color += environmentLighting;
    
    // Add reflections
    color += reflection * (1.0 - roughness * 0.7);
    
    // Add sub-surface scattering
    color += sss;
    
    // Apply atmospheric effects
    color = mix(color, atmospheric, 1.0 - exp(-distance * 0.002));
    
    // Fresnel effect for edge glow
    vec3 F0 = vec3(0.04);
    F0 = mix(F0, albedo, metallic);
    vec3 fresnelEffect = fresnelSchlick(max(dot(N, V), 0.0), F0) * 0.1;
    color += fresnelEffect;
    
    // Gamma correction
    color = pow(color, vec3(1.0 / 2.2));
    
    // Add subtle time-based variation for dynamic feel
    float timeFactor = sin(Time * 0.3) * 0.02 + 0.98;
    color *= timeFactor;
    
    // Clamp and output
    fragColor = vec4(clamp(color, 0.0, 1.0), texColor.a);
}
