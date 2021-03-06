package com.feed_the_beast.ftbl.net;

import com.feed_the_beast.ftbl.api.info.IInfoPage;
import com.feed_the_beast.ftbl.gui.GuiInfo;
import com.feed_the_beast.ftbl.lib.info.InfoPage;
import com.feed_the_beast.ftbl.lib.net.LMNetworkWrapper;
import com.feed_the_beast.ftbl.lib.net.MessageToClient;
import com.feed_the_beast.ftbl.lib.util.LMNetUtils;
import com.google.gson.JsonElement;
import io.netty.buffer.ByteBuf;

public class MessageDisplayInfo extends MessageToClient<MessageDisplayInfo>
{
    private String infoID;
    private JsonElement json;

    public MessageDisplayInfo()
    {
    }

    public MessageDisplayInfo(IInfoPage page)
    {
        infoID = page.getName();
        json = page.getSerializableElement();
    }

    @Override
    public LMNetworkWrapper getWrapper()
    {
        return FTBLibNetHandler.NET;
    }

    @Override
    public void fromBytes(ByteBuf io)
    {
        infoID = LMNetUtils.readString(io);
        json = LMNetUtils.readJsonElement(io);
    }

    @Override
    public void toBytes(ByteBuf io)
    {
        LMNetUtils.writeString(io, infoID);
        LMNetUtils.writeJsonElement(io, json);
    }

    @Override
    public void onMessage(MessageDisplayInfo m)
    {
        InfoPage page = new InfoPage(m.infoID);
        page.fromJson(m.json);
        new GuiInfo(page).openGui();
    }
}