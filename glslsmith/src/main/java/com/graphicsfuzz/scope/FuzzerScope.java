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
  private final int firstAvailableOffset;
  private int availableShadowOffset = 0;
  private final FuzzerScope parentScope;
  private final Map<String, FuzzerScopeEntry> variableMapping = new HashMap<>();

  public boolean isGlobal() {
    return parentScope == null;
  }

  public FuzzerScope() {
    parentScope = null;
    firstAvailableOffset = 0;
  }

  public FuzzerScope(FuzzerScope parent) {
    parentScope = parent;
    firstAvailableOffset = parent.getAvailableOffset();
    availableOffset = parent.getAvailableOffset();
  }

  public FuzzerScope getParent() {
    return parentScope;
  }

  public int getFirstAvailableOffset() {
    return firstAvailableOffset;
  }

  public int getAvailableOffset() {
    return availableOffset;
  }

  public int getAvailableShadowOffset() {
    return availableShadowOffset;
  }

  public void addVariable(String name, UnifiedTypeInterface type,
                          boolean incrementOffset,
                          boolean incrementShadowOffset) {
    assert ! (incrementOffset && incrementShadowOffset);
    availableOffset += incrementOffset ? 1 : 0;
    availableShadowOffset += incrementShadowOffset ? 1 : 0;
    variableMapping.put(name, new FuzzerScopeEntry(name, type));
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

  public List<FuzzerScopeEntry> getWriteAvailableEntries() {
    return getAllDeclaredVariables().stream().filter(
        t -> !t.isReadOnly()
    ).collect(Collectors.toList());
  }

  public List<FuzzerScopeEntry> getReadEntriesOfCompatibleType(BasicType type) {
    return getAllDeclaredVariables().stream().filter(
        // Compatible type filter
        t -> (type.getNumElements() == 1) ? t.getBaseType().getElementType() == type :
            t.getBaseType().getNumElements() >= 2 && t.getBaseType().getElementType()
                == type.getElementType()
    ).filter(
        // No write only value filter
        t -> !t.isWriteOnly()
    ).collect(Collectors.toList());
  }
}
