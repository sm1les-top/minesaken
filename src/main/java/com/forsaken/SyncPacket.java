package com.forsaken;

import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

/** Server -> client: who is the hunter right now (null = no round). */
public class SyncPacket {
    public final UUID hunter;

    public SyncPacket(UUID hunter) {
        this.hunter = hunter;
    }

    public static void encode(SyncPacket p, FriendlyByteBuf buf) {
        buf.writeBoolean(p.hunter != null);
        if (p.hunter != null) buf.writeUUID(p.hunter);
    }

    public static SyncPacket decode(FriendlyByteBuf buf) {
        return new SyncPacket(buf.readBoolean() ? buf.readUUID() : null);
    }

    public static void handle(SyncPacket p, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ClientState.hunter = p.hunter);
        ctx.get().setPacketHandled(true);
    }
}
