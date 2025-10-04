package com.dhjcomical.gui_craftguide.texture;

import java.util.HashMap;
import java.util.Map;
import com.dhjcomical.gui_craftguide.rendering.RendererBase;
import com.dhjcomical.gui_craftguide.theme.ThemeManager;

public class DynamicTexture implements Texture {
    private static Map<String, Texture> textureRegistry = new HashMap<>();
    private static Map<String, DynamicTexture> instanceRegistry = new HashMap<>();
    private final String id;

    private DynamicTexture(String id) { this.id = id; }
    public String getId() { return this.id; }

    public static DynamicTexture instance(String id) {
        if (!instanceRegistry.containsKey(id)) {
            instanceRegistry.put(id, new DynamicTexture(id));
        }
        return instanceRegistry.get(id);
    }

    public static void instance(String id, Texture texture) {

        ThemeManager.debug("[CRAFTGUIDE DEBUG] CACHING texture for ID '" + id + "'. Texture is a: " + texture.getClass().getSimpleName());

        textureRegistry.put(id, texture);
    }

    public Texture getTexture() {
        Texture realTexture = textureRegistry.get(this.id);

        ThemeManager.debug("[CRAFTGUIDE DEBUG]   -> ClipTexture is asking for real texture of '" + this.id + "'. Found: " + (realTexture != null ? realTexture.getClass().getSimpleName() : "null"));

        return realTexture;
    }

    @Override
    public void renderRect(RendererBase renderer, int x, int y, int width, int height, int u, int v) {
        Texture realTexture = getTexture();
        if (realTexture != null) {
            realTexture.renderRect(renderer, x, y, width, height, u, v);
        }
    }
}