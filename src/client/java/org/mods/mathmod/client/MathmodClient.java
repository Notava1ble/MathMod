package org.mods.mathmod.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.brigadier.arguments.FloatArgumentType;
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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    MathVariables variables = new MathVariables();

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
                // The variable sub-command
                .then(ClientCommandManager.literal("variable")
                    .then(ClientCommandManager.argument("name", StringArgumentType.string())
                        .then(ClientCommandManager.argument("value", FloatArgumentType.floatArg())
                            .executes(context -> {
                              String name = StringArgumentType.getString(context,"name");
                              float value = FloatArgumentType.getFloat(context,"value");

                              variables.setVariable(name, value);
                              return 1;
                            })
                        )
                    )
                )
                // The "add" sub-command
                .then(ClientCommandManager.literal("add")
                    .then(ClientCommandManager.argument("x", FloatArgumentType.floatArg())
                        .suggests(CommandUtils.suggestPlayerXZ)
                        .then(ClientCommandManager.argument("y", FloatArgumentType.floatArg())
                            .suggests(CommandUtils.suggestPlayerXZ)
                            .executes(context -> {
                              float x = FloatArgumentType.getFloat(context, "x");
                              float y = FloatArgumentType.getFloat(context, "y");
                              DecimalFormat df = new DecimalFormat();
                              df.setMaximumFractionDigits(config.decimalPrecision);
                              String out = df.format((x + y));
                              String x_out = df.format(x);
                              String y_out = df.format(y);
                              context.getSource().sendFeedback(Text.literal(x_out + " + " + y_out + " = " + out));
                              return 1;
                            })
                        )
                    )
                )
                // The "subtract" sub-command
                .then(ClientCommandManager.literal("subtract")
                    .then(ClientCommandManager.argument("x", FloatArgumentType.floatArg())
                        .suggests(CommandUtils.suggestPlayerXZ)
                        .then(ClientCommandManager.argument("y", FloatArgumentType.floatArg())
                            .suggests(CommandUtils.suggestPlayerXZ)
                            .executes(context -> {
                              float x = FloatArgumentType.getFloat(context, "x");
                              float y = FloatArgumentType.getFloat(context, "y");
                              DecimalFormat df = new DecimalFormat();
                              df.setMaximumFractionDigits(config.decimalPrecision);
                              String out = df.format((x - y));
                              String x_out = df.format(x);
                              String y_out = df.format(y);
                              context.getSource().sendFeedback(Text.literal(x_out + " - " + y_out + " = " + out));
                              return 1;
                            })
                        )
                    )
                )
                // The "multiply" sub-command
                .then(ClientCommandManager.literal("multiply")
                    .then(ClientCommandManager.argument("x", FloatArgumentType.floatArg())
                        .suggests(CommandUtils.suggestPlayerXZ)
                        .then(ClientCommandManager.argument("y", FloatArgumentType.floatArg())
                            .suggests(CommandUtils.suggestPlayerXZ)
                            .executes(context -> {
                              float x = FloatArgumentType.getFloat(context, "x");
                              float y = FloatArgumentType.getFloat(context, "y");
                              DecimalFormat df = new DecimalFormat();
                              df.setMaximumFractionDigits(config.decimalPrecision);
                              String out = df.format((x * y));
                              String x_out = df.format(x);
                              String y_out = df.format(y);
                              context.getSource().sendFeedback(Text.literal(x_out + " * " + y_out + " = " + out));
                              return 1;
                            })
                        )
                    )
                )
                // The "divide" sub-command
                .then(ClientCommandManager.literal("divide")
                    .then(ClientCommandManager.argument("x", FloatArgumentType.floatArg())
                        .suggests(CommandUtils.suggestPlayerXZ)
                        .then(ClientCommandManager.argument("y", FloatArgumentType.floatArg())
                            .suggests(CommandUtils.suggestPlayerXZ)
                            .executes(context -> {
                              float x = FloatArgumentType.getFloat(context, "x");
                              float y = FloatArgumentType.getFloat(context, "y");
                              if (y == 0) {
                                throw DIVIDE_BY_ZERO.create(); // This shows a red error in chat
                              }
                              DecimalFormat df = new DecimalFormat();
                              df.setMaximumFractionDigits(config.decimalPrecision);
                              String out = df.format((x / y));
                              String x_out = df.format(x);
                              String y_out = df.format(y);
                              context.getSource().sendFeedback(Text.literal(x_out + " / " + y_out + " = " + out));
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
                // Other less useful math functions:
                // "abs"
                .then(ClientCommandManager.literal("abs")
                    .then(ClientCommandManager.argument("x", FloatArgumentType.floatArg())
                        .suggests(CommandUtils.suggestPlayerXZ)
                        .executes(context -> {
                          float x = FloatArgumentType.getFloat(context, "x");
                          DecimalFormat df = new DecimalFormat();
                          df.setMaximumFractionDigits(config.decimalPrecision);
                          String out = df.format(Math.abs(x));
                          String x_out = df.format(x);
                          context.getSource().sendFeedback(Text.literal("abs(" + x_out + ")" + " = " + out));
                          return 1;
                        })

                    )
                )
                // "arc_length"
                .then(ClientCommandManager.literal("arc_length")
                    .then(ClientCommandManager.argument("angle", FloatArgumentType.floatArg())
                        .then(ClientCommandManager.argument("radius", FloatArgumentType.floatArg())
                            .executes(context -> {
                              float angle = FloatArgumentType.getFloat(context, "angle");
                              float radius = FloatArgumentType.getFloat(context, "radius");
                              DecimalFormat df = new DecimalFormat();
                              df.setMaximumFractionDigits(config.decimalPrecision);
                              String out = df.format(2 * Math.PI * radius * (angle / 360));
                              String angle_out = df.format(angle);
                              String radius_out = df.format(radius);
                              context.getSource().sendFeedback(Text.literal("arc_length(" + angle_out + ", " + radius_out + ") = " + out));
                              return 1;
                            })
                        )
                    )
                )

                // "prime_factors"
                .then(ClientCommandManager.literal("prime_factors")
                    .then(ClientCommandManager.argument("x", IntegerArgumentType.integer(1))
                        .executes(context -> {
                          int x = IntegerArgumentType.getInteger(context, "x");
                          List<Integer> factors = new ArrayList<>();
                          for (int i = 2; i * i <= x; i++) {
                            while (x % i == 0) {
                              factors.add(i);
                              x /= i;
                            }
                          }
                          if (x > 1) {
                            factors.add(x);
                          }

                          context.getSource().sendFeedback(Text.literal(
                              "prime_factors(" + x + ") = " + factors.stream()
                                  .map(String::valueOf)
                                  .collect(Collectors.joining(", "))
                          ));
                          return 1;
                        })

                    )
                )
                // num_divisors
                .then(ClientCommandManager.literal("num_divisors")
                    .then(ClientCommandManager.argument("n", IntegerArgumentType.integer(1)) // start at 1 to avoid zero division
                        .executes(context -> {
                          int n = IntegerArgumentType.getInteger(context, "n");
                          int count = 0;
                          for (int i = 1; i * i <= n; i++) {
                            if (n % i == 0) {
                              count += (i * i == n) ? 1 : 2; // perfect square case
                            }
                          }
                          context.getSource().sendFeedback(Text.literal("num_divisors(" + n + ") = " + count));
                          return 1;
                        })
                    )
                )
                // "binomial"
                .then(ClientCommandManager.literal("binomial")
                    .then(ClientCommandManager.argument("n", IntegerArgumentType.integer(0))
                        .then(ClientCommandManager.argument("k", IntegerArgumentType.integer(0))
                            .executes(context -> {
                              int n = IntegerArgumentType.getInteger(context, "n");
                              int k = IntegerArgumentType.getInteger(context, "k");

                              if (k > n) {
                                context.getSource().sendError(Text.literal("Error: k cannot be greater than n."));
                                return 0;
                              }

                              if (k > n - k) {
                                k = n - k; // take advantage of symmetry
                              }

                              long result = 1;
                              for (int i = 1; i <= k; i++) {
                                result *= (n - i + 1);
                                result /= i;
                              }

                              context.getSource().sendFeedback(Text.literal("Binomial coefficient (" + n + " choose " + k + ") = " + result));
                              return 1;
                            })
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

