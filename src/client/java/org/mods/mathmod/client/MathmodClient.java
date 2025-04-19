package org.mods.mathmod.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.fabricmc.api.ClientModInitializer;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.text.Text;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MathmodClient implements ClientModInitializer {
  // Config Setup
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  private static final Path CONFIG_PATH =
      FabricLoader.getInstance().getConfigDir().resolve("mathmodConfing.json");

  private static ModConfig config;

  // Logger Setup
  public static final Logger LOGGER = LoggerFactory.getLogger("MyModName");

  // Errors
  private static final SimpleCommandExceptionType DIVIDE_BY_ZERO =
      new SimpleCommandExceptionType(Text.literal("Cannot divide by zero."));
  private static final SimpleCommandExceptionType INVALID_EXPR =
      new SimpleCommandExceptionType(Text.literal("Invalid expression."));


  @Override
  public void onInitializeClient() {
    // Load Config
    config = loadConfig();

    ClientCommandRegistrationCallback.EVENT.register(
        (dispatcher, registryAccess) -> dispatcher.register(
            // Implements the "math" command
            ClientCommandManager.literal("math")
                // The "config" sub-command
                .then(ClientCommandManager.literal("config")
                    .then(ClientCommandManager.literal("precision")
                        .then(ClientCommandManager.argument("value", IntegerArgumentType.integer(0))
                            .executes(ctx -> {
                              int v = IntegerArgumentType.getInteger(ctx, "value");
                              if (v > 15) {
                                v = 15;
                              }
                              config.decimalPrecision = v;
                              saveConfig();
                              ctx.getSource().sendFeedback(
                                  Text.literal("Decimal precision set to " + v)
                              );
                              return 1;
                            })
                        )
                    )
                )
                // The "add" sub-command
                .then(ClientCommandManager.literal("add")
                    .then(ClientCommandManager.argument("x", IntegerArgumentType.integer())
                        .suggests(CommandUtils.suggestPlayerXZ)
                        .then(ClientCommandManager.argument("y", IntegerArgumentType.integer())
                            .suggests(CommandUtils.suggestPlayerXZ)
                            .executes(context -> {
                              int x = IntegerArgumentType.getInteger(context, "x");
                              int y = IntegerArgumentType.getInteger(context, "y");
                              context.getSource().sendFeedback(Text.literal("Result: " + (x + y)));
                              return 1;
                            })
                        )
                    )
                )
                // The "subtract" sub-command
                .then(ClientCommandManager.literal("subtract")
                    .then(ClientCommandManager.argument("x", IntegerArgumentType.integer())
                        .suggests(CommandUtils.suggestPlayerXZ)
                        .then(ClientCommandManager.argument("y", IntegerArgumentType.integer())
                            .suggests(CommandUtils.suggestPlayerXZ)
                            .executes(context -> {
                              int x = IntegerArgumentType.getInteger(context, "x");
                              int y = IntegerArgumentType.getInteger(context, "y");
                              context.getSource().sendFeedback(Text.literal("Result: " + (x - y)));
                              return 1;
                            })
                        )
                    )
                )
                // The "multiply" sub-command
                .then(ClientCommandManager.literal("multiply")
                    .then(ClientCommandManager.argument("x", IntegerArgumentType.integer())
                        .suggests(CommandUtils.suggestPlayerXZ)
                        .then(ClientCommandManager.argument("y", IntegerArgumentType.integer())
                            .suggests(CommandUtils.suggestPlayerXZ)
                            .executes(context -> {
                              int x = IntegerArgumentType.getInteger(context, "x");
                              int y = IntegerArgumentType.getInteger(context, "y");
                              context.getSource().sendFeedback(Text.literal("Result: " + (x * y)));
                              return 1;
                            })
                        )
                    )
                )
                // The "divide" sub-command
                .then(ClientCommandManager.literal("divide")
                    .then(ClientCommandManager.argument("x", IntegerArgumentType.integer())
                        .suggests(CommandUtils.suggestPlayerXZ)
                        .then(ClientCommandManager.argument("y", IntegerArgumentType.integer())
                            .suggests(CommandUtils.suggestPlayerXZ)
                            .executes(context -> {
                              int x = IntegerArgumentType.getInteger(context, "x");
                              int y = IntegerArgumentType.getInteger(context, "y");
                              if (y == 0) {
                                throw DIVIDE_BY_ZERO.create(); // This shows a red error in chat
                              }
                              context.getSource().sendFeedback(Text.literal("Result: " + (x / y)));
                              return 1;
                            })
                        )
                    )
                )
                // The "eval" sub-command
                .then(ClientCommandManager.literal("eval")
                    .then(ClientCommandManager.argument("expression", StringArgumentType.greedyString())
                        .executes(context -> {
                              String expr = StringArgumentType.getString(context, "expression");
                              try {
                                // Parse & evaluate
                                Expression e = new ExpressionBuilder(expr)
                                    .variables("pi", "e")
                                    .build()
                                    // Bind them to their Math constants
                                    .setVariable("pi", Math.PI)
                                    .setVariable("e", Math.E);

                                double result = e.evaluate();

                                // Format using the decimal precision in the config
                                DecimalFormat df = new DecimalFormat();
                                df.setMaximumFractionDigits(config.decimalPrecision);
                                String out = df.format(result);
                                context.getSource()
                                    .sendFeedback(Text.literal(expr + " = " + out));
                              } catch (Exception ex) {
                                // On parse/eval error, show red error text
                                throw INVALID_EXPR.create();
                              }

                              return 1;
                            }
                        )
                    )
                )
        ));
  }

  private ModConfig loadConfig() {
    // Method to load config from file
    try {
      if (Files.exists(CONFIG_PATH)) {
        return GSON.fromJson(Files.newBufferedReader(CONFIG_PATH), ModConfig.class);
      } else {
        ModConfig cfg = new ModConfig();
        Files.writeString(CONFIG_PATH, GSON.toJson(cfg));
        return cfg;
      }
    } catch (IOException e) {
      LOGGER.error("Error reading config file: {}", CONFIG_PATH, e);
      return new ModConfig(); // fallback
    }
  }

  private void saveConfig() {
    // Method to save config to file
    try {
      Files.writeString(CONFIG_PATH, GSON.toJson(config));
    } catch (IOException e) {
      LOGGER.error("Error writing config file", e);
    }
  }

}

