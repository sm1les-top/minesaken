package com.forsaken;

import java.util.*;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;

public class RoundManager {
    private static final Random RNG = new Random();

    // ---- easy-to-tweak numbers ----
    public static final int DEFAULT_ROUND_SECONDS = 300;
    /** cooldowns in seconds for abilities 1..3 (index 0 unused) */
    private static final int[] COOLDOWN_SECONDS = {0, 25, 30, 20};
    private static final int FOG_TICKS = 7 * 20;
    private static final int DARKNESS_TICKS = 6 * 20;
    private static final float COME_TO_ME_DAMAGE = 2.0f; // 2 HP = 1 heart
    // --------------------------------

    public static boolean active = false;
    public static UUID hunterId = null;
    public static HunterType hunterType = null;
    private static int ticksLeft = 0;
    private static final Set<UUID> participants = new HashSet<>();
    private static final Set<UUID> caught = new HashSet<>();
    private static final Map<Integer, Long> cooldownEnd = new HashMap<>();

    public static boolean isHunter(Entity e) {
        return active && hunterId != null && e.getUUID().equals(hunterId);
    }

    /** @return false if a round is already running or nobody is online */
    public static boolean start(MinecraftServer server, int seconds) {
        if (active) return false;
        List<ServerPlayer> players = new ArrayList<>(server.getPlayerList().getPlayers());
        if (players.isEmpty()) return false;

        ServerPlayer hunter = players.get(RNG.nextInt(players.size()));
        HunterType type = HunterType.values()[RNG.nextInt(HunterType.values().length)];

        participants.clear();
        caught.clear();
        cooldownEnd.clear();
        for (ServerPlayer p : players) participants.add(p.getUUID());

        hunterId = hunter.getUUID();
        hunterType = type;
        ticksLeft = seconds * 20;
        active = true;

        syncAll(server);

        for (ServerPlayer p : players) {
            if (p == hunter) {
                p.sendSystemMessage(Component.literal("You are the hunter: " + type.displayName
                        + "! Keys: Z / X / C").withStyle(ChatFormatting.DARK_RED));
            } else {
                p.sendSystemMessage(Component.literal("Round started! A hunter is among you. Survive!")
                        .withStyle(ChatFormatting.YELLOW));
            }
        }
        return true;
    }

    public static void end(MinecraftServer server, String reason) {
        if (!active) return;
        active = false;
        for (UUID id : participants) {
            ServerPlayer p = server.getPlayerList().getPlayer(id);
            if (p != null) {
                p.removeEffect(MobEffects.INVISIBILITY);
                p.removeEffect(MobEffects.DARKNESS);
            }
        }
        participants.clear();
        caught.clear();
        cooldownEnd.clear();
        hunterId = null;
        hunterType = null;
        syncAll(server);
        server.getPlayerList().broadcastSystemMessage(
                Component.literal(reason).withStyle(ChatFormatting.GOLD), false);
    }

    public static void tick(MinecraftServer server) {
        if (!active) return;

        ServerPlayer hunter = server.getPlayerList().getPlayer(hunterId);
        if (hunter == null) {
            end(server, "The hunter left. Round over.");
            return;
        }

        // hunter can't be hurt; heal silently (no effect/particles)
        if (hunter.getHealth() < hunter.getMaxHealth()) hunter.setHealth(hunter.getMaxHealth());

        ticksLeft--;
        if (ticksLeft <= 0) {
            end(server, "Time is up! The survivors win.");
            return;
        }

        if (ticksLeft % 20 == 0) {
            int s = ticksLeft / 20;
            Component c = Component.literal(String.format("Time left: %d:%02d", s / 60, s % 60));
            for (UUID id : participants) {
                ServerPlayer p = server.getPlayerList().getPlayer(id);
                if (p != null) p.displayClientMessage(c, true);
            }
        }
    }

    /** STUB for later: "catching" (ban-like) a survivor. Call this when the hunter catches someone. */
    public static void catchPlayer(MinecraftServer server, ServerPlayer target) {
        if (!active || target.getUUID().equals(hunterId)) return;
        caught.add(target.getUUID());
        boolean anyLeft = false;
        for (UUID id : participants) {
            if (!id.equals(hunterId) && !caught.contains(id)) anyLeft = true;
        }
        if (!anyLeft) end(server, "The hunter caught everyone! The hunter wins.");
    }

    // ---------------- abilities ----------------

    public static void useAbility(ServerPlayer hunter, int id) {
        if (!isHunter(hunter) || id < 1 || id > 3) return;
        MinecraftServer server = hunter.getServer();
        if (server == null) return;

        long now = hunter.level().getGameTime();
        long end = cooldownEnd.getOrDefault(id, 0L);
        if (now < end) {
            long left = (end - now + 19) / 20;
            hunter.displayClientMessage(Component.literal("Cooldown: " + left + "s").withStyle(ChatFormatting.GRAY), true);
            return;
        }

        boolean used = false;
        switch (id) {
            case 1 -> { // from the fog
                hunter.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, FOG_TICKS, 0, false, false, true));
                used = true;
            }
            case 2 -> { // i see you
                for (UUID uid : participants) {
                    if (uid.equals(hunterId)) continue;
                    ServerPlayer p = server.getPlayerList().getPlayer(uid);
                    if (p != null) p.addEffect(new MobEffectInstance(MobEffects.DARKNESS, DARKNESS_TICKS, 0, false, false, true));
                }
                used = true;
            }
            case 3 -> { // come to me
                ServerPlayer target = null;
                double best = Double.MAX_VALUE;
                for (UUID uid : participants) {
                    if (uid.equals(hunterId)) continue;
                    ServerPlayer p = server.getPlayerList().getPlayer(uid);
                    if (p == null || p.level() != hunter.level() || p.isSpectator()) continue;
                    double d = p.distanceToSqr(hunter);
                    if (d < best) {
                        best = d;
                        target = p;
                    }
                }
                if (target == null) {
                    hunter.displayClientMessage(Component.literal("No target").withStyle(ChatFormatting.GRAY), true);
                    return;
                }
                Vec3 dir = hunter.position().subtract(target.position());
                double dist = dir.length();
                double speed = Math.min(4.0, 0.5 + dist * 0.15);
                target.setDeltaMovement(dir.normalize().scale(speed).add(0, 0.4, 0));
                target.hurtMarked = true;
                target.hurt(target.damageSources().magic(), COME_TO_ME_DAMAGE);
                used = true;
            }
        }
        if (used) cooldownEnd.put(id, now + COOLDOWN_SECONDS[id] * 20L);
    }

    // ---------------- sync ----------------

    private static void syncAll(MinecraftServer server) {
        Net.CH.send(PacketDistributor.ALL.noArg(), new SyncPacket(active ? hunterId : null));
    }

    public static void syncTo(ServerPlayer p) {
        Net.CH.send(PacketDistributor.PLAYER.with(() -> p), new SyncPacket(active ? hunterId : null));
    }
}
