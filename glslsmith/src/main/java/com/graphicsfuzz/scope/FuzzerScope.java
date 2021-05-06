package com.graphicsfuzz.scope;

import com.graphicsfuzz.common.ast.type.BasicType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FuzzerScope {

  private int availableOffset = 0;
  private final FuzzerScope parentScope;
  private final Map<String, FuzzerScopeEntry> variableMapping = new HashMap<>();

  public boolean isGlobal() {
    return parentScope == null;
  }

  public FuzzerScope() {
    parentScope = null;
  }

  public FuzzerScope(FuzzerScope parent) {
    parentScope = parent;
    availableOffset = parent.getAvailableOffset();
  }

  public FuzzerScope getParent() {
    return parentScope;
  }

  public int getAvailableOffset() {
    return availableOffset;
  }

  public void addVariable(String name, UnifiedTypeInterface type) {
    this.addVariable(name, type, true);
  }

  public void addVariable(String name, UnifiedTypeInterface type, boolean canBeHidden) {
    variableMapping.put(name, new FuzzerScopeEntry(name, type, canBeHidden));
  }

  public List<String> getNameOfDeclaredVariables() {
    if (parentScope == null) {
      return new ArrayList<>(variableMapping.keySet());
    }
    List<String> parentVariables = parentScope.getNameOfDeclaredVariables();
    parentVariables.addAll(variableMapping.keySet());
    return parentVariables;
  }

  public List<FuzzerScopeEntry> getAllDeclaredVariables() {
    if (parentScope == null) {
      return new ArrayList<>(variableMapping.values());
    }
    List<FuzzerScopeEntry> parentVariables = parentScope.getAllDeclaredVariables();
    parentVariables.addAll(variableMapping.values());
    return parentVariables;
  }

  //TODO add readonly and const support
  public List<FuzzerScopeEntry> getWriteAvailableEntries() {
    return getAllDeclaredVariables();
  }

  //TODO add writeonly support
  public List<FuzzerScopeEntry> getReadEntriesOfCompatibleType(BasicType type) {
    return getAllDeclaredVariables().stream().filter(
        t -> (type.getNumElements() == 1) ? t.getBaseType().getElementType() == type :
            t.getBaseType().getNumElements() >= 2 && t.getBaseType().getElementType()
                == type.getElementType()
    ).collect(Collectors.toList());
  }
}
