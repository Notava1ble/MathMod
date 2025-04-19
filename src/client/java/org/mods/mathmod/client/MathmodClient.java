package org.mods.mathmod.client;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.fabricmc.api.ClientModInitializer;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.text.Text;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;



public class MathmodClient implements ClientModInitializer {
  private static final SimpleCommandExceptionType DIVIDE_BY_ZERO =
      new SimpleCommandExceptionType(Text.literal("Cannot divide by zero."));
  private static final SimpleCommandExceptionType INVALID_EXPR =
      new SimpleCommandExceptionType(Text.literal("Invalid expression."));


  @Override
  public void onInitializeClient() {
    ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
      dispatcher.register(
          ClientCommandManager.literal("math")
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
              .then(ClientCommandManager.literal("eval")
                  .then(ClientCommandManager.argument("expression", StringArgumentType.greedyString())
                      .executes(context -> {
                        String expr = StringArgumentType.getString(context, "expression");
                        try {
                          // Parse & evaluate
                          Expression e = new ExpressionBuilder(expr).build();
                          double result = e.evaluate();
                          // Send result (trim .0 for integers)
                          String out = (result == (long) result)
                              ? Long.toString((long) result)
                              : Double.toString(result);
                          context.getSource().sendFeedback(
                              Text.literal(expr + " = " + out)
                          );
                        } catch (Exception ex) {
                          // On parse/eval error, show red error text
                          throw INVALID_EXPR.create();
                        }

                        return 1;
                      })))
      );
    });
  }
}

