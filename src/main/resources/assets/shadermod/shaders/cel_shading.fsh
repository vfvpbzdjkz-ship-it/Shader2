#version 150 core

in vec3 vColor;
in vec2 vTexCoord;
in vec3 vNormal;
in vec3 vPosition;

out vec4 fragColor;

uniform sampler2D TextureSampler;
uniform vec3 LightDirection;
uniform vec3 CameraPosition;
uniform float Time;

// Cel/Toon shading shader
// Creates a stylized, non-realistic rendering effect popular in anime and cartoon styles

// Number of shade levels for cel shading
const int SHADING_LEVELS = 4;

// Light calculation with cel shading
float calculateCelLight(vec3 normal, vec3 lightDir) {
    float dotProduct = max(dot(normal, lightDir), 0.0);
    
    // Quantize the dot product into discrete levels
    float levelSize = 1.0 / float(SHADING_LEVELS);
    float quantized = floor(dotProduct / levelSize) * levelSize;
    
    // Add a small ramp for smoother transitions between levels
    float ramp = smoothstep(quantized, quantized + levelSize * 0.5, dotProduct);
    
    return quantized + ramp * levelSize * 0.3;
}

// Outline effect using normal and depth
vec3 calculateOutline(vec3 normal, vec2 texCoord, vec3 position) {
    // Sample neighboring pixels to detect edges
    const float offset = 0.005;
    
    vec2 texSize = vec2(1.0 / textureSize(TextureSampler, 0));
    
    vec3 normalRight = texture(TextureSampler, texCoord + vec2(offset, 0.0)).rgb;
    vec3 normalLeft = texture(TextureSampler, texCoord + vec2(-offset, 0.0)).rgb;
    vec3 normalUp = texture(TextureSampler, texCoord + vec2(0.0, offset)).rgb;
    vec3 normalDown = texture(TextureSampler, texCoord + vec2(0.0, -offset)).rgb;
    
    // Calculate differences
    float diffRight = abs(dot(normal, normalize(normalRight)));
    float diffLeft = abs(dot(normal, normalize(normalLeft)));
    float diffUp = abs(dot(normal, normalize(normalUp)));
    float diffDown = abs(dot(normal, normalize(normalDown)));
    
    // Combine differences
    float edge = 1.0 - max(max(diffRight, diffLeft), max(diffUp, diffDown));
    
    // Apply edge detection
    if (edge > 0.3) {
        return vec3(0.1, 0.1, 0.1); // Dark outline
    }
    
    return vec3(1.0);
}

// Specular highlight for cel shading
vec3 calculateCelSpecular(vec3 normal, vec3 lightDir, vec3 viewDir) {
    vec3 reflectDir = reflect(-lightDir, normal);
    float specular = max(dot(viewDir, reflectDir), 0.0);
    
    // Sharpen the specular highlight
    specular = pow(specular, 32.0);
    
    // Quantize specular into 2 levels
    if (specular > 0.5) {
        return vec3(1.0, 1.0, 1.0) * 0.8;
    } else if (specular > 0.1) {
        return vec3(1.0, 1.0, 1.0) * 0.4;
    }
    
    return vec3(0.0);
}

void main() {
    // Sample texture
    vec4 texColor = texture(TextureSampler, vTexCoord);
    
    // Calculate lighting
    vec3 normal = normalize(vNormal);
    vec3 lightDir = normalize(LightDirection);
    vec3 viewDir = normalize(CameraPosition - vPosition);
    
    // Cel shading
    float lightIntensity = calculateCelLight(normal, lightDir);
    
    // Base color with cel shading
    vec3 baseColor = texColor.rgb * vColor * lightIntensity;
    
    // Add specular highlight
    vec3 specular = calculateCelSpecular(normal, lightDir, viewDir);
    
    // Apply outline effect
    vec3 outline = calculateOutline(normal, vTexCoord, vPosition);
    
    // Combine all effects
    vec3 finalColor = mix(baseColor, outline, 0.0); // Outline is separate pass in real implementation
    finalColor += specular;
    
    // Add subtle time-based effect for animation
    float timeFactor = sin(Time * 2.0) * 0.02 + 0.98;
    finalColor *= timeFactor;
    
    // Posterize colors for more cartoon-like appearance
    finalColor = floor(finalColor * 8.0) / 8.0;
    
    fragColor = vec4(finalColor, texColor.a);
}
