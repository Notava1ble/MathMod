package org.mods.mathmod.client;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ClientModInitializer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.text.Text;


public class MathmodClient implements ClientModInitializer {


  @Override
  public void onInitializeClient() {
    ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
      dispatcher.register(
              ClientCommandManager.literal("clienttater")
                      .then(ClientCommandManager.literal("greet")
                              .then(ClientCommandManager.argument("name", StringArgumentType.word())
                                      .suggests((context, builder) -> {
                                        builder.suggest("Steve");
                                        builder.suggest("Alex");
                                        return builder.buildFuture();
                                      })
                                      .executes(context -> {
                                        String name = StringArgumentType.getString(context, "name");
                                        context.getSource().sendFeedback(Text.literal("Hello, " + name + "!"));
                                        return 1;
                                      })
                              )
                      )
      );
    });
  }

}
