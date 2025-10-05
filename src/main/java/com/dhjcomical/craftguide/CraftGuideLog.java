package com.dhjcomical.craftguide;

import net.minecraft.client.renderer.GlStateManager;
import org.apache.logging.log4j.Level;
import org.lwjgl.opengl.GL11;

// 这个类现在非常简单
public class CraftGuideLog {
    public static void log(Object object) {
        if (object != null) {
            CraftGuide_FML.logger.log(Level.INFO, object.toString());
        }
    }

    public static void log(String text) {
        CraftGuide_FML.logger.log(Level.INFO, text);
    }

    public static void log(String text, boolean console) {
        CraftGuide_FML.logger.log(Level.INFO, text);
    }

    public static void log(Throwable e) {
        CraftGuide_FML.logger.log(Level.ERROR, e.getLocalizedMessage(), e);
    }

    public static void log(Throwable e, String description, boolean console) {
        CraftGuide_FML.logger.log(Level.ERROR, description, e);
    }

    public static void checkGlError() {
        checkGlError("Somewhere");
    }
    // ------------------------------------

    /**
     * Checks for any OpenGL errors that have occurred. If an error is found,
     * it is logged to the console.
     *
     * @param context A string describing where this check is being performed.
     */
    public static void checkGlError(String context) {
        int error = GlStateManager.glGetError();

        if (error != GL11.GL_NO_ERROR) {
            String errorString;
            switch (error) {
                case GL11.GL_INVALID_ENUM: errorString = "Invalid Enum"; break;
                case GL11.GL_INVALID_VALUE: errorString = "Invalid Value"; break;
                case GL11.GL_INVALID_OPERATION: errorString = "Invalid Operation"; break;
                case GL11.GL_STACK_OVERFLOW: errorString = "Stack Overflow"; break;
                case GL11.GL_STACK_UNDERFLOW: errorString = "Stack Underflow"; break;
                case GL11.GL_OUT_OF_MEMORY: errorString = "Out of Memory"; break;
                default: errorString = "Unknown Error (" + error + ")"; break;
            }

            CraftGuide_FML.logger.log(Level.ERROR, "########## GL ERROR ##########");
            CraftGuide_FML.logger.log(Level.ERROR, "@ " + context);
            CraftGuide_FML.logger.log(Level.ERROR, error + ": " + errorString);
        }
    }

}