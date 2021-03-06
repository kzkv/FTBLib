package com.feed_the_beast.ftbl.lib.util;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;

import javax.annotation.Nullable;

/**
 * Created by LatvianModder on 14.06.2016.
 */
public class LMTroveUtils
{
    public static void put(TIntIntMap map, int key, int value, int except)
    {
        if(value != except)
        {
            map.put(key, value);
        }
    }

    public static TIntArrayList toIntList(TIntIntMap map)
    {
        TIntArrayList list = new TIntArrayList();

        map.forEachEntry((key, value) ->
        {
            list.add(key);
            list.add(value);
            return true;
        });

        return list;
    }

    public static TIntIntHashMap fromArray(@Nullable int[] a)
    {
        TIntIntHashMap map = new TIntIntHashMap();

        if(a == null || a.length == 0)
        {
            return map;
        }

        for(int i = 0; i < a.length; i += 2)
        {
            map.put(a[i], a[i + 1]);
        }

        return map;
    }
}
