package com.forsaken.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "forsaken", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {
    /** set to true if your Herobrine skin uses slim (Alex-style, 3px) arms */
    public static final boolean SLIM_ARMS = false;

    public static final KeyMapping FOG = new KeyMapping("key.forsaken.fog", InputConstants.KEY_Z, "key.categories.forsaken");
    public static final KeyMapping SEE = new KeyMapping("key.forsaken.see", InputConstants.KEY_X, "key.categories.forsaken");
    public static final KeyMapping COME = new KeyMapping("key.forsaken.come", InputConstants.KEY_C, "key.categories.forsaken");

    public static HerobrineRenderer herobrineRenderer;

    @SubscribeEvent
    public static void onKeys(RegisterKeyMappingsEvent e) {
        e.register(FOG);
        e.register(SEE);
        e.register(COME);
    }

    @SubscribeEvent
    public static void onAddLayers(EntityRenderersEvent.AddLayers e) {
        EntityRendererProvider.Context ctx = e.getContext();
        herobrineRenderer = new HerobrineRenderer(ctx, SLIM_ARMS);
    }
}
