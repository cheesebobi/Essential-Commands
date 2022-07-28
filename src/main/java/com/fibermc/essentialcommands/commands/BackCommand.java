package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.ECText;
import com.fibermc.essentialcommands.PlayerData;
import com.fibermc.essentialcommands.PlayerTeleporter;
import com.fibermc.essentialcommands.TextFormatType;
import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import com.fibermc.essentialcommands.types.MinecraftLocation;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class BackCommand implements Command<ServerCommandSource> {

    public BackCommand() {}

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        //Store command sender
        ServerPlayerEntity player = context.getSource().getPlayer();
        PlayerData playerData = ((ServerPlayerEntityAccess) player).ec$getPlayerData();

        //Get previous location
        MinecraftLocation loc = playerData.getPreviousLocation();

        //chat message
        if (loc == null) {
            context.getSource().sendError(
                ECText.getInstance().getText("cmd.back.error.no_prev_location", TextFormatType.Error)
            );
            return 0;
        }

        //Teleport player to home location
        PlayerTeleporter.requestTeleport(playerData, loc, ECText.getInstance().getText("cmd.back.location_name"));

        return 1;
    }
}
