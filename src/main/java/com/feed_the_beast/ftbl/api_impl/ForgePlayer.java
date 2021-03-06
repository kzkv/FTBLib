package com.feed_the_beast.ftbl.api_impl;

import com.feed_the_beast.ftbl.FTBLibStats;
import com.feed_the_beast.ftbl.api.IForgePlayer;
import com.feed_the_beast.ftbl.api.IForgeTeam;
import com.feed_the_beast.ftbl.api.config.IConfigTree;
import com.feed_the_beast.ftbl.api.events.player.ForgePlayerDeathEvent;
import com.feed_the_beast.ftbl.api.events.player.ForgePlayerInfoEvent;
import com.feed_the_beast.ftbl.api.events.player.ForgePlayerLoggedInEvent;
import com.feed_the_beast.ftbl.api.events.player.ForgePlayerLoggedOutEvent;
import com.feed_the_beast.ftbl.api.events.player.ForgePlayerSettingsEvent;
import com.feed_the_beast.ftbl.lib.INBTData;
import com.feed_the_beast.ftbl.lib.NBTDataStorage;
import com.feed_the_beast.ftbl.lib.util.LMNBTUtils;
import com.feed_the_beast.ftbl.lib.util.LMServerUtils;
import com.feed_the_beast.ftbl.lib.util.LMStringUtils;
import com.feed_the_beast.ftbl.lib.util.LMUtils;
import com.feed_the_beast.ftbl.net.MessageLogin;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatList;
import net.minecraft.stats.StatisticsManagerServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;

import javax.annotation.Nullable;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by LatvianModder on 09.02.2016.
 */
public class ForgePlayer implements IForgePlayer, Comparable<ForgePlayer>
{
    private final NBTDataStorage dataStorage;
    private String teamID = "";
    private GameProfile gameProfile;
    private StatisticsManagerServer statsManager;
    private EntityPlayerMP entityPlayer;
    private NBTTagCompound playerNBT;

    public ForgePlayer(GameProfile p)
    {
        setProfile(p);
        dataStorage = FTBLibRegistries.INSTANCE.createPlayerDataStorage(this);
    }

    ForgePlayer(EntityPlayerMP ep)
    {
        this(ep.getGameProfile());
        entityPlayer = ep;
    }

    @Override
    public final String getTeamID()
    {
        return teamID;
    }

    @Override
    public final void setTeamID(String id)
    {
        teamID = id;
    }

    @Override
    @Nullable
    public final ForgeTeam getTeam()
    {
        return teamID.isEmpty() ? null : Universe.INSTANCE.getTeam(teamID);
    }

    @Override
    public final GameProfile getProfile()
    {
        if(gameProfile == null)
        {
            throw new NullPointerException("GameProfile is null!");
        }

        return gameProfile;
    }

    public final void setProfile(GameProfile p)
    {
        gameProfile = new GameProfile(p.getId(), p.getName());
    }

    @Override
    @Nullable
    public INBTData getData(ResourceLocation id)
    {
        return dataStorage == null ? null : dataStorage.get(id);
    }

    @Override
    public final int compareTo(ForgePlayer o)
    {
        return getProfile().getName().compareToIgnoreCase(o.getProfile().getName());
    }

    public final String toString()
    {
        return gameProfile.getName();
    }

    public final int hashCode()
    {
        return gameProfile.getId().hashCode();
    }

    public boolean equals(Object o)
    {
        if(o == null)
        {
            return false;
        }
        else if(o == this)
        {
            return true;
        }
        else if(o instanceof UUID)
        {
            return gameProfile.getId().equals(o);
        }
        else if(o instanceof IForgePlayer)
        {
            return equalsPlayer((IForgePlayer) o);
        }
        return equalsPlayer(Universe.INSTANCE.getPlayer(o));
    }

    @Override
    public boolean equalsPlayer(@Nullable IForgePlayer p)
    {
        return p == this || (p != null && gameProfile.getId().equals(p.getProfile().getId()));
    }

    public Map<EntityEquipmentSlot, ItemStack> getArmor()
    {
        Map<EntityEquipmentSlot, ItemStack> map = new HashMap<>();

        EntityPlayerMP ep = getPlayer();

        if(ep != null)
        {
            for(EntityEquipmentSlot e : EntityEquipmentSlot.values())
            {
                ItemStack is = ep.getItemStackFromSlot(e);

                if(is != null)
                {
                    map.put(e, is.copy());
                }
            }
        }

        return map;
    }

    @Override
    public boolean isOnline()
    {
        return getPlayer() != null;
    }

    @Override
    @Nullable
    public EntityPlayerMP getPlayer()
    {
        return entityPlayer;
    }

    @Override
    public boolean isFake()
    {
        return getPlayer() instanceof FakePlayer;
    }

