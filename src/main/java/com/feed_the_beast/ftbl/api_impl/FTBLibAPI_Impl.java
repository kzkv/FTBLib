package com.feed_the_beast.ftbl.api_impl;

import com.feed_the_beast.ftbl.FTBLibIntegrationInternal;
import com.feed_the_beast.ftbl.FTBLibLang;
import com.feed_the_beast.ftbl.FTBLibNotifications;
import com.feed_the_beast.ftbl.api.FTBLibAPI;
import com.feed_the_beast.ftbl.api.FTBLibAddon;
import com.feed_the_beast.ftbl.api.INotification;
import com.feed_the_beast.ftbl.api.ISyncData;
import com.feed_the_beast.ftbl.api.IUniverse;
import com.feed_the_beast.ftbl.api.config.IConfigContainer;
import com.feed_the_beast.ftbl.api.events.ReloadEvent;
import com.feed_the_beast.ftbl.api.events.ReloadType;
import com.feed_the_beast.ftbl.api.gui.IGuiHandler;
import com.feed_the_beast.ftbl.api.info.IInfoPage;
import com.feed_the_beast.ftbl.lib.BroadcastSender;
import com.feed_the_beast.ftbl.lib.util.ASMUtils;
import com.feed_the_beast.ftbl.lib.util.LMFileUtils;
import com.feed_the_beast.ftbl.lib.util.LMJsonUtils;
import com.feed_the_beast.ftbl.lib.util.LMNBTUtils;
import com.feed_the_beast.ftbl.lib.util.LMServerUtils;
import com.feed_the_beast.ftbl.lib.util.LMUtils;
import com.feed_the_beast.ftbl.net.MessageDisplayInfo;
import com.feed_the_beast.ftbl.net.MessageEditConfig;
import com.feed_the_beast.ftbl.net.MessageNotifyPlayer;
import com.feed_the_beast.ftbl.net.MessageNotifyPlayerCustom;
import com.feed_the_beast.ftbl.net.MessageOpenGui;
import com.feed_the_beast.ftbl.net.MessageReload;
import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.relauncher.Side;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by LatvianModder on 11.08.2016.
 */
public class FTBLibAPI_Impl implements FTBLibAPI
{
    private static PackModes packModes;
    private static SharedData sharedDataServer, sharedDataClient;
    public static boolean hasServer, isClientPlayerOP, useFTBPrefix;

    public static void init(ASMDataTable table)
    {
        FTBLibAPI api = new FTBLibAPI_Impl();
        ASMUtils.findAnnotatedObjects(table, FTBLibAPI.class, FTBLibAddon.class, (obj, field, data) -> field.set(null, api));
        ASMUtils.findAnnotatedMethods(table, FTBLibAddon.class, (method, params, data) -> method.invoke(null));

        FTBLibRegistries.INSTANCE.init(table);
    }

    @Override
    public boolean hasServer(@Nullable String id)
    {
        return (id == null || id.isEmpty()) ? hasServer : FTBLibRegistries.INSTANCE.OPTIONAL_SERVER_MODS_CLIENT.contains(id);
    }

    @Override
    public boolean isClientPlayerOP()
    {
        return isClientPlayerOP;
    }

    @Override
    public Collection<ITickable> ticking()
    {
        return TickHandler.INSTANCE.TICKABLES;
    }

    @Override
    public PackModes getPackModes()
    {
        if(packModes == null)
        {
            reloadPackModes();
        }

        return packModes;
    }

    @Override
    public SharedData getSharedData(Side side)
    {
        if(side.isServer())
        {
            if(sharedDataServer == null)
            {
                sharedDataServer = new SharedData(Side.SERVER);
            }

            return sharedDataServer;
        }
        else
        {
            if(sharedDataClient == null)
            {
                sharedDataClient = new SharedData(Side.CLIENT);
            }

            return sharedDataClient;
        }
    }

    @Override
    @Nullable
    public IUniverse getUniverse()
    {
        return Universe.INSTANCE;
    }

    @Override
    public void addServerCallback(int timer, Runnable runnable)
    {
        TickHandler.INSTANCE.addServerCallback(timer, runnable);
    }

