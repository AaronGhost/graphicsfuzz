package com.graphicsfuzz.scope;

import com.graphicsfuzz.common.ast.decl.ArrayInfo;
import com.graphicsfuzz.common.ast.expr.IntConstantExpr;
import com.graphicsfuzz.common.ast.type.ArrayType;
import com.graphicsfuzz.common.ast.type.BasicType;
import java.util.Collections;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Test;

public class FuzzerScopeEntryTest {

  @Test
  public void testFuzzerScopeEntryForBasicType() {
    FuzzerScopeEntry entry = new FuzzerScopeEntry("int1", new UnifiedTypeProxy(BasicType.INT));
    Assert.assertEquals(entry.getRealType(), BasicType.INT);
    Assert.assertEquals(entry.getBaseType(), BasicType.INT);
    Assert.assertFalse(entry.isArray());
    Assert.assertEquals(entry.getSize(), 1);
  }

  @Test
  public void testFuzzerScopeEntryForArrayType() {
    ArrayInfo arrayInfo = new ArrayInfo(Collections.singletonList(Optional.of(
        new IntConstantExpr(String.valueOf(5)))));
    arrayInfo.setConstantSizeExpr(0, 5);
    FuzzerScopeEntry entry = new FuzzerScopeEntry("int1",
        new UnifiedTypeProxy(new ArrayType(BasicType.UINT, arrayInfo)));
    Assert.assertEquals(entry.getRealType().getClass(), ArrayType.class);
    Assert.assertEquals(entry.getBaseType(), BasicType.UINT);
    Assert.assertTrue(entry.isArray());
    Assert.assertEquals(entry.getSize(), 5);
  }
}
