package ftb.lib.mod.net;

import cpw.mods.fml.common.network.simpleimpl.*;
import cpw.mods.fml.relauncher.*;
import ftb.lib.api.client.FTBLibClient;
import ftb.lib.api.config.ServerConfigProvider;
import ftb.lib.api.net.*;
import ftb.lib.mod.client.gui.GuiEditConfig;
import latmod.lib.ByteCount;
import latmod.lib.config.ConfigGroup;

public class MessageEditConfig extends MessageLM // MessageEditConfigResponse
{
	public MessageEditConfig() { super(ByteCount.INT); }
	
	public MessageEditConfig(long t, ConfigGroup o)
	{
		this();
		io.writeLong(t);
		io.writeUTF(o.ID);
		o.writeExtended(io);
	}
	
	public LMNetworkWrapper getWrapper()
	{ return FTBLibNetHandler.NET; }
	
	@SideOnly(Side.CLIENT)
	public IMessage onMessage(MessageContext ctx)
	{
		long token = io.readLong();
		String id = io.readUTF();
		
		ConfigGroup group = new ConfigGroup(id);
		group.readExtended(io);
		
		FTBLibClient.openGui(new GuiEditConfig(FTBLibClient.mc.currentScreen, new ServerConfigProvider(token, group)));
		return null;
	}
}