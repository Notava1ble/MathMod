package org.mods.mathmod.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MathVariables {
  private Map<String, String> variables;

  public int getSize() {
    return variables.size();
  }

  public List<Map.Entry<String, String>> getAllVariables() {
    return new ArrayList<>(variables.entrySet());
  }

  public MathVariables() {
    variables = new HashMap<>();
  }

  // Add or update a variable
  public void setVariable(String name, String value) {
    variables.put(name, value);
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
  }
}
