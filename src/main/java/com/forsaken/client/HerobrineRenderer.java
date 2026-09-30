package com.forsaken.client;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.resources.ResourceLocation;

public class HerobrineRenderer extends PlayerRenderer {
    public static final ResourceLocation TEXTURE =
            new ResourceLocation("forsaken", "textures/entity/herobrine.png");

    public HerobrineRenderer(EntityRendererProvider.Context ctx, boolean slimArms) {
        super(ctx, slimArms);
    }

    @Override
    public ResourceLocation getTextureLocation(AbstractClientPlayer player) {
        return TEXTURE;
    }
}
