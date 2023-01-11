package com.graphicsfuzz.glslsmith.functions;

import com.graphicsfuzz.glslsmith.scope.UnifiedTypeProxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FunctionStruct {

  public final String name;
  public final UnifiedTypeProxy returnType;
  public final List<UnifiedTypeProxy> parameterTypes;
  private boolean hasDefinition;

  private boolean hasBeenCalled;

  public FunctionStruct(String name, UnifiedTypeProxy returnType, boolean hasDefinition,
                        boolean hasBeenCalled, List<UnifiedTypeProxy> parameterTypes) {
    this.name = name;
    this.returnType = returnType;
    this.parameterTypes = parameterTypes;
    this.hasDefinition = hasDefinition;
    this.hasBeenCalled = hasBeenCalled;
  }

  public FunctionStruct(String name, UnifiedTypeProxy returnType, boolean hasDefinition,
                        boolean hasBeenCalled, UnifiedTypeProxy... parameterTypes) {
    this.name = name;
    this.returnType = returnType;
    this.parameterTypes = new ArrayList<>();
    this.parameterTypes.addAll(Arrays.asList(parameterTypes));
    this.hasDefinition = hasDefinition;
    this.hasBeenCalled = hasBeenCalled;
  }

  public boolean hasDefinition() {
    return hasDefinition;
  }

  public void hasbeenDefined() {
    hasDefinition = true;
  }

  public void isCalled() {
    hasBeenCalled = true;
  }

  public boolean wasCalled() {
    return hasBeenCalled;
  }

  @Override
  // Two functions are equal if they have the same names and the same parameters
  public boolean equals(Object obj) {
    if (obj.getClass() != FunctionStruct.class) {
      return false;
    }
    FunctionStruct other = (FunctionStruct) obj;
    if (!name.equals(other.name)) {
      return false;
    }
    if (parameterTypes.size() != other.parameterTypes.size()) {
      return false;
    }
    for (int i = 0; i < parameterTypes.size(); i++) {
      if (!parameterTypes.get(i).equals(other.parameterTypes.get(i))) {
        return false;
      }
    }
    return true;
  }

  @Override
  public String toString() {
    String parametersText = Arrays.toString(parameterTypes.toArray());
    return name + "<" + parametersText.substring(1, parametersText.length() - 1) + ">";
  }
}
