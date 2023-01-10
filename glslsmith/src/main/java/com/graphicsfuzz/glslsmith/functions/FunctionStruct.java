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

  public FunctionStruct(String name, UnifiedTypeProxy returnType, boolean hasDefinition,
                        List<UnifiedTypeProxy> parameterTypes) {
    this.name = name;
    this.returnType = returnType;
    this.parameterTypes = parameterTypes;
    this.hasDefinition = hasDefinition;
  }

  public FunctionStruct(String name, UnifiedTypeProxy returnType, boolean hasDefinition,
                        UnifiedTypeProxy... parameterTypes) {
    this.name = name;
    this.returnType = returnType;
    this.parameterTypes = new ArrayList<>();
    this.parameterTypes.addAll(Arrays.asList(parameterTypes));
    this.hasDefinition = hasDefinition;
  }

  public boolean hasDefinition() {
    return hasDefinition;
  }

  public void hasbeenDefined() {
    hasDefinition = true;
  }

  @Override
  public String toString() {
    String parametersText = Arrays.toString(parameterTypes.toArray());
    return name + "<" + parametersText.substring(1, parametersText.length() - 1) + ">";
  }
}
