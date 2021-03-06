package com.feed_the_beast.ftbl.api.config;

import net.minecraft.util.IStringSerializable;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;

/**
 * Created by LatvianModder on 11.09.2016.
 */
public interface IConfigKey extends IStringSerializable
{
    /**
     * Will be excluded from writing / reading from files
     */
    byte EXCLUDED = 1;

    /**
     * Will be hidden from config gui
     */
    byte HIDDEN = 2;

    /**
     * Will be visible in config gui, but uneditable
     */
    byte CANT_EDIT = 4;

    /**
     * Use scroll bar on numbers whenever that is available
     */
    byte USE_SCROLL_BAR = 8;

    /**
     * Use display name as I18n key
     */
    byte TRANSLATE_DISPLAY_NAME = 16;

    byte getFlags();

    default boolean getFlag(byte flag)
    {
        return (getFlags() & flag) != 0;
    }

    IConfigValue getDefValue();

    String getRawDisplayName();

    default ITextComponent getDisplayName()
    {
        String s = getRawDisplayName();

        if(!s.isEmpty())
        {
            if(getFlag(TRANSLATE_DISPLAY_NAME))
            {
                //TODO: Replace with client side I18n
                return new TextComponentTranslation(s);
            }

            return new TextComponentString(s);
        }

        return new TextComponentString(getName());
    }

    String getInfo();
}