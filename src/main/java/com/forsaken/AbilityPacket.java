package com.forsaken;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

/** Client -> server: hunter pressed an ability key (1..3). */
public class AbilityPacket {
    public final int id;

    public AbilityPacket(int id) {
        this.id = id;
    }

    public static void encode(AbilityPacket p, FriendlyByteBuf buf) {
        buf.writeVarInt(p.id);
    }

    public static AbilityPacket decode(FriendlyByteBuf buf) {
        return new AbilityPacket(buf.readVarInt());
    }

    public static void handle(AbilityPacket p, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer sender = ctx.get().getSender();
        ctx.get().enqueueWork(() -> {
            if (sender != null) RoundManager.useAbility(sender, p.id);
        });
        ctx.get().setPacketHandled(true);
    }
}
