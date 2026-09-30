package com.forsaken.client;

import com.forsaken.AbilityPacket;
import com.forsaken.ClientState;
import com.forsaken.Net;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "forsaken", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientForgeEvents {
    private static boolean rendering = false;

    /** Swap the hunter's skin by rendering him with our own renderer. */
    @SubscribeEvent
    public static void onRenderPlayer(RenderPlayerEvent.Pre e) {
        if (rendering) return;
        if (ClientState.hunter == null || ClientModEvents.herobrineRenderer == null) return;
        Player p = e.getEntity();
        if (!p.getUUID().equals(ClientState.hunter) || !(p instanceof AbstractClientPlayer acp)) return;

        e.setCanceled(true);
        rendering = true;
        try {
            ClientModEvents.herobrineRenderer.render(acp, acp.getYRot(), e.getPartialTick(),
                    e.getPoseStack(), e.getMultiBufferSource(), e.getPackedLight());
        } finally {
            rendering = false;
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        if (Minecraft.getInstance().player == null) return;
        while (ClientModEvents.FOG.consumeClick()) Net.CH.sendToServer(new AbilityPacket(1));
        while (ClientModEvents.SEE.consumeClick()) Net.CH.sendToServer(new AbilityPacket(2));
        while (ClientModEvents.COME.consumeClick()) Net.CH.sendToServer(new AbilityPacket(3));
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut e) {
        ClientState.hunter = null;
    }
}
