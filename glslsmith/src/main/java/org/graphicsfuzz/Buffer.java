package org.graphicsfuzz;

import com.graphicsfuzz.common.ast.type.LayoutQualifierSequence;
import com.graphicsfuzz.common.ast.type.Type;
import com.graphicsfuzz.common.ast.type.TypeQualifier;
import java.util.List;

public class Buffer implements Symbol {
  private String bufferName;
  private LayoutQualifierSequence layoutQualifiers;
  private List<? extends Number> values;
  private TypeQualifier interfaceQualifier;
  private List<String> memberNames;
  private List<Type> memberTypes;
  private String instanceName;
  private boolean input;
  private int binding;

  //TODO check if the constructor is correct
  public Buffer(String bufferName, LayoutQualifierSequence layoutQualifiers,
                List<? extends Number> values, TypeQualifier interfaceQualifier,
                List<String> memberNames, List<Type> memberTypes, String instanceName,
                boolean input, int binding) {
    this.layoutQualifiers = layoutQualifiers;
    this.bufferName = bufferName;
    this.values = values;
    this.interfaceQualifier = interfaceQualifier;
    this.memberNames = memberNames;
    this.memberTypes = memberTypes;
    this.instanceName = instanceName;
    this.input = input;
    this.binding = binding;
  }

  @Override
  public String getName() {
    return bufferName;
  }

  @Override
  public String getType() {
    return "Buffer";
  }

  public TypeQualifier getInterfaceQualifier() {
    return interfaceQualifier;
  }

  public LayoutQualifierSequence getLayoutQualifiers() {
    return layoutQualifiers;
  }

  public int getLength() {
    return values.size();
  }

  public List<String> getMemberNames() {
    return memberNames;
  }

  public List<Type> getMemberTypes() {
    return memberTypes;
  }

  public String getInstanceName() {
    return instanceName;
  }

  public List<? extends Number> getValues() {
    return values;
  }

  public boolean isInput() {
    return input;
  }

  public int getBinding() {
    return binding;
  }

}
