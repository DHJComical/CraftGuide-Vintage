package com.dhjcomical.craftguide.client.ui;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import com.dhjcomical.craftguide.api.Renderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import com.dhjcomical.craftguide.CommonUtilities;
import com.dhjcomical.craftguide.CraftGuide;
import com.dhjcomical.craftguide.CraftGuideLog;
import com.dhjcomical.craftguide.api.NamedTexture;
import com.dhjcomical.craftguide.client.ui.Rendering.Overlay;
import com.dhjcomical.gui_craftguide.minecraft.Gui;
import com.dhjcomical.gui_craftguide.rendering.Renderable;
import com.dhjcomical.gui_craftguide.rendering.RendererBase;
import com.dhjcomical.gui_craftguide.rendering.TexturedRect;
import com.dhjcomical.gui_craftguide.texture.DynamicTexture;
import com.dhjcomical.gui_craftguide.texture.Texture;
import org.lwjgl.opengl.GL12;

public class GuiRenderer extends RendererBase implements Renderer {
    // ======================= DEBUG CONFIGURATION =======================
    /**
     * Master switch for all debugging features.
     * Set to 'true' to enable detailed logging for debugging rendering issues.
     * Set to 'false' for normal gameplay to avoid log spam.
     */
    public static final boolean DEBUG_MODE = true;

    /**
     * Log GL state before and after each item render.
     */
    public static final boolean DEBUG_GL_STATE = true;

    /**
     * Log detailed model information.
     */
    public static final boolean DEBUG_MODEL_INFO = true;

    /**
     * Track rendering performance.
     */
    public static final boolean DEBUG_PERFORMANCE = true;
    // ===================================================================

    private double frameStartTime;
    private List<Overlay> overlays = new LinkedList<>();
    private Gui gui;
    private RenderItem itemRenderer = null;

    private static Map<ItemStack, ItemStack> itemStackFixes = new HashMap<>();
    private Renderable itemError = new TexturedRect(-1, -1, 18, 18, DynamicTexture.instance("item_error"), 238, 200);

    // Debug counters
    private static int totalRenderAttempts = 0;
    private static int successfulRenders = 0;
    private static int failedRenders = 0;
    private static Map<String, Integer> itemRenderCounts = new HashMap<>();

    // Batch rendering tracking
    private static int currentBatchSize = 0;
    private static final int BATCH_WARNING_THRESHOLD = 50;
    private static long batchStartTime = 0;

    public void startFrame(Gui gui) {
        this.gui = gui;
        resetValues();
        frameStartTime = Minecraft.getSystemTime() / 1000.0;

        // CRITICAL: Ensure texture atlas is bound at frame start
        // This prevents carryover issues from previous frame
        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        // Reset GL state to known good values
        GlStateManager.disableLighting();
        GlStateManager.disableAlpha();
        GlStateManager.disableDepth();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        if (DEBUG_MODE && DEBUG_PERFORMANCE) {
            int startTexture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
            CraftGuideLog.log("[FRAME_START] New frame started at " + frameStartTime + " | Initial texture: " + startTexture);
        }
    }

    public void endFrame() {
        for (Overlay overlay : overlays) {
            overlay.renderOverlay(this);
        }

        overlays.clear();
        GlStateManager.color(1, 1, 1, 1);

        if (DEBUG_MODE && DEBUG_PERFORMANCE) {
            CraftGuideLog.log("[FRAME_END] Total renders this session: " + totalRenderAttempts +
                    " | Success: " + successfulRenders +
                    " | Failed: " + failedRenders +
                    " | This frame: " + currentBatchSize);
        }

        // Reset batch counter for next frame
        currentBatchSize = 0;
    }

    public void setColor(int colour, int alpha) {
        setColorRgb(colour);
        setAlpha(alpha);
    }

    @Override
    public void setTextureID(int textureID) {
        if (textureID != -1) {
            GlStateManager.bindTexture(textureID);
        }
    }

