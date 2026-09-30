package com.forsaken;

import java.util.UUID;

/** Filled by the server via SyncPacket; read by the client renderer. */
public class ClientState {
    public static volatile UUID hunter = null;
}
