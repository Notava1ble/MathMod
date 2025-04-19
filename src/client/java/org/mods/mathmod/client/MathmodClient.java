package org.mods.mathmod.client;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.fabricmc.api.ClientModInitializer;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.text.Text;




public class MathmodClient implements ClientModInitializer {
  private static final SimpleCommandExceptionType DIVIDE_BY_ZERO =
      new SimpleCommandExceptionType(Text.literal("Cannot divide by zero."));

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
                              throw DIVIDE_BY_ZERO.create(); // 🔴 This shows a red error in chat
                            }
                            context.getSource().sendFeedback(Text.literal("Result: " + (x / y)));
                            return 1;
                          })
                      )
                  )
              )
      );
    });
  }
}

