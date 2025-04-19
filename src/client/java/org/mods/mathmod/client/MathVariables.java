package org.mods.mathmod.client;

import java.util.HashMap;
import java.util.Map;

public class MathVariables {
  private Map<String, Float> variables;

  public MathVariables() {
    variables = new HashMap<>();
  }

  // Add or update a variable
  public void setVariable(String name, float value) {
    variables.put(name, value);
  }

  // Get the value of a variable
  public Float getVariable(String name) {
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
