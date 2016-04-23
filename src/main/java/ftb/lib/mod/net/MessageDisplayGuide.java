package ftb.lib.mod.net;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ftb.lib.api.client.FTBLibClient;
import ftb.lib.api.info.InfoPage;
import ftb.lib.api.net.LMNetworkWrapper;
import ftb.lib.api.net.MessageLM;
import ftb.lib.mod.client.gui.info.GuiInfo;
import latmod.lib.ByteCount;
import latmod.lib.json.JsonElementIO;

public class MessageDisplayGuide extends MessageLM
{
	public MessageDisplayGuide() { super(ByteCount.INT); }
	
	public MessageDisplayGuide(InfoPage file)
	{
		this();
		file.cleanup();
		io.writeUTF(file.getID());
		JsonElementIO.write(io, file.getSerializableElement());
	}
	
	@Override
	public LMNetworkWrapper getWrapper()
	{ return FTBLibNetHandler.NET_GUI; }
	
	@Override
	@SideOnly(Side.CLIENT)
	public IMessage onMessage(MessageContext ctx)
	{
		InfoPage file = new InfoPage(io.readUTF());
		file.func_152753_a(JsonElementIO.read(io));
		FTBLibClient.openGui(new GuiInfo(null, file));
		return null;
	}
}