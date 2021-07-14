package com.graphicsfuzz.scope;

import com.graphicsfuzz.common.ast.decl.ArrayInfo;
import com.graphicsfuzz.common.ast.expr.IntConstantExpr;
import com.graphicsfuzz.common.ast.type.ArrayType;
import com.graphicsfuzz.common.ast.type.BasicType;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class FuzzerScopeTest {

  FuzzerScope globalScope = new FuzzerScope();
  FuzzerScope notGlobalScope = new FuzzerScope(globalScope);
  List<String> expectedGlobalEntries = Arrays.asList("int1", "int2", "uint1", "uint2");
  List<String> expectedNonGlobalEntries = Arrays.asList("n_int1", "n_int2", "n_uint1", "n_uint2",
      "n_uvec1", "n_ivec1");

  @Before
  public void setup() {
    globalScope.addVariable("int1", new UnifiedTypeProxy(BasicType.INT), true, false);
    ArrayInfo arrayInfo1 = new ArrayInfo(Collections.singletonList(Optional.of(
        new IntConstantExpr(String.valueOf(3)))));
    arrayInfo1.setConstantSizeExpr(0, 3);
    globalScope.addVariable("int2",
        new UnifiedTypeProxy(new ArrayType(BasicType.INT, arrayInfo1)), true, false);
    globalScope.addVariable("uint1", new UnifiedTypeProxy(BasicType.UINT), true, false);
    globalScope.addVariable("uint2", new UnifiedTypeProxy(BasicType.UINT), true, false);
    notGlobalScope.addVariable("n_int1", new UnifiedTypeProxy(BasicType.INT), true, false);
    notGlobalScope.addVariable("n_int2", new UnifiedTypeProxy(BasicType.INT), true, false);
    ArrayInfo arrayInfo2 = new ArrayInfo(Collections.singletonList(Optional.of(
            new IntConstantExpr(String.valueOf(5)))));
    arrayInfo2.setConstantSizeExpr(0, 5);
    notGlobalScope.addVariable("n_uint1", new UnifiedTypeProxy(new ArrayType(BasicType.UINT,
        arrayInfo2)), true, false);
    notGlobalScope.addVariable("n_uint2", new UnifiedTypeProxy(BasicType.UINT), true, false);
    notGlobalScope.addVariable("n_uvec1", new UnifiedTypeProxy(BasicType.UVEC3), true, false);
    notGlobalScope.addVariable("n_ivec1", new UnifiedTypeProxy(BasicType.IVEC3), true, false);
  }

  @Test
  public void testIsGlobal() {
    Assert.assertTrue(globalScope.isGlobal());
    Assert.assertFalse(notGlobalScope.isGlobal());
  }

  @Test
  public void testGetParent() {
    Assert.assertEquals(notGlobalScope.getParent(), globalScope);
    Assert.assertNull(globalScope.getParent());
  }

  @Test
  public void testGetScopeEntryByName() {
    Assert.assertEquals(notGlobalScope.getScopeEntryByName("uint1").getBaseType(),
        BasicType.UINT);
    Assert.assertNull(globalScope.getScopeEntryByName("n_int2"));
    Assert.assertEquals(globalScope.getScopeEntryByName("int1").getBaseType(), BasicType.INT);

  }

  @Test
  public void testGetNameOfDeclaredVariables() {
    List<String> globalScopeEntries = globalScope.getNameOfDeclaredVariables();
    List<String> notGlobalScopeEntries = notGlobalScope.getNameOfDeclaredVariables();
    Assert.assertTrue(globalScopeEntries.size() == expectedGlobalEntries.size()
        && globalScopeEntries.containsAll(expectedGlobalEntries));
    Assert.assertTrue(notGlobalScopeEntries.size()
        == (expectedGlobalEntries.size() + expectedNonGlobalEntries.size())
        && notGlobalScopeEntries.containsAll(expectedGlobalEntries)
        && notGlobalScopeEntries.containsAll(expectedNonGlobalEntries));
  }

  @Test
  public void testGetReadEntriesOfCompatibleTypeForOneElement() {
    List<String> globalUintEntries =
        globalScope.getReadEntriesOfCompatibleType(BasicType.UINT)
        .stream().map(FuzzerScopeEntry::getName).collect(Collectors.toList());
    List<String> expectedEntries = Arrays.asList("uint1", "uint2");
    Assert.assertTrue(expectedEntries.size() == globalUintEntries.size()
        && globalUintEntries.containsAll(expectedEntries));
    List<String> notGlobalUintEntries =
        notGlobalScope.getReadEntriesOfCompatibleType(BasicType.UINT).stream()
            .map(FuzzerScopeEntry::getName).collect(Collectors.toList());
    expectedEntries = Arrays.asList("uint1", "uint2", "n_uint1", "n_uint2", "n_uvec1");
    Assert.assertTrue(expectedEntries.size() == notGlobalUintEntries.size()
        && notGlobalUintEntries.containsAll(expectedEntries));
  }

  @Test
  public void testGetReadEntriesOfCompatibleTypeForMultipleElements() {
    List<String> uvec2Entries = notGlobalScope.getReadEntriesOfCompatibleType(BasicType.UVEC2)
        .stream().map(FuzzerScopeEntry::getName).collect(Collectors.toList());
    List<String> expectedEntries = Collections.singletonList("n_uvec1");
    Assert.assertTrue(uvec2Entries.size() == expectedEntries.size()
        && uvec2Entries.containsAll(expectedEntries));
    List<String> ivec4Entries = notGlobalScope.getReadEntriesOfCompatibleType(BasicType.IVEC4)
        .stream().map(FuzzerScopeEntry::getName).collect(Collectors.toList());
    expectedEntries = Collections.singletonList("n_ivec1");
    Assert.assertTrue(ivec4Entries.size() == expectedEntries.size()
        && ivec4Entries.containsAll(expectedEntries));
  }

  @Test
  public void testgetReadEntriesOfCompatibleTypeWithArray() {
    List<String> intEntries =
        notGlobalScope.getReadEntriesOfCompatibleType(BasicType.INT)
            .stream().map(FuzzerScopeEntry::getName).collect(Collectors.toList());
    List<String> expectedEntries = Arrays.asList("int1", "int2", "n_int1", "n_int2", "n_ivec1");
    Assert.assertTrue(expectedEntries.size() == intEntries.size()
        && intEntries.containsAll(expectedEntries));
  }
}
