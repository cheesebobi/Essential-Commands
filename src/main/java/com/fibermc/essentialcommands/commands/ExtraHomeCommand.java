package com.fibermc.essentialcommands.commands;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.PermissionNode;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

public class ExtraHomeCommand {

    private static final LuckPerms luckPerms = LuckPermsProvider.get();

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("extrahome")
            .then(CommandManager.literal("add")
                .then(CommandManager.argument("player", StringArgumentType.word())
                    .then(CommandManager.argument("number", IntegerArgumentType.integer(1))
                        .executes(context -> addExtraHomes(context, StringArgumentType.getString(context, "player"), IntegerArgumentType.getInteger(context, "number"))))))
            .then(CommandManager.literal("subtract")
                .then(CommandManager.argument("player", StringArgumentType.word())
                    .then(CommandManager.argument("number", IntegerArgumentType.integer(1))
                        .executes(context -> subtractExtraHomes(context, StringArgumentType.getString(context, "player"), IntegerArgumentType.getInteger(context, "number")))))));
    }

    public static int addExtraHomes(CommandContext<ServerCommandSource> context, String playerName, int extraHomesToAdd) {
        ServerCommandSource source = context.getSource();

        // Fetch the player using their username
        ServerPlayerEntity playerEntity = source.getServer().getPlayerManager().getPlayer(playerName);
        if (playerEntity == null) {
            source.sendError(Text.literal("Player not found: " + playerName));
            return 0;
        }

        UUID playerUUID = playerEntity.getUuid();
        User user = luckPerms.getUserManager().loadUser(playerUUID).join();

        if (user == null) {
            source.sendError(Text.literal("User not found: " + playerName));
            return 0;
        }

        int currentExtraHomes = getCurrentExtraHomes(user);
        int newExtraHomes = currentExtraHomes + extraHomesToAdd;

        // Update the permission
        updateExtraHomesPermission(user, newExtraHomes);

        source.sendFeedback(Text.literal("Added " + extraHomesToAdd + " extra homes to " + playerName + ". Total extra homes: " + newExtraHomes), false);
        return 1;
    }


    public static int subtractExtraHomes(CommandContext<ServerCommandSource> context, String playerName, int extraHomesToSubtract) {
        ServerCommandSource source = context.getSource();

        // Fetch the player using their username
        ServerPlayerEntity playerEntity = source.getServer().getPlayerManager().getPlayer(playerName);
        if (playerEntity == null) {
            source.sendError(Text.literal("Player not found: " + playerName));
            return 0;
        }

        UUID playerUUID = playerEntity.getUuid();
        User user = luckPerms.getUserManager().loadUser(playerUUID).join();

        if (user == null) {
            source.sendError(Text.literal("User not found: " + playerName));
            return 0;
        }

        int currentExtraHomes = getCurrentExtraHomes(user);
        int newExtraHomes = Math.max(currentExtraHomes - extraHomesToSubtract, 0);

        // Update the permission
        updateExtraHomesPermission(user, newExtraHomes);

        source.sendFeedback(Text.literal("Subtracted " + extraHomesToSubtract + " extra homes from " + playerName + ". Total extra homes: " + newExtraHomes), false);
        return 1;
    }

    private static int getCurrentExtraHomes(User user) {
        // Get the nodes as a Collection
        Collection<Node> nodes = user.getNodes();
        for (Node node : nodes) {
            if (node instanceof PermissionNode) {
                String permission = ((PermissionNode) node).getPermission();
                if (permission.startsWith("extrahomes.")) {
                    try {
                        return Integer.parseInt(permission.substring("extrahomes.".length()));
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
        return 0;  // Default to 0 if no extrahomes.[number] permission found
    }


    private static void updateExtraHomesPermission(User user, int newExtraHomes) {
        // Remove any existing extrahomes.[number] permission
        user.data().clear(node -> node instanceof PermissionNode && ((PermissionNode) node).getPermission().startsWith("extrahomes."));

        // Add the new extrahomes.[number] permission
        PermissionNode newPermissionNode = PermissionNode.builder("extrahomes." + newExtraHomes).build();
        user.data().add(newPermissionNode);

        // Save the user
        luckPerms.getUserManager().saveUser(user);
    }


}
