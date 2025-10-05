package com.dhjcomical.craftguide.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;

public class PseudoFluidFilter implements ItemFilter
{
    private static NamedTexture containerTexture = null;
    public PseudoFluidStack liquid;
    private String liquidName;
    private List<String> tooltip = new ArrayList<>();

    public PseudoFluidFilter(PseudoFluidStack liquid)
    {
        if(containerTexture == null)
        {
            containerTexture = Util.instance.getTexture("liquidFilterContainer");
        }

        setLiquid(liquid);
    }

    public void setLiquid(PseudoFluidStack liquid)
    {
        String name = liquid.getLocalizedName();
        this.liquid = liquid;
        liquidName = name.toLowerCase();
        tooltip.clear();
        tooltip.add(name);
    }

    @Override
    public boolean matches(Object item)
    {
        if(item instanceof PseudoFluidStack)
        {
            return liquid.isFluidEqual((PseudoFluidStack)item);
        }
        else if(item instanceof String)
        {
            return liquidName.contains(((String)item).toLowerCase());
        }
        else if(item instanceof List)
        {
            for(Object object: ((List<?>)item))
            {
                if(matches(object))
                {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public void draw(Renderer renderer, int x, int y)
    {
        if(liquid != null)
        {
            TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();

            TextureAtlasSprite icon = Util.getFluidIcon(liquid.getIcon());

            if(icon != null)
            {
                textureManager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

                double u = icon.getInterpolatedU(3.0);
                double u2 = icon.getInterpolatedU(13.0);
                double v = icon.getInterpolatedV(1.0);
                double v2 = icon.getInterpolatedV(15.0);

                GlStateManager.enableTexture2D();
                GlStateManager.color(1, 1, 1, 1);

                Tessellator tessellator = Tessellator.getInstance();
                BufferBuilder buffer = tessellator.getBuffer();
                buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
                buffer.pos(x + 3, y + 1, 0).tex(u, v).endVertex();
                buffer.pos(x + 3, y + 15, 0).tex(u, v2).endVertex();
                buffer.pos(x + 13, y + 15, 0).tex(u2, v2).endVertex();
                buffer.pos(x + 13, y + 1, 0).tex(u2, v).endVertex();
                tessellator.draw();
            }
        }

        renderer.renderRect(x - 1, y - 1, 18, 18, containerTexture);
    }

    @Override
    public List<String> getTooltip()
    {
        return tooltip;
    }

    @Override
    public List<ItemStack> getRepresentativeItems()
    {
        return Collections.emptyList();
    }
}