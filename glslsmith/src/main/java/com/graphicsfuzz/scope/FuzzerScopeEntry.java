package com.graphicsfuzz.scope;

import com.graphicsfuzz.common.ast.type.BasicType;
import com.graphicsfuzz.common.ast.type.Type;

public class FuzzerScopeEntry implements UnifiedTypeInterface {
  private final String name;
  private final UnifiedTypeInterface type;

  public FuzzerScopeEntry(String name, UnifiedTypeInterface type) {
    this.name = name;
    this.type = type;
  }

  @Override
  public String toString() {
    return name;
  }

  public String getName() {
    return name;
  }


  @Override
  public Type getRealType() {
    return type.getRealType();
  }

  @Override
  public UnifiedTypeInterface getChildType() {
    return type.getChildType();
  }

  @Override
  public BasicType getBaseType() {
    return type.getBaseType();
  }

  @Override
  public int getCurrentTypeSize() {
    return type.getCurrentTypeSize();
  }

  @Override
  public int getBaseTypeSize() {
    return type.getBaseTypeSize();
  }

  @Override
  public int getElementSize() {
    return type.getElementSize();
  }

  public boolean isArray() {
    return type.isArray();
  }

  @Override
  public boolean isReadOnly() {
    return type.isReadOnly();
  }

  @Override
  public boolean isCoherent() {
    return type.isCoherent();
  }

  @Override
  public boolean isWriteOnly() {
    return type.isWriteOnly();
  }

  //A fuzzer scope entry can never be void
  @Override
  public boolean isVoid() {
    return false;
  }

  //A fuzzer scope entry can never be a out param
  @Override
  public boolean isOut() {
    return false;
  }

  @Override
  public boolean isConstOnly() {
    return type.isConstOnly();
  }

  @Override
  public void setReadOnly(boolean readOnly) {
    type.setReadOnly(readOnly);
  }

  @Override
  public void setWriteOnly(boolean writeOnly) {
    type.setWriteOnly(writeOnly);
  }

  @Override
  public void setCoherent(boolean coherent) {
    type.setCoherent(coherent);
  }

  public int getSize() {
    return type.getBaseTypeSize();
  }

  @Override
  public boolean equals(Object other) {
    if (other instanceof FuzzerScopeEntry) {
      return ((FuzzerScopeEntry) other).getName().equals(this.name);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return this.name.hashCode();
  }

}
