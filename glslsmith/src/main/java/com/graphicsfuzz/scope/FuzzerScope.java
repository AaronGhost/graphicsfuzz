package com.graphicsfuzz.scope;

import com.graphicsfuzz.common.ast.type.BasicType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    availableOffset = 0;
  }

  public FuzzerScope getParent() {
    return parentScope;
  }

  public int getAvailableOffset() {
    return availableOffset;
  }

  public void incrementOffset() {
    availableOffset++;
  }

  public void addVariable(String name, UnifiedTypeInterface type) {
    this.addVariable(name, type, true);
  }

  public void addVariable(String name, UnifiedTypeInterface type, boolean canBeHidden) {
    variableMapping.put(name, new FuzzerScopeEntry(name, type, canBeHidden));
  }

  public List<String> getNameOfDeclaredVariables() {
    Set<String> names = new HashSet<>(variableMapping.keySet());
    FuzzerScope nextScope = this.parentScope;
    while (nextScope != null) {
      names.addAll(nextScope.variableMapping.keySet());
      nextScope = nextScope.parentScope;
    }
    return Collections.unmodifiableList(new ArrayList<>(names));
  }

  public List<FuzzerScopeEntry> getAllDeclaredVariables() {
    Set<FuzzerScopeEntry> declaredVariables = new HashSet<>(variableMapping.values());
    FuzzerScope nextScope = this.parentScope;
    while (nextScope != null) {
      declaredVariables.addAll(nextScope.variableMapping.values());
      nextScope = nextScope.parentScope;
    }
    return Collections.unmodifiableList(new ArrayList<>(declaredVariables));
  }

  public FuzzerScopeEntry getScopeEntryByName(String name) {
    if (variableMapping.containsKey(name)) {
      return variableMapping.get(name);
    }
    FuzzerScope nextScope = this.parentScope;
    while (nextScope != null) {
      if (nextScope.variableMapping.containsKey(name)) {
        return nextScope.variableMapping.get(name);
      }
      nextScope = nextScope.parentScope;
    }
    return null;
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
