package com.dhjcomical.gui_craftguide.texture;

import com.dhjcomical.gui_craftguide.rendering.RendererBase;
import com.dhjcomical.gui_craftguide.theme.ThemeManager;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

public class BasicTexture implements Texture {
    private final int glTextureId;
    private final int textureWidth;
    private final int textureHeight;

    public BasicTexture(int width, int height, int glTextureId) {
        this.textureWidth = width;
        this.textureHeight = height;
        this.glTextureId = glTextureId;
    }

    @Override
    public void renderRect(RendererBase renderer, int x, int y, int width, int height, int u, int v) {
        if (this.glTextureId <= 0) return;

        ThemeManager.debug("[CRAFTGUIDE DEBUG]     -> RENDERING with GL ID: " + this.glTextureId);


        GlStateManager.enableTexture2D();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.glTextureId);

        double u1 = (double) u / (double) this.textureWidth;
        double v1 = (double) v / (double) this.textureHeight;
        double u2 = (double) (u + width) / (double) this.textureWidth;
        double v2 = (double) (v + height) / (double) this.textureHeight;

        renderer.drawTexturedRect(x, y, width, height, u1, v1, u2, v2);
    }
}