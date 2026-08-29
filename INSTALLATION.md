# ShaderMod 2 - Installation & Usage Guide

This guide will walk you through the complete process of getting ShaderMod 2 installed, built, and working in your Minecraft environment.

---

## Prerequisites

Before you begin, ensure you have the following installed:

### Required Software
- **Java JDK 17** - Required for NeoForge mod development
  - Download: [Oracle JDK 17](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html) or [Adoptium Temurin 17](https://adoptium.net/temurin/releases/?version=17)
  - Verify: `java -version` should show version 17
  
- **Git** - For version control
  - Download: [Git](https://git-scm.com/downloads)
  - Verify: `git --version`

- **Gradle** - Build system (will be installed automatically by the Gradle wrapper)

### System Requirements
- Windows 10/11, macOS 10.15+, or Linux (most modern distributions)
- Minimum 8GB RAM (16GB recommended for development)
- GPU that supports OpenGL 4.5+ (for shader rendering)

---

## Step 1: Clone the Repository

Open a terminal or command prompt and run:

```bash
# Clone the repository
git clone https://github.com/vfvpbzdjkz-ship-it/Shader2.git

# Navigate to the project directory
cd Shader2
```

If you already have the repository, make sure to pull the latest changes:

```bash
git pull origin main
```

---

## Step 2: Set Up the Development Environment

### Option A: Using IntelliJ IDEA (Recommended)

1. **Download IntelliJ IDEA Community Edition** (free)
   - [Download IntelliJ IDEA](https://www.jetbrains.com/idea/download/)

2. **Open the Project**
   - Launch IntelliJ IDEA
   - Click "Open" and select the `Shader2` folder
   - When prompted, select "Trust Project"

3. **Configure Gradle**
   - IntelliJ will automatically detect the Gradle project
   - Wait for Gradle to sync (this may take several minutes on first run)
   - In the Gradle panel (usually on the right side), click the refresh button

4. **Set Up Run Configurations**
   - The NeoForge Gradle plugin automatically creates run configurations
   - You should see "client" and "server" configurations in the run dropdown

### Option B: Using Eclipse

1. **Generate Eclipse project files**
   ```bash
   ./gradlew eclipse
   ```

2. **Import into Eclipse**
   - Open Eclipse
   - File > Import > Existing Projects into Workspace
   - Select the `Shader2` folder

### Option C: Command Line Only

You can build and run from the command line without an IDE.

---

## Step 3: Build the Mod

### First-Time Setup

The first build will take longer as Gradle downloads all dependencies:

```bash
# On Windows
./gradlew build

# On macOS/Linux
./gradlew build
```

This command will:
- Download NeoForge dependencies
- Download Minecraft assets
- Compile the Java code
- Process resources
- Create the mod JAR file

### Subsequent Builds

After the first build, subsequent builds will be faster:

```bash
# Build the mod
./gradlew build

# Clean build (if you encounter issues)
./gradlew clean build
```

### Build Output Location

The built mod JAR will be located at:
```
build/libs/shadermod-1.0.0.jar
```

---

## Step 4: Install the Mod for Testing

### Method A: Direct Installation (For Testing)

1. **Locate your Minecraft directory**
   - Windows: `%appdata%\.minecraft`
   - macOS: `~/Library/Application Support/minecraft`
   - Linux: `~/.minecraft`

2. **Create the mods folder** (if it doesn't exist)
   ```bash
   mkdir -p ~/.minecraft/mods
   ```

3. **Copy the mod JAR**
   ```bash
   # On Windows
   copy build\libs\shadermod-1.0.0.jar %appdata%\.minecraft\mods\
   
   # On macOS/Linux
   cp build/libs/shadermod-1.0.0.jar ~/.minecraft/mods/
   ```

### Method B: Using Gradle Run Configurations (Easier for Development)

The NeoForge Gradle plugin provides built-in run configurations:

1. **Run the Minecraft client with the mod**
   ```bash
   ./gradlew runClient
   ```

2. **Run the Minecraft server with the mod**
   ```bash
   ./gradlew runServer
   ```

This automatically:
- Builds the mod
- Sets up a temporary Minecraft instance
- Includes the mod in the classpath
- Launches the game

### Method C: Using IntelliJ Run Configurations

1. Select "client" from the run configuration dropdown
2. Click the green play button
3. This will launch Minecraft with your mod loaded

---

## Step 5: Verify the Mod is Working

### Check the Logs

When you launch Minecraft with the mod installed, you should see messages like:

```
[ShaderMod] Common setup complete
[ShaderMod] Client setup complete - shaders loaded
[ShaderMod] Loaded shader: vibrant
[ShaderMod] Loaded shader: cel_shading
[ShaderMod] Loaded shader: pbr_basic
```

### In-Game Verification

1. **Launch Minecraft** with the NeoForge profile
2. **Check the Mods menu** - You should see "ShaderMod 2" listed
3. **Look for console output** in the Minecraft log (F3 + L to open latest.log)

### Debugging Common Issues

**Problem: Mod doesn't appear in the mods list**
- Ensure the JAR file is in the correct mods folder
- Check that you're using the NeoForge profile (not vanilla)
- Verify the mod is built for the correct Minecraft version (1.20.4)

**Problem: Game crashes on startup**
- Check the crash log in `.minecraft/crash-reports/`
- Look for errors related to ShaderMod
- Ensure you have Java 17 installed

**Problem: Shaders not loading**
- Check the Minecraft log for shader loading errors
- Verify the shader files are in the correct location in the JAR

---

## Step 6: Using the Shaders

### Available Shaders

ShaderMod 2 includes three shaders:

1. **vibrant** - Enhances colors with increased saturation and subtle glow
2. **cel_shading** - Creates a cartoon/anime-style rendering effect
3. **pbr_basic** - Provides standard PBR lighting with shadows

### Shader Selection

Currently, the shaders are loaded but not automatically applied. To use them, you have several options:

#### Option A: Using Commands (Future Implementation)

The mod will be extended with commands like:
```
/shader list          - List available shaders
/shader vibrant      - Apply the vibrant shader
/shader cel_shading  - Apply the cel shading shader
/shader pbr_basic    - Apply the PBR shader
/shader none         - Disable shaders
```

#### Option B: Modifying the Code

To test shaders immediately, you can modify the `ShaderRendererMixin.java` file:

```java
// In ShaderRendererMixin.java
@Inject(
    method = "render",
    at = @At("HEAD")
)
private void onRender(PoseStack poseStack, float partialTicks, long nanoTime, boolean renderLevel, CallbackInfo ci) {
    ShaderMod mod = ShaderMod.getInstance();
    if (mod != null && mod.getShaderManager() != null) {
        ShaderManager manager = mod.getShaderManager();
        
        // Apply a specific shader
        if (manager.hasShader("vibrant")) {
            manager.applyShader("vibrant");
        }
        // Or cycle through shaders
        // manager.applyShader("cel_shading");
        // manager.applyShader("pbr_basic");
    }
}
```

#### Option C: Using Mixin Injection Points

The `ShaderRendererMixin` currently hooks into the render pipeline. You can add more injection points to apply shaders at different stages of rendering.

---

## Step 7: Development Workflow

### Making Changes

1. **Modify shader files** in `src/main/resources/assets/shadermod/shaders/`
2. **Rebuild the mod**
   ```bash
   ./gradlew build
   ```
3. **Test in-game** using one of the run methods above

### Hot-Reloading (Limited)

For shader changes only:
1. Modify the .fsh or .vsh files
2. Rebuild: `./gradlew build`
3. Copy the new JAR to your mods folder
4. Restart Minecraft

Note: Java code changes require a full rebuild and Minecraft restart.

### Debugging

**Viewing Logs:**
- Minecraft logs: `.minecraft/logs/latest.log`
- Gradle logs: Check the terminal output

**Enabling Debug Output:**
Add this to `ShaderManager.java`:
```java
System.out.println("[DEBUG] Loading shader: " + name);
```

---

## Step 8: Creating Custom Shaders

### Shader File Structure

Each shader consists of two files:
- `{name}.vsh` - Vertex shader
- `{name}.fsh` - Fragment shader

### Adding a New Shader

1. **Create the shader files** in `src/main/resources/assets/shadermod/shaders/`
   - `my_shader.vsh`
   - `my_shader.fsh`

2. **Register the shader** in `ShaderManager.java`:
   ```java
   loadShader("my_shader");
   ```

3. **Rebuild and test**

### Shader Development Tips

**Vertex Shader Requirements:**
- Must output: `vColor`, `vTexCoord`, `vNormal`, `vPosition`
- Must use: `ModelViewMat`, `ProjMat`, `NormalMat` uniforms

**Fragment Shader Requirements:**
- Must input: `vColor`, `vTexCoord`, `vNormal`, `vPosition`
- Must use: `TextureSampler` uniform
- Can use: `LightDirection`, `CameraPosition`, `Time` uniforms

**Example Minimal Shader:**

`minimal.vsh`:
```glsl
#version 150 core
in vec3 Position;
in vec3 Color;
in vec2 TexCoord;
in vec3 Normal;
out vec3 vColor;
out vec2 vTexCoord;
out vec3 vNormal;
out vec3 vPosition;
uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform mat3 NormalMat;
void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    vColor = Color;
    vTexCoord = TexCoord;
    vNormal = normalize(NormalMat * Normal);
    vPosition = Position;
}
```

`minimal.fsh`:
```glsl
#version 150 core
in vec3 vColor;
in vec2 vTexCoord;
out vec4 fragColor;
uniform sampler2D TextureSampler;
void main() {
    fragColor = texture(TextureSampler, vTexCoord) * vec4(vColor, 1.0);
}
```

---

## Step 9: Deploying the Mod

### For Personal Use

Simply copy the JAR file to your mods folder as described above.

### For Distribution

1. **Build the mod**
   ```bash
   ./gradlew build
   ```

2. **The JAR file** will be at `build/libs/shadermod-1.0.0.jar`

3. **Share the JAR** with others (they need NeoForge 20.4.167 for MC 1.20.4)

### Version Management

To update the version:
1. Edit `build.gradle`:
   ```gradle
   version = '1.0.1'
   ```
2. Update `mods.toml`:
   ```toml
   version="1.0.1"
   ```
3. Commit and tag:
   ```bash
   git commit -am "Update to 1.0.1"
   git tag v1.0.1
   git push origin main --tags
   ```

---

## Troubleshooting

### Common Issues and Solutions

**Issue: `Could not find or load main class`**
- Solution: Ensure you're using Java 17, not an older version
- Run: `java -version` to check

**Issue: `Gradle build failed`**
- Solution: Try cleaning and rebuilding
  ```bash
  ./gradlew clean build
  ```
- If that fails, delete the `.gradle` folder and try again

**Issue: `Mod loading error`**
- Check that `mods.toml` has the correct mod ID
- Verify the mod ID matches in all Java files

**Issue: `Shader compilation error`**
- Check the Minecraft log for the specific GLSL error
- Ensure your shader uses `#version 150 core`
- Verify all required inputs/outputs are present

**Issue: `OpenGL errors`**
- Ensure your GPU drivers are up to date
- Try lowering graphics settings in Minecraft

### Getting Help

If you encounter issues:
1. Check the [NeoForge Discord](https://discord.gg/neoforged)
2. Look at the [NeoForge Documentation](https://neoforged.net/docs/)
3. Review the Minecraft log files

---

## Project Structure Reference

```
Shader2/
├── build.gradle              # Build configuration
├── settings.gradle           # Project settings
├── INSTALLATION.md           # This file
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/example/shadermod/
    │   │       ├── ShaderMod.java          # Main mod class
    │   │       ├── ShaderManager.java      # Shader management
    │   │       └── mixin/
    │   │           └── ShaderRendererMixin.java
    │   └── resources/
    │       ├── META-INF/
    │       │   └── mods.toml               # Mod metadata
    │       ├── assets/shadermod/
    │       │   ├── lang/en_us.json        # Localization
    │       │   └── shaders/
    │       │       ├── vibrant.vsh
    │       │       ├── vibrant.fsh
    │       │       ├── cel_shading.vsh
    │       │       ├── cel_shading.fsh
    │       │       ├── pbr_basic.vsh
    │       │       └── pbr_basic.fsh
    │       ├── shadermod.mixins.json       # Mixins config
    │       └── shadermod.refmap.json       # Mixin refmap
    └── test/                 # (Optional) Unit tests
```

---

## Next Steps

Now that you have ShaderMod 2 installed and working, consider:

1. **Adding a configuration GUI** using NeoForge's configuration system
2. **Implementing shader hot-reloading** for easier development
3. **Adding more shaders** (water distortion, night vision, etc.)
4. **Creating a shader selection menu** in-game
5. **Adding shader parameters** that can be adjusted in real-time

---

## License

This mod is provided as-is for educational and entertainment purposes. Feel free to modify and distribute according to the MIT license.

---

*Last updated: $(date)*
*ShaderMod 2 Version: 1.0.0*
*Minecraft Version: 1.20.4*
*NeoForge Version: 20.4.167*