    @Override
    public boolean isOP()
    {
        return LMServerUtils.isOP(getProfile());
    }

    public void getInfo(IForgePlayer owner, List<ITextComponent> info)
    {
        long ms = System.currentTimeMillis();
        IForgeTeam team = getTeam();

        if(team != null)
        {
            ITextComponent c = new TextComponentString("[" + team.getTitle() + "]");
            c.getStyle().setColor(team.getColor().getTextFormatting()).setUnderlined(true);
            info.add(c);

            if(!team.getDesc().isEmpty())
            {
                c = new TextComponentString(team.getDesc());
                c.getStyle().setColor(TextFormatting.GRAY).setItalic(true);
                info.add(c);
            }

            info.add(null);
        }

        StatisticsManagerServer stats = stats();

        if(!owner.isOnline())
        {
            long lastSeen = FTBLibStats.getLastSeen(stats, false);

            if(lastSeen > 0L)
            {
                info.add(new TextComponentTranslation(FTBLibStats.LAST_SEEN.statId).appendText(": " + LMStringUtils.getTimeString(ms - lastSeen)));
            }
        }

        long firstJoined = FTBLibStats.getFirstJoined(stats);

        if(firstJoined > 0L)
        {
            info.add(new TextComponentTranslation(FTBLibStats.FIRST_JOINED.statId).appendText(": " + LMStringUtils.getTimeString(ms - firstJoined)));
        }

        if(stats.readStat(StatList.DEATHS) > 0)
        {
            info.add(new TextComponentTranslation(StatList.DEATHS.statId).appendText(": " + stats.readStat(StatList.DEATHS)));
        }

        if(stats.readStat(StatList.PLAY_ONE_MINUTE) > 0L)
        {
            ITextComponent c = new TextComponentTranslation(StatList.PLAY_ONE_MINUTE.statId);
            info.add(c.appendSibling(new TextComponentString(": " + LMStringUtils.getTimeString(stats.readStat(StatList.PLAY_ONE_MINUTE) * 50L))));
        }

        MinecraftForge.EVENT_BUS.post(new ForgePlayerInfoEvent(this, info, ms));
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt)
    {
        setTeamID(nbt.getString("TeamID"));

        if(dataStorage != null)
        {
            dataStorage.deserializeNBT(nbt.hasKey("Caps") ? nbt.getCompoundTag("Caps") : nbt.getCompoundTag("Data"));
        }
    }

    @Override
    public NBTTagCompound serializeNBT()
    {
        NBTTagCompound tag = new NBTTagCompound();

        if(teamID != null && !teamID.isEmpty())
        {
            tag.setString("TeamID", teamID);
        }

        if(dataStorage != null)
        {
            tag.setTag("Data", dataStorage.serializeNBT());
        }

        return tag;
    }

    public void onLoggedIn(EntityPlayerMP ep, boolean firstLogin)
    {
        entityPlayer = ep;
        playerNBT = null;
        statsManager = null;
        FTBLibStats.updateLastSeen(stats());
        new MessageLogin(ep, this).sendTo(entityPlayer);
        MinecraftForge.EVENT_BUS.post(new ForgePlayerLoggedInEvent(this, firstLogin));
    }

    public void onLoggedOut()
    {
        FTBLibStats.updateLastSeen(stats());
        MinecraftForge.EVENT_BUS.post(new ForgePlayerLoggedOutEvent(this));
        entityPlayer = null;
        playerNBT = null;
        statsManager = null;
    }

    public void onDeath()
    {
        if(isOnline())
        {
            FTBLibStats.updateLastSeen(stats());
            MinecraftForge.EVENT_BUS.post(new ForgePlayerDeathEvent(this));
            statsManager = null;
        }
    }

    @Override
    public StatisticsManagerServer stats()
    {
        if(statsManager == null)
        {
            statsManager = LMServerUtils.getServer().getPlayerList().getPlayerStatsFile(entityPlayer == null ? new FakePlayer(LMServerUtils.getServerWorld(), getProfile()) : entityPlayer);
        }

        return statsManager;
    }

    @Override
    public void getSettings(IConfigTree tree)
    {
        MinecraftForge.EVENT_BUS.post(new ForgePlayerSettingsEvent(this, tree));
    }

    @Override
    public NBTTagCompound getPlayerNBT()
    {
        if(isOnline())
        {
            return getPlayer().serializeNBT();
        }

        if(playerNBT == null)
        {
            try
            {
                playerNBT = LMNBTUtils.readTag(new File(LMUtils.folderWorld, "playerdata/" + getProfile().getId() + ".dat"));
            }
            catch(Exception ex)
            {
                ex.printStackTrace();
            }
        }

        return playerNBT;
    }
}