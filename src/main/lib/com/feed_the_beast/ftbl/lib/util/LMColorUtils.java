package com.feed_the_beast.ftbl.lib.util;

import com.feed_the_beast.ftbl.lib.math.MathHelperLM;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.lwjgl.BufferUtils;

import javax.annotation.Nullable;
import java.awt.*;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.EnumMap;

public class LMColorUtils
{
    public static final int[] CHAT_FORMATTING_COLORS = new int[16];
    private static final int[] ID_COLORS = new int[256];

    private static EnumMap<EnumDyeColor, TextFormatting> dyeToTextFormattingMap;
    private static EnumMap<TextFormatting, Character> textFormattingToCharMap;
    private static EnumMap<EnumDyeColor, Integer> dyeTextFormattingColorsLight, dyeTextFormattingColorsDark;

    static
    {
        for(int i = 0; i < 16; i++)
        {
            int j = (i >> 3 & 1) * 85;
            int r = (i >> 2 & 1) * 170 + j;
            int g = (i >> 1 & 1) * 170 + j;
            int b = (i & 1) * 170 + j;
            CHAT_FORMATTING_COLORS[i] = getRGBA((i == 6) ? r + 85 : r, g, b, 255);
        }

        for(int y = 0; y < 16; y++)
        {
            for(int x = 0; x < 16; x++)
            {
                if(x == 0)
                {
                    if(y > 0)
                    {
                        float ry = y == 1 ? 0F : (y == 15) ? 1F : ((y - 1F) / 15F);
                        ID_COLORS[y * 16] = 0xFF000000 | getRGBAF(ry, ry, ry, 1F);
                    }
                }
                else
                {
                    float h = (x - 1F) / 15F;
                    float b = 1F;
                    float s = 1F;

                    if(y < 8)
                    {
                        b = 0.2F + (y / 8F) * 0.8F;
                    }
                    else if(y > 8)
                    {
                        s = 1F - (0.2F + ((y - 8) / 9F) * 0.8F);
                    }

                    ID_COLORS[x + y * 16] = 0xFF000000 | Color.HSBtoRGB(h, s, b);
                }
            }
        }

        ID_COLORS[0] = 0;
    }

    public static int getColorFromID(byte col)
    {
        return ID_COLORS[col & 0xFF];
    }

    public static JsonElement serialize(int col)
    {
        return new JsonPrimitive('#' + Integer.toHexString(col).toUpperCase());
    }

    public static int deserialize(@Nullable JsonElement e)
    {
        if(e == null || !e.isJsonPrimitive())
        {
            return 0xFF000000;
        }

        return (int) Long.parseLong(e.getAsString().substring(1), 16);
    }

    public static int getRGBA(int r, int g, int b, int a)
    {
        return ((a & 255) << 24) | ((r & 255) << 16) | ((g & 255) << 8) | ((b & 255));
    }

    public static int getRGBAF(float r, float g, float b, float a)
    {
        return getRGBA((int) (r * 255F), (int) (g * 255F), (int) (b * 255F), (int) (a * 255F));
    }

    public static int getRed(int c)
    {
        return (c >> 16) & 255;
    }

    public static int getGreen(int c)
    {
        return (c >> 8) & 255;
    }

    public static int getBlue(int c)
    {
        return (c) & 255;
    }

    public static int getAlpha(int c)
    {
        return (c >> 24) & 255;
    }

    public static float getRedF(int c)
    {
        return getRed(c) / 255F;
    }

    public static float getGreenF(int c)
    {
        return getGreen(c) / 255F;
    }

    public static float getBlueF(int c)
    {
        return getBlue(c) / 255F;
    }

    public static float getAlphaF(int c)
    {
        return getAlpha(c) / 255F;
    }

    public static String getHex(int c)
    {
        return '#' + Integer.toHexString(getRGBA(c, 255)).substring(2).toUpperCase();
    }

    public static int getRGBA(int c, int a)
    {
        return getRGBA(getRed(c), getGreen(c), getBlue(c), a);
    }

    public static void setGLColor(int c, int a)
    {
        GlStateManager.color(getRedF(c), getGreenF(c), getBlueF(c), a / 255F);
    }

    public static void setGLColor(int c)
    {
        GlStateManager.color(getRedF(c), getGreenF(c), getBlueF(c), 1F);
    }

    public static ByteBuffer toByteBuffer(int pixels[], boolean alpha)
    {
        ByteBuffer bb = BufferUtils.createByteBuffer(pixels.length * 4);
        byte alpha255 = (byte) 255;

        for(int p : pixels)
        {
            bb.put((byte) getRed(p));
            bb.put((byte) getGreen(p));
            bb.put((byte) getBlue(p));
            bb.put(alpha ? (byte) getAlpha(p) : alpha255);
        }

        bb.flip();
        return bb;
    }

