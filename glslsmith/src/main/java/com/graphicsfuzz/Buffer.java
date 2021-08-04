package com.graphicsfuzz;

import com.graphicsfuzz.common.ast.type.LayoutQualifierSequence;
import com.graphicsfuzz.common.ast.type.Type;
import com.graphicsfuzz.common.ast.type.TypeQualifier;
import java.util.List;
import java.util.ListIterator;

public class Buffer {
  //Buffer elements to generate the interface block
  private final String bufferName;
  private final LayoutQualifierSequence layoutQualifiers;
  private final TypeQualifier interfaceQualifier;
  private final List<String> memberNames;
  private final List<Type> memberTypes;
  private final String instanceName;
  private final int binding;
  //Wrapper related variables
  private final List<? extends Number> values;
  private final boolean input;

  //TODO add test for memberType maybe
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

  public boolean memberExist(String name) {
    return memberNames.contains(name);
  }

  public Type getMemberType(String name) {
    assert memberExist(name);
    ListIterator<Type> typeIterator = memberTypes.listIterator();
    for (String memberName : memberNames) {
      Type memberType = typeIterator.next();
      if (memberName.equals(name)) {
        return memberType;
      }
    }
    return null;
  }

  public String getName() {
    return bufferName;
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
