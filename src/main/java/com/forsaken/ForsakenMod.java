package com.forsaken;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;

@Mod(ForsakenMod.MODID)
public class ForsakenMod {
    public static final String MODID = "forsaken";

    public ForsakenMod() {
        Net.register();
        MinecraftForge.EVENT_BUS.register(new ServerEvents());
    }
}
