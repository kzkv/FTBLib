package com.feed_the_beast.ftbl.gui.friends;

import com.feed_the_beast.ftbl.api.gui.IWidget;
import com.feed_the_beast.ftbl.client.ClientNotifications;
import com.feed_the_beast.ftbl.gui.GuiInfo;
import com.feed_the_beast.ftbl.lib.gui.GuiIcons;
import com.feed_the_beast.ftbl.lib.info.ButtonInfoPage;
import com.feed_the_beast.ftbl.lib.info.InfoPage;
import com.feed_the_beast.ftbl.lib.info.TexturePageIconRenderer;
import com.feed_the_beast.ftbl.lib.util.LMMapUtils;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

/**
 * Created by LatvianModder on 24.03.2016.
 */
public class InfoNotificationsPage extends InfoPage
{
    public InfoNotificationsPage()
    {
        super("notifications");
        setTitle(new TextComponentTranslation("client_config.ftbl.notifications"));
    }

    @Override
    public void refreshGui(GuiInfo gui)
    {
        ITextComponent title = new TextComponentTranslation("client_config.ftbl.notifications");

        if(!ClientNotifications.Perm.MAP.isEmpty())
        {
            title.appendText(" [");
            ITextComponent num = new TextComponentString(Integer.toString(ClientNotifications.Perm.MAP.size()));
            num.getStyle().setColor(TextFormatting.RED);
            title.appendSibling(num);
            title.appendText("]");
        }

        setTitle(title);

        clear();

        if(!ClientNotifications.Perm.MAP.isEmpty())
        {
            LMMapUtils.sortMap(ClientNotifications.Perm.MAP, (o1, o2) -> Long.compare(o2.getValue().timeAdded, o1.getValue().timeAdded));

            for(ClientNotifications.Perm p : ClientNotifications.Perm.MAP.values())
            {
                println(new InfoNotificationLine(p));
            }
        }
    }

    @Override
    public IWidget createButton(GuiInfo gui)
    {
        return new ButtonInfoPage(gui, this, new TexturePageIconRenderer(GuiIcons.CHAT));
    }
}
