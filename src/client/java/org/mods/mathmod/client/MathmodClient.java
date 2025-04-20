package org.mods.mathmod.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.fabricmc.api.ClientModInitializer;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
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
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MathmodClient implements ClientModInitializer {
  // Set up Configuration file for Decimal Precision
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  private static final Path CONFIG_PATH =
      FabricLoader.getInstance().getConfigDir().resolve("mathConfig.json");

  private static ModConfig config;

  // Set up Logger
  public static final Logger LOGGER = LoggerFactory.getLogger("MyModName");

  // Constant Error used in command math operations
  private static final SimpleCommandExceptionType DIVIDE_BY_ZERO =
      new SimpleCommandExceptionType(Text.literal("Cannot divide by zero."));
  private static final SimpleCommandExceptionType INVALID_EXPR =
      new SimpleCommandExceptionType(Text.literal("Invalid expression."));


  @Override
  public void onInitializeClient() {
    // Load Config from json
    config = loadConfig();

    // Load variables
    MathVariables variables = new MathVariables();

    // Register Commands
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
                                  Text.literal("Set Decimal precision to " + v)
                              );
                              return 1;
                            })
                        )
                    )
                )
                // The variable sub-command
                .then(ClientCommandManager.literal("variable")
                    // Set(create) a new variable
                    .then(ClientCommandManager.literal("set")
                        .then(ClientCommandManager.argument("name", StringArgumentType.string())
                            .then(ClientCommandManager.argument("value", StringArgumentType.string())
                                .executes(context -> {
                                  String name = StringArgumentType.getString(context, "name");
                                  String value = StringArgumentType.getString(context, "value");

                                  variables.setVariable(name, value);
                                  context.getSource().sendFeedback(Text.literal("Added variable \"" + name + "\" with value \"" + value + "\""));
                                  return 1;
                                })
                            )
                        )
                    )
                    // Get a created variable
                    .then(ClientCommandManager.literal("get")
                        .then(ClientCommandManager.argument("name", StringArgumentType.string())
                            .suggests((context, builder) -> {
                              for (Map.Entry<String, String> entry : variables.getAllVariables()) {
                                builder.suggest(entry.getKey());
                              }
                              return builder.buildFuture();
                            })
                            .executes(context -> {
                              String name = StringArgumentType.getString(context, "name");
                              if (variables.hasVariable(name)) {
                                String value = variables.getVariable(name);
                                context.getSource().sendFeedback(Text.literal(String.valueOf(value)));
                              } else {
                                throw new SimpleCommandExceptionType(Text.literal("The variable \"" + name + "\" does not exist")).create();
                              }
                              return 1;
                            })
                        )
                    )
                    // Delete a created variable
                    .then(ClientCommandManager.literal("delete")
                        .then(ClientCommandManager.argument("name", StringArgumentType.string())
                            .suggests((context, builder) -> {
                              for (Map.Entry<String, String> entry : variables.getAllVariables()) {
                                builder.suggest(entry.getKey());
                              }
                              return builder.buildFuture();
                            })
                            .executes(context -> {
                              String name = StringArgumentType.getString(context, "name");
                              if (variables.hasVariable(name)) {
                                variables.removeVariable(name);
                                context.getSource().sendFeedback(Text.literal("Removed the variable \"" + name + "\""));
                              } else {
                                throw new SimpleCommandExceptionType(Text.literal("The variable \"" + name + "\" does not exist")).create();
                              }
                              return 1;
                            })
                        )
                    )
                    // List all created variables
                    .then(ClientCommandManager.literal("list")
                        .executes(context -> {
                          if (variables.getSize() == 0) {
                            context.getSource().sendFeedback(Text.literal("There are no variables"));
                          } else {
                            for (Map.Entry<String, String> entry : variables.getAllVariables()) {
                              context.getSource().sendFeedback(Text.literal(entry.getKey() + ": " + entry.getValue()));
                            }
                          }
                          return 1;
                        })
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
                                // On parse/eval error, show red error text on chat.
                                throw INVALID_EXPR.create();
                              }

                              return 1;
                            }
                        )
                    )
                )

                // OTHER LESS USEFUL MATH FUNCTIONS
                // The absolute function (returns the positive of a number): "abs"
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
                // The arc length of a circle: "arc_length"
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

                // Gets the prime factors for a positive integer: "prime_factors"
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
                // Gets all divisors of a positive integer: "num_divisors"
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
                // The number of ways to choose k elements from n: "binomial"
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
                // sin
                .then(ClientCommandManager.literal("sin")
                    .then(ClientCommandManager.argument("angle", FloatArgumentType.floatArg())
                        .executes(context -> trigOutline(context, "sin", Math::sin))
                    )
                )
                // cos
                .then(ClientCommandManager.literal("cos")
                    .then(ClientCommandManager.argument("angle", FloatArgumentType.floatArg())
                        .executes(context -> trigOutline(context, "cos", Math::cos))
                    )
                )
                // tan
                .then(ClientCommandManager.literal("tan")
                    .then(ClientCommandManager.argument("angle", FloatArgumentType.floatArg())
                        .executes(context -> trigOutline(context, "tan", Math::tan))
                    )
                )
                // csc
                .then(ClientCommandManager.literal("csc")
                    .then(ClientCommandManager.argument("angle", FloatArgumentType.floatArg())
                        .executes(context -> trigOutline(context, "csc", (Double value) -> 1 / Math.sin(value)))
                    )
                )
                // sec
                .then(ClientCommandManager.literal("sec")
                    .then(ClientCommandManager.argument("angle", FloatArgumentType.floatArg())
                        .executes(context -> trigOutline(context, "sec", (Double value) -> 1 / Math.cos(value)))
                    )
                )
                // cot
                .then(ClientCommandManager.literal("cot")
                    .then(ClientCommandManager.argument("angle", FloatArgumentType.floatArg())
                        .executes(context -> trigOutline(context, "cot", (Double value) -> 1 / Math.tan(value)))
                    )
                )
                // sinh
                .then(ClientCommandManager.literal("sinh")
                    .then(ClientCommandManager.argument("angle", FloatArgumentType.floatArg())
                        .executes(context -> trigOutline(context, "sinh", Math::sinh))
                    )
                )
                // cosh
                .then(ClientCommandManager.literal("cosh")
                    .then(ClientCommandManager.argument("angle", FloatArgumentType.floatArg())
                        .executes(context -> trigOutline(context, "cosh", Math::cosh))
                    )
                )
                // tanh
                .then(ClientCommandManager.literal("tanh")
                    .then(ClientCommandManager.argument("angle", FloatArgumentType.floatArg())
                        .executes(context -> trigOutline(context, "tanh", Math::tanh))
                    )
                )
                // asin
                .then(ClientCommandManager.literal("asin")
                    .then(ClientCommandManager.argument("length", FloatArgumentType.floatArg())
                        .executes(context -> inverseTrigOutline(context, "asin", Math::asin))
                    )
                )
                // acos
                .then(ClientCommandManager.literal("acos")
                    .then(ClientCommandManager.argument("length", FloatArgumentType.floatArg())
                        .executes(context -> inverseTrigOutline(context, "acos", Math::acos))
                    )
                )
                // atan
                .then(ClientCommandManager.literal("atan")
                    .then(ClientCommandManager.argument("length", FloatArgumentType.floatArg())
                        .executes(context -> inverseTrigOutline(context, "atan", Math::atan))
                    )
                )
                // ln
                .then(ClientCommandManager.literal("ln")
                    .then(ClientCommandManager.argument("x", FloatArgumentType.floatArg())
                        .executes(context -> {
                          float x = FloatArgumentType.getFloat(context, "x");
                          DecimalFormat df = new DecimalFormat();
                          df.setMaximumFractionDigits(config.decimalPrecision);
                          String out = df.format(Math.log(x));
                          String x_out = df.format(x);
                          context.getSource().sendFeedback(Text.literal("ln(" + x_out + ") = " + out));
                          return 1;
                        })
                    )
                )// log10
                .then(ClientCommandManager.literal("log10")
                    .then(ClientCommandManager.argument("x", FloatArgumentType.floatArg())
                        .executes(context -> {
                          float x = FloatArgumentType.getFloat(context, "x");
                          DecimalFormat df = new DecimalFormat();
                          df.setMaximumFractionDigits(config.decimalPrecision);
                          String out = df.format(Math.log10(x));
                          String x_out = df.format(x);
                          context.getSource().sendFeedback(Text.literal("log10(" + x_out + ") = " + out));
                          return 1;
                        })
                    )
                )
                // rand
                .then(ClientCommandManager.literal("rand")
                    .executes(context -> {
                      context.getSource().sendFeedback(Text.literal("Rand: " + Math.random()));
                      return 1;
                    })
                )
                // randInt
                .then(ClientCommandManager.literal("randInt")
                    .then(ClientCommandManager.argument("x", IntegerArgumentType.integer())
                        .then(ClientCommandManager.argument("y", IntegerArgumentType.integer())
                            .executes(context -> {
                              int x = IntegerArgumentType.getInteger(context, "x");
                              int y = IntegerArgumentType.getInteger(context, "y");

                              if (x == y) {
                                context.getSource().sendFeedback(Text.literal(String.valueOf(x)));
                              } else {
                                Random rand = new Random();
                                int randInt = rand.nextInt(y - x + 1) + x;
                                context.getSource().sendFeedback(Text.literal(String.valueOf(randInt)));
                              }
                              return 1;
                            })
                        )
                    )
                )


        ));
  }

  static int trigOutline(CommandContext<FabricClientCommandSource> context, String name, Function<Double, Double> function) {
    float angle_x = FloatArgumentType.getFloat(context, "angle");
    float x = toRadians(angle_x);

    DecimalFormat df = new DecimalFormat();
    df.setMaximumFractionDigits(config.decimalPrecision);
    String out = df.format(function.apply((double) x));
    String x_out = df.format(angle_x);
    context.getSource().sendFeedback(Text.literal(name + "(" + x_out + ") = " + out));
    return 1;
  }

  static int inverseTrigOutline(CommandContext<FabricClientCommandSource> context, String name, Function<Double, Double> function) {
    float x = FloatArgumentType.getFloat(context, "length");

    DecimalFormat df = new DecimalFormat();
    df.setMaximumFractionDigits(config.decimalPrecision);
    String out = df.format(toDegrees(function.apply((double) x)));
    String x_out = df.format(x);
    context.getSource().sendFeedback(Text.literal(name + "(" + x_out + ") = " + out));
    return 1;
  }

  private static float toRadians(float degrees) {
    return (float) Math.toRadians(degrees);
  }

  private static double toDegrees(double radians) {
    return Math.toDegrees(radians);
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

