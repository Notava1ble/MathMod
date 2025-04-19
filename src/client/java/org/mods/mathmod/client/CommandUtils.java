package org.mods.mathmod.client;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;

public class CommandUtils {
  // Gives some suggestions to the user, or more specifically, the players X and Z coordinate
  public static final SuggestionProvider<FabricClientCommandSource> suggestPlayerXZ = (context, builder) -> {
    MinecraftClient client = MinecraftClient.getInstance();

    if (client.player != null) {
      int x = (int) client.player.getX();
      int z = (int) client.player.getZ();
      builder.suggest(x);
      builder.suggest(z);
    }
    return builder.buildFuture();
  };
}