    public void drawGradient(int x, int y, int width, int height, int topColor, int bottomColor) {
        renderVerticalGradient(x, y, width, height, topColor, bottomColor);
    }

    public void render(Renderable renderable, int xOffset, int yOffset) {
        renderable.render(this, xOffset, yOffset);
    }

    public void overlay(Overlay overlay) {
        overlays.add(overlay);
    }

    /**
     * Adapt from cg.api.Renderer conventions to uq.gui.Renderer conventions
     */
    @Override
    public void renderText(int x, int y, String text, int textColor, boolean drawShadow) {
        int prevColor = currentColor();
        setColor(textColor);
        if (drawShadow)
            drawTextWithShadow(text, x, y);
        else
            drawText(text, x, y);
        setColor(prevColor);
    }

    @Override
    public void drawText(String text, int x, int y) {
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.disableBlend();
        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        Minecraft.getMinecraft().fontRenderer.drawString(text, x, y, currentColor());
        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
    }

    @Override
    public void drawTextWithShadow(String text, int x, int y) {
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.disableBlend();
        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(text, x, y, currentColor());
        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
    }

    private int currentColor() {
        return ((((int) (alpha * 255)) & 0xff) << 24) | ((((int) (red * 255)) & 0xff) << 16) | ((((int) (green * 255)) & 0xff) << 8) | (((int) (blue * 255)) & 0xff);
    }

    public void drawFloatingText(int x, int y, String text) {
        List<String> list = new ArrayList<>(1);
        list.add(text);
        drawFloatingText(x, y, list);
    }

    public void drawFloatingText(int x, int y, List<String> text) {
        int textWidth = 0;
        int textHeight = (text.size() > 1) ? text.size() * 10 : 8;

        for (String s : text) {
            int w;

            if (s.charAt(0) == '\u00a7') {
                w = Minecraft.getMinecraft().fontRenderer.getStringWidth(s.substring(2));
            } else {
                w = Minecraft.getMinecraft().fontRenderer.getStringWidth(s);
            }

            if (w > textWidth) {
                textWidth = w;
            }
        }

        int xMax = gui.width - textWidth - 4;
        int yMax = gui.height - textHeight - 4;

        if (x > xMax) {
            x = xMax;
        }

        if (x < 3) {
            x = 3;
        }

        if (y > yMax) {
            y = yMax;
        }

        if (y < 4) {
            y = 4;
        }

        setColor(0xf0100010);
        drawRect(x - 3, y - 4, textWidth + 6, 1);
        drawRect(x - 3, y + textHeight + 3, textWidth + 6, 1);
        drawRect(x - 3, y - 3, textWidth + 6, textHeight + 6);
        drawRect(x - 4, y - 3, 1, textHeight + 6);
        drawRect(x + textWidth + 3, y - 3, 1, textHeight + 6);

        setColor(0x505000ff);
        drawRect(x - 3, y - 3, textWidth + 6, 1);

        setColor(0x5028007f);
        drawRect(x - 3, y + textHeight + 2, textWidth + 6, 1);

        drawGradient(x - 3, y - 2, 1, textHeight + 4, 0x505000ff, 0x5028007f);
        drawGradient(x + textWidth + 2, y - 2, 1, textHeight + 4, 0x505000ff, 0x5028007f);

        setColor(0xffffffff);

        int textY = y;
        boolean first = true;
        for (String s : text) {
            drawTextWithShadow(s, x, textY);

            if (first) {
                textY += 2;
                first = false;
            }

            textY += 10;
        }
    }

    // ======================= ENHANCED ITEM RENDERING WITH FULL DEBUG =======================