    public static int addBrightness(int c, int b)
    {
        int red = MathHelper.clamp_int(getRed(c) + b, 0, 255);
        int green = MathHelper.clamp_int(getGreen(c) + b, 0, 255);
        int blue = MathHelper.clamp_int(getBlue(c) + b, 0, 255);
        return getRGBA(red, green, blue, getAlpha(c));
    }

    public static void addHSB(int pixels[], float h, float s, float b)
    {
        if(pixels.length > 0)
        {
            float[] hsb = new float[3];

            for(int i = 0; i < pixels.length; i++)
            {
                java.awt.Color.RGBtoHSB(getRed(pixels[i]), getGreen(pixels[i]), getBlue(pixels[i]), hsb);
                hsb[0] += h;
                hsb[1] = MathHelper.clamp_float(hsb[1] + s, 0F, 1F);
                hsb[2] = MathHelper.clamp_float(hsb[2] + b, 0F, 1F);
                pixels[i] = getRGBA(java.awt.Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]), 255);
            }
        }
    }

    public static int lerp(int col1, int col2, double m, int alpha)
    {
        m = MathHelper.clamp_double(m, 0D, 1D);
        int r = MathHelperLM.lerp_int(getRed(col1), getRed(col2), m);
        int g = MathHelperLM.lerp_int(getGreen(col1), getGreen(col2), m);
        int b = MathHelperLM.lerp_int(getBlue(col1), getBlue(col2), m);
        return getRGBA(r, g, b, alpha);
    }

    public static int lerp(int col1, int col2, double m)
    {
        return lerp(col1, col2, m, getAlpha(col1));
    }

    public static int multiply(int col1, int col2, int a)
    {
        float r = MathHelper.clamp_float(getRedF(col1) * getRedF(col2), 0F, 1F);
        float g = MathHelper.clamp_float(getGreenF(col1) * getGreenF(col2), 0F, 1F);
        float b = MathHelper.clamp_float(getBlueF(col1) * getBlueF(col2), 0F, 1F);
        return getRGBA((int) (r * 255F), (int) (g * 255F), (int) (b * 255F), a);
    }

    public static TextFormatting getFromDyeColor(EnumDyeColor color)
    {
        if(dyeToTextFormattingMap == null)
        {
            dyeToTextFormattingMap = new EnumMap<>(EnumDyeColor.class);

            for(EnumDyeColor col : EnumDyeColor.values())
            {
                try
                {
                    Field field = ReflectionHelper.findField(EnumDyeColor.class, "chatColor", "field_176793_x");
                    field.setAccessible(true);
                    dyeToTextFormattingMap.put(col, (TextFormatting) field.get(col));
                }
                catch(Exception ex)
                {
                    ex.printStackTrace();
                }
            }
        }

        return dyeToTextFormattingMap.get(color);
    }

    public static char getTextFormattingChar(TextFormatting formatting)
    {
        if(textFormattingToCharMap == null)
        {
            textFormattingToCharMap = new EnumMap<>(TextFormatting.class);

            for(TextFormatting f : TextFormatting.values())
            {
                try
                {
                    Field field = ReflectionHelper.findField(TextFormatting.class, "formattingCode", "field_96329_z");
                    field.setAccessible(true);
                    textFormattingToCharMap.put(f, (Character) field.get(f));
                }
                catch(Exception ex)
                {
                    ex.printStackTrace();
                }
            }
        }

        return textFormattingToCharMap.get(formatting);
    }

    public static int getDyeColor(EnumDyeColor color, boolean isLighter)
    {
        if(isLighter)
        {
            if(dyeTextFormattingColorsLight == null)
            {
                dyeTextFormattingColorsLight = new EnumMap<>(EnumDyeColor.class);

                for(EnumDyeColor col : EnumDyeColor.values())
                {
                    dyeTextFormattingColorsLight.put(col, GuiUtils.getColorCode(getTextFormattingChar(getFromDyeColor(color)), true));
                }
            }

            return dyeTextFormattingColorsLight.get(color);
        }
        else
        {
            if(dyeTextFormattingColorsDark == null)
            {
                dyeTextFormattingColorsDark = new EnumMap<>(EnumDyeColor.class);

                for(EnumDyeColor col : EnumDyeColor.values())
                {
                    dyeTextFormattingColorsDark.put(col, GuiUtils.getColorCode(getTextFormattingChar(getFromDyeColor(color)), false));
                }
            }

            return dyeTextFormattingColorsDark.get(color);
        }
    }
}