package com.daqem.newlife;

import com.daqem.newlife.entity.NewLifePlayerEntity;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NewLife implements ModInitializer {

    public static final String MOD_ID = "afterlife";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    public static final int MAX_LIVES = 6;
    public static final int MAX_ROLLS = 3;

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, commandRegistryAccess, registrationEnvironment) ->
                dispatcher.register(CommandManager.literal("newlife")
                        .then(CommandManager.literal("set")
                                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                                .then(CommandManager.literal("lives")
                                        .then(CommandManager.argument("player", EntityArgumentType.players())
                                                .then(CommandManager.argument("lives", IntegerArgumentType.integer(1, MAX_LIVES))
                                                        .executes(context -> setLives(
                                                                context.getSource(),
                                                                EntityArgumentType.getPlayer(context, "player"),
                                                                IntegerArgumentType.getInteger(context, "lives"))))))
//                                .then(CommandManager.literal("rolls")
//                                        .then(CommandManager.argument("player", EntityArgumentType.players())
//                                                .then(CommandManager.argument("rolls", IntegerArgumentType.integer(0, MAX_ROLLS))
//                                                        .executes(context -> setRolls(
//                                                                context.getSource(),
//                                                                EntityArgumentType.getPlayer(context, "player"),
//                                                                IntegerArgumentType.getInteger(context, "rolls")))))))
                        .then(CommandManager.literal("get")
                                .then(CommandManager.literal("lives")
                                        .then(CommandManager.argument("player", EntityArgumentType.players())
                                                .executes(context -> getLives(
                                                        context.getSource(),
                                                        EntityArgumentType.getPlayer(context, "player")))))
//                                .then(CommandManager.literal("rolls")
//                                        .then(CommandManager.argument("player", EntityArgumentType.players())
//                                                .executes(context -> getRolls(
//                                                        context.getSource(),
//                                                        EntityArgumentType.getPlayer(context, "player"))))
                        ))));

//        CommandRegistrationCallback.EVENT.register((dispatcher, commandRegistryAccess, registrationEnvironment) ->
//                dispatcher.register(CommandManager.literal("reroll")
//                        .executes(context -> reroll(context.getSource()))));
//
//        CommandRegistrationCallback.EVENT.register((dispatcher, commandRegistryAccess, registrationEnvironment) ->
//                dispatcher.register(CommandManager.literal("rerolls")
//                        .executes(context -> rerolls(context.getSource()))));
    }

//    private int rerolls(ServerCommandSource source) {
//        if (source.getEntity() instanceof NewLifePlayerEntity afterlifePlayer) {
//            int rolls = afterlifePlayer.getRerolls();
//            rolls = rolls >= 0 && rolls <= MAX_ROLLS - 1 ? MAX_ROLLS - rolls : 0;
//            source.sendFeedback(Text.of("You have " + rolls + " rerolls left."), false);
//        }
//        return 1;
//    }
//
//    private int reroll(ServerCommandSource source) {
//        if (source.getEntity() instanceof NewLifePlayerEntity afterlifePlayer) {
//            if (afterlifePlayer.getRerolls() < MAX_ROLLS) {
//                if (source.getServer() != null) {
//                    afterlifePlayer.setRerolls(afterlifePlayer.getRerolls() + 1);
//                    final Text of = Text.of(Formatting.DARK_RED + "" + Formatting.BOLD + source.getName() + Formatting.RESET + "" + Formatting.RED + " has used a reroll and received the " + Formatting.DARK_RED + "" + Formatting.BOLD + StringUtils.capitalize(afterlifePlayer.assignRandomOrigin(false).getIdentifier().toString().split(":")[1]) + Formatting.RESET + "" + Formatting.RED + " origin.");
//                    for (ServerPlayerEntity serverPlayerEntity : source.getServer().getPlayerManager().getPlayerList()) {
//                        serverPlayerEntity.sendMessage(of, false);
//                    }
//                }
//            } else {
//                source.sendFeedback(Text.of("You have no rerolls left."), false);
//            }
//        }
//        return 1;
//    }

    private int setLives(ServerCommandSource source, PlayerEntity target, int lives) {

        if (target instanceof NewLifePlayerEntity afterlifePlayer) {
            String message = "Set " + target.getName().getString() + "s lives to " + lives;
            lives = lives >= 0 && lives <= NewLife.MAX_LIVES - 1 ? NewLife.MAX_LIVES - lives : 0;
            afterlifePlayer.setLives(lives);


            if (source.getEntity() instanceof PlayerEntity sender) sender.sendMessage(Text.of(message), false);
            else NewLife.LOGGER.info(message);
        }

        return 1;
    }

    private int getLives(ServerCommandSource source, PlayerEntity target) {

        if (target instanceof NewLifePlayerEntity afterlifePlayer) {
            int lives = afterlifePlayer.getLives();
            lives = lives >= 0 && lives <= NewLife.MAX_LIVES - 1 ? NewLife.MAX_LIVES - lives : 0;
            String message = target.getName().getString() + " has " + lives + " lives left.";

            if (source.getEntity() instanceof PlayerEntity sender) sender.sendMessage(Text.of(message), false);
            else NewLife.LOGGER.info(message);
        }

        return 1;
    }

//    private int setRolls(ServerCommandSource source, PlayerEntity target, int rolls) {
//
//        if (target instanceof NewLifePlayerEntity afterlifePlayer) {
//            String message = "Set " + target.getName().getString() + "s rerolls to " + rolls;
//            rolls = rolls >= 0 && rolls <= MAX_ROLLS - 1 ? MAX_ROLLS - rolls : 0;
//            afterlifePlayer.setRerolls(rolls);
//
//
//            if (source.getEntity() instanceof PlayerEntity sender) sender.sendMessage(Text.of(message), false);
//            else NewLife.LOGGER.info(message);
//        }
//
//        return 1;
//    }
//
//    private int getRolls(ServerCommandSource source, PlayerEntity target) {
//        if (target instanceof NewLifePlayerEntity afterlifePlayer) {
//            int rolls = afterlifePlayer.getRerolls();
//            rolls = rolls >= 0 && rolls <= MAX_ROLLS - 1 ? MAX_ROLLS - rolls : 0;
//            String message = target.getName().getString() + " has " + rolls + " rerolls left.";
//
//            if (source.getEntity() instanceof PlayerEntity sender) sender.sendMessage(Text.of(message), false);
//            else NewLife.LOGGER.info(message);
//        }
//
//        return 1;
//    }
}
