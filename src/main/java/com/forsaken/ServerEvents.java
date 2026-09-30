package com.forsaken;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.server.ServerLifecycleHooks;

public class ServerEvents {
    @SubscribeEvent
    public void onTick(TickEvent.ServerTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        MinecraftServer s = ServerLifecycleHooks.getCurrentServer();
        if (s != null) RoundManager.tick(s);
    }

    @SubscribeEvent
    public void onCommands(RegisterCommandsEvent e) {
        RoundCommands.register(e.getDispatcher());
    }

    /** The hunter can't be damaged (except /kill and the void). */
    @SubscribeEvent
    public void onAttack(LivingAttackEvent e) {
        if (RoundManager.isHunter(e.getEntity())
                && !e.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            e.setCanceled(true);
        }
    }

    /** The hunter's melee hit always deals 1 HP. */
    @SubscribeEvent
    public void onHurt(LivingHurtEvent e) {
        Entity attacker = e.getSource().getEntity();
        if (attacker != null && RoundManager.isHunter(attacker) && e.getSource().getDirectEntity() == attacker) {
            e.setAmount(1.0f);
        }
    }

    @SubscribeEvent
    public void onLogin(PlayerEvent.PlayerLoggedInEvent e) {
        if (e.getEntity() instanceof ServerPlayer sp) RoundManager.syncTo(sp);
    }
}
