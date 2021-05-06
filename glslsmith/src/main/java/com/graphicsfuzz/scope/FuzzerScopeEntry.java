package com.graphicsfuzz.scope;

import com.graphicsfuzz.common.ast.type.BasicType;
import com.graphicsfuzz.common.ast.type.Type;

public class FuzzerScopeEntry implements UnifiedTypeInterface {
  private final String name;
  private final UnifiedTypeInterface type;
  private final boolean canBeHidden;

  public FuzzerScopeEntry(String name, UnifiedTypeInterface type, boolean canBeHidden) {
    this.name = name;
    this.type = type;
    this.canBeHidden = canBeHidden;
  }

  @Override
  public String toString() {
    return name;
  }

  public String getName() {
    return name;
  }

  public boolean canBeHidden() {
    return canBeHidden;
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