    /**
     * Main item rendering method with comprehensive debugging.
     */
    public void drawItemStack(ItemStack itemStack, int x, int y, boolean renderOverlay) {
        totalRenderAttempts++;
        currentBatchSize++;
        long startTime = DEBUG_PERFORMANCE ? System.nanoTime() : 0;

        // Warn if batch is getting large
        if (DEBUG_MODE && currentBatchSize == 1) {
            batchStartTime = System.currentTimeMillis();
        }
        if (DEBUG_MODE && currentBatchSize == BATCH_WARNING_THRESHOLD) {
            long batchDuration = System.currentTimeMillis() - batchStartTime;
            CraftGuideLog.log("[BATCH_WARNING] Rendered " + BATCH_WARNING_THRESHOLD +
                    " items in " + batchDuration + "ms - GL state may accumulate issues");
        }

        if (DEBUG_MODE) {
            CraftGuideLog.log("========================================");
            CraftGuideLog.log("[RENDER_START] Attempt #" + totalRenderAttempts +
                    " (Batch: " + currentBatchSize + ")");
        }

        // === STEP 1: Validate ItemStack ===
        if (itemStack == null || itemStack.isEmpty()) {
            if (DEBUG_MODE) {
                CraftGuideLog.log("[VALIDATION] ItemStack is " + (itemStack == null ? "NULL" : "EMPTY"));
                CraftGuideLog.log("[RENDER_ABORT] Nothing to render");
                CraftGuideLog.log("========================================\n");
            }
            return;
        }

        String itemId = getItemIdentifier(itemStack);
        itemRenderCounts.put(itemId, itemRenderCounts.getOrDefault(itemId, 0) + 1);

        // CRITICAL: Force GL state to known good values BEFORE anything else
        // This protects against state pollution from previous rendering operations
        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO
        );
        GlStateManager.disableLighting();
        GlStateManager.disableAlpha();
        RenderHelper.disableStandardItemLighting();

        if (DEBUG_MODE) {
            CraftGuideLog.log("[ITEM_INFO]");
            CraftGuideLog.log("  Registry Name: " + itemStack.getItem().getRegistryName());
            CraftGuideLog.log("  Metadata: " + itemStack.getMetadata());
            CraftGuideLog.log("  Stack Size: " + itemStack.getCount());
            CraftGuideLog.log("  Position: [x=" + x + ", y=" + y + "]");
            CraftGuideLog.log("  Render Overlay: " + renderOverlay);
            CraftGuideLog.log("  Times Rendered: " + itemRenderCounts.get(itemId));
        }

        // === STEP 2: Get RenderItem ===
        RenderItem itemRenderer = Minecraft.getMinecraft().getRenderItem();
        if (itemRenderer == null) {
            CraftGuideLog.log("[CRITICAL_ERROR] RenderItem is NULL! Cannot proceed.");
            failedRenders++;
            return;
        }

        if (DEBUG_MODE) {
            CraftGuideLog.log("[RENDER_ITEM] Instance obtained: " + itemRenderer.getClass().getName());
            CraftGuideLog.log("  zLevel: " + itemRenderer.zLevel);
        }

