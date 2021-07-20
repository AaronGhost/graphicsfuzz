package com.graphicsfuzz.shadergenerators;

import com.graphicsfuzz.scope.UnifiedTypeProxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FunctionStruct {

  public final String name;
  public final UnifiedTypeProxy returnType;
  public final List<UnifiedTypeProxy> parameterTypes;

  public FunctionStruct(String name, UnifiedTypeProxy returnType,
                        List<UnifiedTypeProxy> parameterTypes) {
    this.name = name;
    this.returnType = returnType;
    this.parameterTypes = parameterTypes;
  }

  public FunctionStruct(String name, UnifiedTypeProxy returnType,
                        UnifiedTypeProxy... parameterTypes) {
    this.name = name;
    this.returnType = returnType;
    this.parameterTypes = new ArrayList<>();
    this.parameterTypes.addAll(Arrays.asList(parameterTypes));
  }

  @Override
  public String toString() {
    String parametersText = Arrays.toString(parameterTypes.toArray());
    return name + "<" + parametersText.substring(1, parametersText.length() - 1) + ">";
  }
}
