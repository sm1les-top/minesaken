package com.forsaken;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class RoundCommands {
    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("startround")
                .requires(s -> s.hasPermission(2))
                .executes(c -> start(c, RoundManager.DEFAULT_ROUND_SECONDS))
                .then(Commands.argument("seconds", IntegerArgumentType.integer(10, 3600))
                        .executes(c -> start(c, IntegerArgumentType.getInteger(c, "seconds")))));

        d.register(Commands.literal("stopround")
                .requires(s -> s.hasPermission(2))
                .executes(c -> {
                    if (!RoundManager.active) {
                        c.getSource().sendFailure(Component.literal("No round is running."));
                        return 0;
                    }
                    RoundManager.end(c.getSource().getServer(), "Round stopped by an operator.");
                    return 1;
                }));
    }

    private static int start(CommandContext<CommandSourceStack> c, int seconds) {
        if (!RoundManager.start(c.getSource().getServer(), seconds)) {
            c.getSource().sendFailure(Component.literal("Round is already running or nobody is online."));
            return 0;
        }
        return 1;
    }
}
