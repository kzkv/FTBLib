package ftb.lib.api;

import ftb.lib.api.players.*;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.*;

/**
 * Created by LatvianModder on 10.02.2016.
 */
public class ForgePlayerDataEvent extends Event
{
	public final LMPlayer player;
	private final Map<String, ForgePlayerData> map;
	
	public ForgePlayerDataEvent(LMPlayer p)
	{
		player = p;
		map = new HashMap<>();
	}
	
	public void add(ForgePlayerData data)
	{
		if(!map.containsKey(data.ID))
		{
			map.put(data.ID, data);
		}
	}
	
	public Map<String, ForgePlayerData> getMap()
	{ return Collections.unmodifiableMap(map); }
}