        // === STEP 3: Get and Analyze Model ===
        IBakedModel bakedModel = null;
        if (DEBUG_MODE && DEBUG_MODEL_INFO) {
            try {
                bakedModel = itemRenderer.getItemModelWithOverrides(itemStack, null, Minecraft.getMinecraft().player);
                CraftGuideLog.log("[MODEL_INFO]");
                CraftGuideLog.log("  Class: " + bakedModel.getClass().getName());
                CraftGuideLog.log("  Is Built-in Renderer: " + bakedModel.isBuiltInRenderer());
                CraftGuideLog.log("  Is GUI 3D: " + bakedModel.isGui3d());
                CraftGuideLog.log("  Is Ambient Occlusion: " + bakedModel.isAmbientOcclusion());
                CraftGuideLog.log("  Particle Texture: " + (bakedModel.getParticleTexture() != null ?
                        bakedModel.getParticleTexture().getIconName() : "null"));

                // Check for missing model
                if (bakedModel.getClass().getSimpleName().contains("Missing")) {
                    CraftGuideLog.log("  [WARNING] This appears to be a MISSING MODEL!");
                }
            } catch (Exception e) {
                CraftGuideLog.log("[MODEL_ERROR] Failed to retrieve model: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // === STEP 4: Log GL State BEFORE rendering ===
        if (DEBUG_MODE && DEBUG_GL_STATE) {
            logGLState("BEFORE_RENDER");
        }

        // === STEP 5: Perform Rendering ===
        GlStateManager.pushMatrix();
        if (DEBUG_MODE) CraftGuideLog.log("[GL_STATE] Matrix pushed");

        boolean renderSuccessful = false;
        Exception caughtException = null;

        try {
            if (DEBUG_MODE) CraftGuideLog.log("[RENDER_CALL] Calling renderItemAndEffectIntoGUI...");

            // Main render call
            itemRenderer.renderItemAndEffectIntoGUI(itemStack, x, y);

            if (DEBUG_MODE) CraftGuideLog.log("[RENDER_CALL] ✓ Main render completed");

            // Overlay render
            if (renderOverlay) {
                if (DEBUG_MODE) CraftGuideLog.log("[RENDER_CALL] Rendering overlay...");
                itemRenderer.renderItemOverlayIntoGUI(
                        Minecraft.getMinecraft().fontRenderer,
                        itemStack, x, y, null
                );
                if (DEBUG_MODE) CraftGuideLog.log("[RENDER_CALL] ✓ Overlay render completed");
            }

            renderSuccessful = true;
            successfulRenders++;

        } catch (Exception e) {
            caughtException = e;
            failedRenders++;

            CraftGuideLog.log("[RENDER_EXCEPTION] ✗ Exception during rendering!");
            CraftGuideLog.log("  Exception Type: " + e.getClass().getName());
            CraftGuideLog.log("  Message: " + e.getMessage());

            logItemRenderException(itemStack, e);

            // Try to draw error indicator
            try {
                drawItemStackError(x, y);
            } catch (Exception e2) {
                CraftGuideLog.log("[ERROR_INDICATOR] Failed to draw error texture: " + e2.getMessage());
            }

        } finally {
            // === STEP 6: Cleanup and State Reset ===
            if (DEBUG_MODE) CraftGuideLog.log("[CLEANUP] Restoring GL state...");

            // Pop matrix first
            GlStateManager.popMatrix();

            // Reset all GL state that item rendering might have changed
            GlStateManager.disableLighting();
            GlStateManager.disableAlpha();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(
                    GlStateManager.SourceFactor.SRC_ALPHA,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                    GlStateManager.SourceFactor.ONE,
                    GlStateManager.DestFactor.ZERO
            );
            GlStateManager.disableDepth();
            GlStateManager.disableRescaleNormal();
            GlStateManager.enableTexture2D();

            // Disable item lighting
            RenderHelper.disableStandardItemLighting();

            // Reset color to white (important!)
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

            // CRITICAL: Restore texture binding to blocks atlas
            // This MUST be the last operation to ensure it's not overridden
            Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

            if (DEBUG_MODE) {
                // Verify the fix worked
                int textureAfterFix = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
                CraftGuideLog.log("[CLEANUP] ✓ State reset complete - Texture after fix: " + textureAfterFix);
            }
        }

        // === STEP 7: Log GL State AFTER rendering ===
        if (DEBUG_MODE && DEBUG_GL_STATE) {
            logGLState("AFTER_RENDER");
        }

        // === STEP 8: Performance Summary ===
        if (DEBUG_MODE) {
            if (DEBUG_PERFORMANCE) {
                long duration = (System.nanoTime() - startTime) / 1000; // microseconds
                CraftGuideLog.log("[PERFORMANCE] Render took " + duration + " μs");
            }

            CraftGuideLog.log("[RENDER_RESULT] " + (renderSuccessful ? "✓ SUCCESS" : "✗ FAILED"));
            CraftGuideLog.log("========================================\n");
        }

        // SUPER CRITICAL: Force texture rebind one final time
        // This is necessary because some items (especially TESR/BuiltInModel items like beds)
        // may change texture bindings AFTER the finally block completes
        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
    }

    public void drawItemStack(ItemStack itemStack, int x, int y) {
        drawItemStack(itemStack, x, y, true);
    }

    /**
     * Logs detailed GL state information for debugging.
     */
    private void logGLState(String phase) {
        CraftGuideLog.log("[GL_STATE_" + phase + "]");

        try {
            // Texture state
            int currentTexture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
            CraftGuideLog.log("  Texture Bound: " + currentTexture);
            CraftGuideLog.log("  Texture 2D: " + (GL11.glIsEnabled(GL11.GL_TEXTURE_2D) ? "ENABLED" : "DISABLED"));

            // Blending
            CraftGuideLog.log("  Blend: " + (GL11.glIsEnabled(GL11.GL_BLEND) ? "ENABLED" : "DISABLED"));
            if (GL11.glIsEnabled(GL11.GL_BLEND)) {
                CraftGuideLog.log("    Src Factor: " + GL11.glGetInteger(GL11.GL_BLEND_SRC));
                CraftGuideLog.log("    Dst Factor: " + GL11.glGetInteger(GL11.GL_BLEND_DST));
            }

            // Depth test
            CraftGuideLog.log("  Depth Test: " + (GL11.glIsEnabled(GL11.GL_DEPTH_TEST) ? "ENABLED" : "DISABLED"));
            CraftGuideLog.log("  Depth Mask: " + GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK));

            // Lighting
            CraftGuideLog.log("  Lighting: " + (GL11.glIsEnabled(GL11.GL_LIGHTING) ? "ENABLED" : "DISABLED"));

            // Alpha test
            CraftGuideLog.log("  Alpha Test: " + (GL11.glIsEnabled(GL11.GL_ALPHA_TEST) ? "ENABLED" : "DISABLED"));

            // Cull face
            CraftGuideLog.log("  Cull Face: " + (GL11.glIsEnabled(GL11.GL_CULL_FACE) ? "ENABLED" : "DISABLED"));

            // Current color (requires 16 float buffer for some drivers)
            try {
                FloatBuffer colorBuffer = BufferUtils.createFloatBuffer(16);
                GL11.glGetFloat(GL11.GL_CURRENT_COLOR, colorBuffer);
                CraftGuideLog.log("  Current Color: [R=" + colorBuffer.get(0) +
                        ", G=" + colorBuffer.get(1) +
                        ", B=" + colorBuffer.get(2) +
                        ", A=" + colorBuffer.get(3) + "]");
            } catch (Exception e) {
                CraftGuideLog.log("  Current Color: <unable to query>");
            }

        } catch (Exception e) {
            CraftGuideLog.log("  [ERROR] Failed to query GL state: " + e.getMessage());
        }
    }

    /**
     * Gets a unique identifier for an ItemStack for tracking purposes.
     */
    private String getItemIdentifier(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return "null";
        return stack.getItem().getRegistryName() + "@" + stack.getMetadata();
    }

    /**
     * Logs exception details for failed item renders.
     */
    private void logItemRenderException(ItemStack itemStack, Exception e) {
        if (!hasLogged(itemStack)) {
            CraftGuideLog.log("========== RENDER EXCEPTION DETAILS ==========");
            CraftGuideLog.log("Failed to render ItemStack:");

            if (itemStack == null) {
                CraftGuideLog.log("  ItemStack: null");
            } else {
                CraftGuideLog.log("  Item ID: " + ForgeRegistries.ITEMS.getKey(itemStack.getItem()));
                CraftGuideLog.log("  Damage: " + CommonUtilities.getItemDamage(itemStack));
                CraftGuideLog.log("  Stack Size: " + itemStack.getCount());
                CraftGuideLog.log("  Has NBT: " + itemStack.hasTagCompound());
                if (itemStack.hasTagCompound()) {
                    CraftGuideLog.log("  NBT: " + itemStack.getTagCompound());
                }
            }

            CraftGuideLog.log("\nStack Trace:");
            CraftGuideLog.log(e);
            CraftGuideLog.log("(Further exceptions from this ItemStack will be suppressed)");
            CraftGuideLog.log("==============================================\n");
        }
    }

    private ItemStack renderItem(ItemStack itemStack, boolean renderOverlay) {
        if (CommonUtilities.getItemDamage(itemStack) == CraftGuide.DAMAGE_WILDCARD) {
            itemStack = fixedItemStack(itemStack);
        }

        itemRenderer.renderItemAndEffectIntoGUI(itemStack, 0, 0);

        if (renderOverlay) {
            itemRenderer.renderItemOverlayIntoGUI(Minecraft.getMinecraft().fontRenderer, itemStack, 0, 0, null);
        }

        return itemStack;
    }

    private HashSet<ItemStack> loggedStacks = new HashSet<>();

    private boolean hasLogged(ItemStack stack) {
        return !loggedStacks.add(stack);
    }

    public static ItemStack fixedItemStack(ItemStack itemStack) {
        ItemStack stack = itemStackFixes.get(itemStack);

        if (stack == null) {
            stack = itemStack.copy();
            stack.setItemDamage(0);
            itemStackFixes.put(itemStack, stack);
        }

        return stack;
    }

    private void drawItemStackError(int x, int y) {
        if (DEBUG_MODE) {
            CraftGuideLog.log("[ERROR_VISUAL] Drawing error indicator at [" + x + ", " + y + "]");
        }
        itemError.render(this, x, y);
    }

    public void setClippingRegion(int x, int y, int width, int height) {
        GL11.glEnable(GL11.GL_SCISSOR_TEST);

        x *= Minecraft.getMinecraft().displayHeight / (float) gui.height;
        y *= Minecraft.getMinecraft().displayWidth / (float) gui.width;
        height *= Minecraft.getMinecraft().displayHeight / (float) gui.height;
        width *= Minecraft.getMinecraft().displayWidth / (float) gui.width;

        GL11.glScissor(x, Minecraft.getMinecraft().displayHeight - y - height, width, height);
    }

    public void clearClippingRegion() {
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    public int guiXFromMouseX(int x) {
        return (x * gui.width) / Minecraft.getMinecraft().displayWidth;
    }

    public int guiYFromMouseY(int y) {
        return gui.height - (y * gui.height) / Minecraft.getMinecraft().displayHeight - 1;
    }

    @Override
    public void renderItemStack(int x, int y, ItemStack stack) {
        drawItemStack(stack, x, y);
    }

    @Override
    public void renderRect(int x, int y, int width, int height, NamedTexture texture) {
        if (texture != null) {
            setColor(255, 255, 255, 255);
            drawTexturedRect((Texture) texture, x, y, width, height, 0, 0);
        }
    }

    @Override
    public void renderRect(int x, int y, int width, int height, int red, int green, int blue, int alpha) {
        setColor(red, green, blue, alpha);
        drawRect(x, y, width, height);
        setColor(0xff, 0xff, 0xff, 0xff);
    }

    @Override
    public void renderRect(int x, int y, int width, int height, int color_rgb, int alpha) {
        renderRect(x, y, width, height,
                (color_rgb >> 16) & 0xff,
                (color_rgb >> 8) & 0xff,
                (color_rgb >> 0) & 0xff,
                alpha);
    }

    @Override
    public void renderRect(int x, int y, int width, int height, int color_argb) {
        renderRect(x, y, width, height, color_argb & 0x00ffffff, (color_argb >> 24) & 0xff);
    }

    @Override
    public void renderVerticalGradient(int x, int y, int width, int height, int topColor_argb, int bottomColor_argb) {
        renderGradient(x, y, width, height, topColor_argb, topColor_argb, bottomColor_argb, bottomColor_argb);
    }

    @Override
    public void renderHorizontalGradient(int x, int y, int width, int height, int leftColor_argb, int rightColor_argb) {
        renderGradient(x, y, width, height, leftColor_argb, rightColor_argb, leftColor_argb, rightColor_argb);
    }

    @Override
    public void renderGradient(int x, int y, int width, int height, int topLeftColor_argb, int topRightColor_argb,
                               int bottomLeftColor_argb, int bottomRightColor_argb) {
        GlStateManager.disableTexture2D();
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(770, 771);
        GlStateManager.disableLighting();

        GL11.glBegin(GL11.GL_QUADS);
        glColor1i(topLeftColor_argb);
        GL11.glVertex2i(x, y);

        glColor1i(bottomLeftColor_argb);
        GL11.glVertex2i(x, y + height);

        glColor1i(bottomRightColor_argb);
        GL11.glVertex2i(x + width, y + height);

        glColor1i(topRightColor_argb);
        GL11.glVertex2i(x + width, y);
        GL11.glEnd();

        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    private void glColor1i(int color) {
        setGlColor(
                ((color >> 16) & 0xff) / 255.0f,
                ((color >> 8) & 0xff) / 255.0f,
                ((color >> 0) & 0xff) / 255.0f,
                ((color >> 24) & 0xff) / 255.0f);
    }

    private WeakHashMap<ItemStack, Void> invalidStacks = new WeakHashMap<>();

    public List<String> getItemNameandInformation(ItemStack stack) {
        if (!invalidStacks.containsKey(stack)) {
            if (!stack.isEmpty()) {
                try {
                    return getTooltip(stack);
                } catch (Exception e) {
                    try {
                        stack = fixedItemStack(stack);
                        return getTooltip(stack);
                    } catch (Exception e2) {
                        CraftGuideLog.log(e2);
                    }
                }
            }
        }

        invalidStacks.put(stack, null);
        List<String> list = new ArrayList<>();
        list.add(TextFormatting.YELLOW + "Err: Item " + Item.REGISTRY.getNameForObject(stack.getItem()) + ", damage " + CommonUtilities.getItemDamage(stack));
        return list;
    }

    private List<String> getTooltip(ItemStack stack) {
        return stack.getTooltip(
                Minecraft.getMinecraft().player,
                Minecraft.getMinecraft().gameSettings.advancedItemTooltips ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL
        );
    }

    @Override
    public double getClock() {
        return frameStartTime;
    }

    /**
     * Prints a debug report of all rendered items and their statistics.
     * Call this method to get a summary of rendering activity.
     */
    public static void printDebugReport() {
        CraftGuideLog.log("\n==================== RENDER DEBUG REPORT ====================");
        CraftGuideLog.log("Total Render Attempts: " + totalRenderAttempts);
        CraftGuideLog.log("Successful Renders: " + successfulRenders +
                " (" + (totalRenderAttempts > 0 ? (successfulRenders * 100 / totalRenderAttempts) : 0) + "%)");
        CraftGuideLog.log("Failed Renders: " + failedRenders +
                " (" + (totalRenderAttempts > 0 ? (failedRenders * 100 / totalRenderAttempts) : 0) + "%)");

        if (!itemRenderCounts.isEmpty()) {
            CraftGuideLog.log("\n--- Item Render Statistics ---");
            itemRenderCounts.entrySet().stream()
                    .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                    .limit(20)
                    .forEach(entry -> {
                        CraftGuideLog.log("  " + entry.getKey() + ": " + entry.getValue() + " times");
                    });
            if (itemRenderCounts.size() > 20) {
                CraftGuideLog.log("  ... and " + (itemRenderCounts.size() - 20) + " more items");
            }
        }

        CraftGuideLog.log("=============================================================\n");
    }

    /**
     * Resets all debug counters. Useful for testing specific scenarios.
     */
    public static void resetDebugCounters() {
        totalRenderAttempts = 0;
        successfulRenders = 0;
        failedRenders = 0;
        itemRenderCounts.clear();
        CraftGuideLog.log("[DEBUG] All render counters have been reset.");
    }
}