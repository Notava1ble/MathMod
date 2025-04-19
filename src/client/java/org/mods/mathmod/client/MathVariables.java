package org.mods.mathmod.client;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import net.fabricmc.loader.api.FabricLoader;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mods.mathmod.client.MathmodClient.LOGGER;

public class MathVariables {
  private Map<String, String> variables;
  private static final Path FILE_PATH = FabricLoader.getInstance().getConfigDir().resolve("variables.json");
  private static final Gson gson = new Gson();


  public int getSize() {
    return variables.size();
  }

  public List<Map.Entry<String, String>> getAllVariables() {
    return new ArrayList<>(variables.entrySet());
  }

  public MathVariables() {
    variables = new HashMap<>();
    loadFromFile();
  }

  // Add or update a variable
  public void setVariable(String name, String value) {
    variables.put(name, value);
    saveToFile();
  }

  // Get the value of a variable
  public String getVariable(String name) {
    return variables.get(name);
  }

  // Check if a variable exists
  public boolean hasVariable(String name) {
    return variables.containsKey(name);
  }

  // Remove a variable
  public void removeVariable(String name) {
    variables.remove(name);
    saveToFile();
  }

  // Save to JSON file
  public void saveToFile() {
    try (FileWriter writer = new FileWriter(FILE_PATH.toFile())) {
      gson.toJson(variables, writer);
    } catch (IOException e) {
      LOGGER.error("Error writing variable file", e);
    }

  }

  // Load from JSON file
  public void loadFromFile() {
    try (FileReader reader = new FileReader(FILE_PATH.toFile())) {
      Type type = new TypeToken<Map<String, String>>() {}.getType();
      variables = gson.fromJson(reader, type);
      if (variables == null) {
        variables = new HashMap<>();
      }
    } catch (IOException e) {
      LOGGER.warn("No existing variable file found. Starting fresh.");
      variables = new HashMap<>();
    }
  }

}