    @Override
    public void reload(ICommandSender sender, ReloadType type)
    {
        Preconditions.checkNotNull(Universe.INSTANCE, "Can't reload yet!");
        Preconditions.checkArgument(type != ReloadType.LOGIN, "ReloadType can't be LOGIN!");

        long ms = System.currentTimeMillis();

        if(type.reload(Side.SERVER))
        {
            FTBLibRegistries.INSTANCE.reloadConfig();
            reloadPackModes();
            MinecraftForge.EVENT_BUS.post(new ReloadEvent(Side.SERVER, sender, type));
        }

        if(LMServerUtils.hasOnlinePlayers())
        {
            NBTTagCompound sharedDataTag = getSharedData(Side.SERVER).serializeNBT();

            for(EntityPlayerMP ep : LMServerUtils.getServer().getPlayerList().getPlayerList())
            {
                Map<String, NBTTagCompound> syncData = new HashMap<>();

                for(ISyncData data : FTBLibRegistries.INSTANCE.SYNCED_DATA.values())
                {
                    syncData.put(data.getID().toString(), data.writeSyncData(ep, Universe.INSTANCE.getPlayer(ep)));
                }

                new MessageReload(type, sharedDataTag, syncData).sendTo(ep);
            }
        }

        if(type.reload(Side.SERVER))
        {
            FTBLibLang.RELOAD_SERVER.printChat(BroadcastSender.INSTANCE, (System.currentTimeMillis() - ms) + "ms");
        }

        if(type == ReloadType.SERVER_ONLY_NOTIFY_CLIENT && sender instanceof EntityPlayerMP)
        {
            sendNotification((EntityPlayerMP) sender, FTBLibNotifications.RELOAD_CLIENT_CONFIG);
        }
    }

    @Override
    public void openGui(ResourceLocation guiID, EntityPlayerMP player, @Nullable NBTTagCompound data)
    {
        IGuiHandler handler = FTBLibRegistries.INSTANCE.GUIS.get(guiID);

        if(handler == null)
        {
            return;
        }

        Container c = handler.getContainer(player, data);

        player.getNextWindowId();
        player.closeContainer();

        if(c != null)
        {
            player.openContainer = c;
        }

        player.openContainer.windowId = player.currentWindowId;
        player.openContainer.addListener(player);
        new MessageOpenGui(guiID, data, player.currentWindowId).sendTo(player);
    }

    @Override
    public void sendNotification(@Nullable EntityPlayerMP player, INotification n)
    {
        short id = FTBLibRegistries.INSTANCE.NOTIFICATIONS.getIDs().getIDFromKey(n.getID() + "@" + n.getVariant());

        if(id != 0)
        {
            new MessageNotifyPlayer(id).sendTo(player);
        }
        else
        {
            new MessageNotifyPlayerCustom(n).sendTo(player);
        }
    }

    @Override
    public void editServerConfig(EntityPlayerMP player, @Nullable NBTTagCompound nbt, IConfigContainer configContainer)
    {
        new MessageEditConfig(player.getGameProfile().getId(), nbt, configContainer).sendTo(player);
    }

    @Override
    public void displayInfoGui(EntityPlayerMP player, IInfoPage page)
    {
        new MessageDisplayInfo(page).sendTo(player);
    }

    // Other Methods //

    public static void reloadPackModes()
    {
        File file = LMFileUtils.newFile(new File(LMUtils.folderModpack, "packmodes.json"));
        packModes = new PackModes(LMJsonUtils.fromJson(file));
        LMJsonUtils.toJson(file, packModes.toJsonObject());
    }

    public static void createAndLoadWorld()
    {
        try
        {
            Universe.INSTANCE = new Universe();
            Universe.INSTANCE.init();

            JsonElement worldData = LMJsonUtils.fromJson(new File(LMUtils.folderWorld, "world_data.json"));

            if(worldData.isJsonObject())
            {
                FTBLibIntegrationInternal.API.getSharedData(Side.SERVER).fromJson(worldData.getAsJsonObject());
            }

            Universe.INSTANCE.playerMap.clear();

            NBTTagCompound nbt = LMNBTUtils.readTag(new File(LMUtils.folderWorld, "data/FTBLib.dat"));

            if(nbt != null)
            {
                Universe.INSTANCE.deserializeNBT(nbt);
            }
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }

        FTBLibRegistries.INSTANCE.worldLoaded();
    }
}
