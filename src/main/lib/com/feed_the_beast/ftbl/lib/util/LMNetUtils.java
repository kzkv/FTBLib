package com.feed_the_beast.ftbl.lib.util;

import com.feed_the_beast.ftbl.lib.io.ByteIOStream;
import com.google.gson.JsonElement;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import javax.annotation.Nullable;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.util.UUID;

/**
 * Created by LatvianModder on 31.07.2016.
 */
public class LMNetUtils
{
    @Nullable
    public static String getHostAddress()
    {
        try
        {
            return InetAddress.getLocalHost().getHostAddress();
        }
        catch(Exception e)
        {
            return null;
        }
    }

    @Nullable
    public static String getExternalAddress()
    {
        try
        {
            return LMStringUtils.readString(new URL("http://checkip.amazonaws.com").openStream());
        }
        catch(Exception e)
        {
            return null;
        }
    }

    // Misc //

    public static boolean openURI(URI uri) throws Exception
    {
        Class<?> oclass = Class.forName("java.awt.Desktop");
        Object object = oclass.getMethod("getDesktop", new Class[0]).invoke(null);
        oclass.getMethod("browse", new Class[] {URI.class}).invoke(object, uri);
        return true;
    }

    public static void moveBytes(InputStream is, OutputStream os, boolean close) throws Exception
    {
        byte[] buffer = new byte[1024];
        int len;
        while((len = is.read(buffer, 0, buffer.length)) > 0)
        {
            os.write(buffer, 0, len);
        }
        os.flush();

        if(close)
        {
            is.close();
            os.close();
        }
    }

    // ByteBuf fucntions //

    public static void writePos(ByteBuf io, Vec3i pos)
    {
        io.writeInt(pos.getX());
        io.writeInt(pos.getY());
        io.writeInt(pos.getZ());
    }

    public static BlockPos readPos(ByteBuf io)
    {
        int x = io.readInt();
        int y = io.readInt();
        int z = io.readInt();
        return new BlockPos(x, y, z);
    }

    public static void writeUUID(ByteBuf io, UUID id)
    {
        io.writeLong(id.getMostSignificantBits());
        io.writeLong(id.getLeastSignificantBits());
    }

    public static UUID readUUID(ByteBuf io)
    {
        long msb = io.readLong();
        long lsb = io.readLong();
        return new UUID(msb, lsb);
    }

    public static void writeString(ByteBuf io, String s)
    {
        ByteBufUtils.writeUTF8String(io, s);
    }

    public static String readString(ByteBuf io)
    {
        return ByteBufUtils.readUTF8String(io);
    }

    public static void writeResourceLocation(ByteBuf io, ResourceLocation r)
    {
        writeString(io, r.getResourceDomain());
        writeString(io, r.getResourcePath());
    }

    public static ResourceLocation readResourceLocation(ByteBuf io)
    {
        String d = readString(io);
        String p = readString(io);
        return new ResourceLocation(d, p);
    }

    public static void writeTag(ByteBuf io, @Nullable NBTTagCompound tag)
    {
        ByteBufUtils.writeTag(io, tag);
    }

    @Nullable
    public static NBTTagCompound readTag(ByteBuf io)
    {
        return ByteBufUtils.readTag(io);
    }

    public static void writeJsonElement(ByteBuf io, JsonElement e)
    {
        JsonElementIO.write(io, e);
    }

    public static JsonElement readJsonElement(ByteBuf io)
    {
        return JsonElementIO.read(io);
    }

    public static void writeCompressedByteIOStream(ByteBuf io, ByteIOStream stream)
    {
        byte[] b = stream.toCompressedByteArray();
        io.writeInt(b.length);
        io.writeBytes(b, 0, b.length);
    }

    public static ByteIOStream readCompressedByteIOStream(ByteBuf io)
    {
        byte[] b = new byte[io.readInt()];
        io.readBytes(b, 0, b.length);
        ByteIOStream stream = new ByteIOStream();
        stream.setCompressedData(b);
        return stream;
    }

    public static void writeTextComponent(ByteBuf io, ITextComponent t)
    {
        writeJsonElement(io, LMJsonUtils.serializeTextComponent(t));
    }

    public static ITextComponent readTextComponent(ByteBuf io)
    {
        return LMJsonUtils.deserializeTextComponent(readJsonElement(io));
    }

    public static void writeItemStack(ByteBuf io, ItemStack is)
    {
        ByteBufUtils.writeItemStack(io, is);
    }

    public static ItemStack readItemStack(ByteBuf io)
    {
        return ByteBufUtils.readItemStack(io);
    }
}
