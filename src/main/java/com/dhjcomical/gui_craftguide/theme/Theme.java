package com.dhjcomical.gui_craftguide.theme;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dhjcomical.gui_craftguide.texture.BasicTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
import com.dhjcomical.craftguide.CraftGuideLog;
import com.dhjcomical.gui_craftguide.minecraft.Image;
import com.dhjcomical.gui_craftguide.texture.DynamicTexture;
import com.dhjcomical.gui_craftguide.texture.SolidColorTexture;
import com.dhjcomical.gui_craftguide.texture.Texture;

public class Theme
{
	public enum SourceType
	{
		DIRECTORY,
		GENERATED,
		STREAM,
	}

	private static Texture errorTexture = new SolidColorTexture(255, 0, 255, 255);

	public String id;
	public String name;
	public String description;
	public File fileSource;
	public SourceType fileSourceType;
	public Map<String, List<Object[]>> images = new HashMap<>();
	public String loadError = null;
	public List<String> dependencies = new ArrayList<>();
	public Map<String, Texture> textures = new HashMap<>();

	private static Object[] errorImage = {"builtin", "error", null};

	public Theme(File location)
	{
		fileSource = location;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public void setID(String id)
	{
		this.id = id.toLowerCase().replaceAll("[^a-z0-9_-]", "");
	}

	public void setMetadata(String propertyName, String value)
	{
		if(propertyName.equalsIgnoreCase("id"))
		{
			setID(value);
		}
		else if(propertyName.equalsIgnoreCase("name"))
		{
			setName(value);
		}
		else if(propertyName.equalsIgnoreCase("description"))
		{
			setDescription(value);
		}
	}

	public void addImage(String id, List<String> sources)
	{
		List<Object[]> converted = new ArrayList<>(sources.size());

		for(String source: sources)
		{
			Object[] o = new Object[]{source.substring(0, source.indexOf(':')), source.substring(source.indexOf(':') + 1), fileSource};
			converted.add(o);
		}

		images.put(id, converted);
	}

	public void addDependency(String dependency)
	{
		dependencies.add(dependency);
	}

    public void generateTextures() {
        ThemeManager.debug("=== Starting Texture Generation for theme '" + this.id + "' ===");

        for (String imageID : this.images.keySet()) {
            ThemeManager.debug("  Processing source image definition: '" + imageID + "'");
            BufferedImage bufferedImage = null;

            for (Object[] imageFormat : this.images.get(imageID)) {
                bufferedImage = loadImageToRam(imageFormat);
                if (bufferedImage != null) {
                    ThemeManager.debug("    -> SUCCESS: Loaded " + imageID + " (" + bufferedImage.getWidth() + "x" + bufferedImage.getHeight() + ")");

                    try {
                        int glTextureId = TextureUtil.glGenTextures();
                        if (glTextureId == 0) {
                            ThemeManager.debug("    -> CRITICAL ERROR: glGenTextures() returned 0!");
                            continue;
                        }
                        TextureUtil.uploadTextureImageAllocate(glTextureId, bufferedImage, false, false);
                        ThemeManager.debug("    -> UPLOADED to GPU. OpenGL ID: " + glTextureId);

                        BasicTexture gpuTexture = new BasicTexture(bufferedImage.getWidth(), bufferedImage.getHeight(), glTextureId);
                        DynamicTexture.instance(imageID, gpuTexture);

                    } catch (Exception e) {
                        ThemeManager.debug("    -> CRITICAL ERROR during GPU upload for '" + imageID + "'");
                        CraftGuideLog.log(e);
                    }
                    break;
                }
            }
            if (bufferedImage == null) {
                ThemeManager.debug("  -> ERROR: Failed to load any source for image '" + imageID + "'");
            }
        }
        ThemeManager.debug("  Finalizing all texture definitions...");
        for (Map.Entry<String, Texture> entry : this.textures.entrySet()) {
            String textureId = entry.getKey();
            Texture textureObject = entry.getValue();
            DynamicTexture.instance(textureId, textureObject);
            ThemeManager.debug("    -> Cached definition for '" + textureId + "' (" + textureObject.getClass().getSimpleName() + ")");
        }

        ThemeManager.debug("=== Finished Texture Generation ===");
    }

	private Texture loadImage(Object[] imageFormat)
	{
		String sourceType = (String)imageFormat[0];
		String source = (String)imageFormat[1];

		ThemeManager.debug("      Loading " + sourceType + " image '" + source + "'");

		if(sourceType.equalsIgnoreCase("builtin"))
		{
			if(source.equalsIgnoreCase("error"))
			{
				return errorTexture;
			}
		}
		else if(sourceType.equalsIgnoreCase("file-jar") || sourceType.equalsIgnoreCase("resource"))
		{
			try
			{
				if(Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation(source)) != null)
				{
					return Image.fromJar(source);
				}
			}
			catch(FileNotFoundException e)
			{
			}
			catch(IOException e)
			{
				CraftGuideLog.log(e, "", true);
			}
		}
		else if(sourceType.equalsIgnoreCase("file") && imageFormat[2] != null)
		{
			return Image.fromFile((File)imageFormat[2], source);
		}

		ThemeManager.debug("        Not found.");
		return null;
	}

    private BufferedImage loadImageToRam(Object[] imageFormat) {
        String sourceType = (String) imageFormat[0];
        String source = (String) imageFormat[1];

        ThemeManager.debug("      Attempting to load " + sourceType + " image '" + source + "'");

        try {
            if (sourceType.equalsIgnoreCase("resource")) {
                ResourceLocation location = new ResourceLocation(source);
                IResource resource = Minecraft.getMinecraft().getResourceManager().getResource(location);
                try (InputStream inputStream = resource.getInputStream()) {
                    return TextureUtil.readBufferedImage(inputStream);
                }
            }
            else if (sourceType.equalsIgnoreCase("file")) {
                File themeFolder = (File) imageFormat[2];
                if (themeFolder != null) {
                    File imageFile = new File(themeFolder, source);
                    if (imageFile.exists() && imageFile.isFile()) {
                        try (InputStream inputStream = Files.newInputStream(imageFile.toPath())) {
                            return TextureUtil.readBufferedImage(inputStream);
                        }
                    }
                }
            }
        } catch (IOException e) {
            ThemeManager.debug("        -> Failed to load with IOException: " + e.getMessage());
        }

        ThemeManager.debug("        -> Not found or failed to read.");
        return null;
    }

	public void addTexture(String id, Texture texture)
	{
		textures.put(id, texture);
	}
}
