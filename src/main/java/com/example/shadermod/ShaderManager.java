package com.example.shadermod;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL20;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@OnlyIn(Dist.CLIENT)
public class ShaderManager {
    
    private static final String SHADER_PATH = "shadermod/shaders";
    private final Map<String, ShaderProgram> shaders = new HashMap<>();
    private ResourceManager resourceManager;
    
    public void initialize() {
        this.resourceManager = Minecraft.getInstance().getResourceManager();
        
        // Load built-in shaders
        loadShader("vibrant");
        loadShader("cel_shading");
        loadShader("pbr_basic");
    }
    
    public void loadShader(String name) {
        try {
            ResourceLocation vertexShaderLoc = new ResourceLocation(ShaderMod.MODID, SHADER_PATH + "/" + name + ".vsh");
            ResourceLocation fragmentShaderLoc = new ResourceLocation(ShaderMod.MODID, SHADER_PATH + "/" + name + ".fsh");
            
            String vertexShader = loadShaderSource(vertexShaderLoc);
            String fragmentShader = loadShaderSource(fragmentShaderLoc);
            
            if (vertexShader != null && fragmentShader != null) {
                ShaderProgram program = new ShaderProgram(name, vertexShader, fragmentShader);
                shaders.put(name, program);
                System.out.println("[ShaderMod] Loaded shader: " + name);
            } else {
                System.err.println("[ShaderMod] Failed to load shader sources for: " + name);
            }
        } catch (Exception e) {
            System.err.println("[ShaderMod] Error loading shader " + name + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private String loadShaderSource(ResourceLocation location) {
        try {
            var resource = resourceManager.getResource(location);
            try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.open(), StandardCharsets.UTF_8)
            )) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        } catch (IOException e) {
            System.err.println("[ShaderMod] Failed to read shader file " + location + ": " + e.getMessage());
            return null;
        }
    }
    
    public ShaderProgram getShader(String name) {
        return shaders.get(name);
    }
    
    public boolean hasShader(String name) {
        return shaders.containsKey(name);
    }
    
    public void applyShader(String name) {
        ShaderProgram program = shaders.get(name);
        if (program != null) {
            program.use();
        }
    }
    
    public void releaseShader() {
        GlStateManager._setShader(() -> 0);
    }
    
    public void renderWithShader(String shaderName, Runnable renderTask) {
        ShaderProgram program = shaders.get(shaderName);
        if (program != null) {
            program.use();
            renderTask.run();
            releaseShader();
        } else {
            renderTask.run();
        }
    }
    
    public void cleanup() {
        shaders.values().forEach(ShaderProgram::cleanup);
        shaders.clear();
    }
    
    // Inner class representing a compiled shader program
    public static class ShaderProgram {
        private final String name;
        private final int programId;
        private final int vertexShaderId;
        private final int fragmentShaderId;
        
        public ShaderProgram(String name, String vertexSource, String fragmentSource) {
            this.name = name;
            this.vertexShaderId = compileShader(vertexSource, GL20.GL_VERTEX_SHADER);
            this.fragmentShaderId = compileShader(fragmentSource, GL20.GL_FRAGMENT_SHADER);
            this.programId = linkProgram(this.vertexShaderId, this.fragmentShaderId);
        }
        
        private int compileShader(String source, int type) {
            int shaderId = GL20.glCreateShader(type);
            GL20.glShaderSource(shaderId, source);
            GL20.glCompileShader(shaderId);
            
            if (GL20.glGetShaderi(shaderId, GL20.GL_COMPILE_STATUS) == 0) {
                String infoLog = GL20.glGetShaderInfoLog(shaderId);
                System.err.println("[ShaderMod] Shader compilation error for " + name + ": " + infoLog);
                GL20.glDeleteShader(shaderId);
                throw new RuntimeException("Failed to compile shader: " + infoLog);
            }
            
            return shaderId;
        }
        
        private int linkProgram(int vertexShaderId, int fragmentShaderId) {
            int programId = GL20.glCreateProgram();
            GL20.glAttachShader(programId, vertexShaderId);
            GL20.glAttachShader(programId, fragmentShaderId);
            GL20.glLinkProgram(programId);
            
            if (GL20.glGetProgrami(programId, GL20.GL_LINK_STATUS) == 0) {
                String infoLog = GL20.glGetProgramInfoLog(programId);
                System.err.println("[ShaderMod] Shader program linking error for " + name + ": " + infoLog);
                GL20.glDeleteProgram(programId);
                throw new RuntimeException("Failed to link shader program: " + infoLog);
            }
            
            return programId;
        }
        
        public void use() {
            GL20.glUseProgram(programId);
        }
        
        public void cleanup() {
            if (programId != 0) {
                GL20.glDeleteProgram(programId);
            }
            if (vertexShaderId != 0) {
                GL20.glDeleteShader(vertexShaderId);
            }
            if (fragmentShaderId != 0) {
                GL20.glDeleteShader(fragmentShaderId);
            }
        }
        
        public String getName() {
            return name;
        }
        
        public int getProgramId() {
            return programId;
        }
    }
}
