package com.feed_the_beast.ftbl.api.events.team;

import com.feed_the_beast.ftbl.api.IForgeTeam;
import com.feed_the_beast.ftbl.lib.INBTData;

/**
 * Created by LatvianModder on 10.10.2016.
 */
public interface ITeamDataProvider
{
    INBTData getTeamData(IForgeTeam team);
}
