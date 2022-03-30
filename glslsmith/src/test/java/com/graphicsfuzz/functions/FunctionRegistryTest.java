package com.graphicsfuzz.functions;

import com.graphicsfuzz.common.ast.type.BasicType;
import com.graphicsfuzz.common.util.ShaderKind;
import com.graphicsfuzz.common.util.ZeroCannedRandom;
import com.graphicsfuzz.scope.UnifiedTypeProxy;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class FunctionRegistryTest {
  FunctionRegistry registry;

  @Before
  public void setup() {
    registry = new FunctionRegistry(new ZeroCannedRandom(), ShaderKind.COMPUTE);
    FunctionStruct add_uint_uint = new FunctionStruct("add",new UnifiedTypeProxy(BasicType.UINT),
        new UnifiedTypeProxy(BasicType.UINT));
    FunctionStruct add_int_int = new FunctionStruct("add", new UnifiedTypeProxy(BasicType.INT),
        new UnifiedTypeProxy(BasicType.INT));
    registry.addUserDefinedFunction(add_int_int);
    registry.addUserDefinedFunction(add_uint_uint);
  }

  @Test
  public void testCheckIfUserFunctionExist() {
    FunctionStruct add_int_uint = new FunctionStruct("add", new UnifiedTypeProxy(BasicType.INT),
        new UnifiedTypeProxy(BasicType.UINT));
    FunctionStruct add_int_uint_uint = new FunctionStruct("add",
        new UnifiedTypeProxy(BasicType.INT),
        new UnifiedTypeProxy(BasicType.UINT), new UnifiedTypeProxy(BasicType.UINT));
    FunctionStruct mult = new FunctionStruct("mult", new UnifiedTypeProxy(BasicType.INT));
    // Check when a function with same name and parameters exist
    Assert.assertTrue(registry.checkIfUserFunctionExist(add_int_uint));
    // Check when a function with same name but different parameters exist
    Assert.assertFalse(registry.checkIfUserFunctionExist(add_int_uint_uint));
    // Check when no function with the same name exists
    Assert.assertFalse(registry.checkIfUserFunctionExist(mult));
  }
}
