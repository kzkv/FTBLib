package com.feed_the_beast.ftbl.lib.cmd;

import com.feed_the_beast.ftbl.FTBLibLang;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public abstract class CommandLM extends CommandBase
{
    public static void checkArgs(String[] args, int i, String desc) throws CommandException
    {
        if(args == null || args.length < i)
        {
            if(desc == null || desc.isEmpty())
            {
                throw FTBLibLang.MISSING_ARGS_NUM.commandError(Integer.toString(i - (args == null ? 0 : args.length)));
            }
            else
            {
                throw FTBLibLang.MISSING_ARGS.commandError(desc);
            }
        }
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender ics)
    {
        return getRequiredPermissionLevel() == 0 || !server.isDedicatedServer() || super.checkPermission(server, ics);
    }

    @Override
    public String getCommandUsage(ICommandSender ics)
    {
        return '/' + getCommandName();
    }

    @Override
    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        if(args.length == 0)
        {
            return Collections.emptyList();
        }
        else if(isUsernameIndex(args, args.length - 1))
        {
            return getListOfStringsMatchingLastWord(args, server.getAllUsernames());
        }

        return super.getTabCompletionOptions(server, sender, args, pos);
    }

    @Override
    public boolean isUsernameIndex(String[] args, int i)
    {
        return false;
    }
}