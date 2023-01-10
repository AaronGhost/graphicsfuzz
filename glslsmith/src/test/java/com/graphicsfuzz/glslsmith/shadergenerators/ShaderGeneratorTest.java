package com.graphicsfuzz.glslsmith.shadergenerators;

import com.graphicsfuzz.common.ast.decl.ArrayInfo;
import com.graphicsfuzz.common.ast.expr.ArrayIndexExpr;
import com.graphicsfuzz.common.ast.expr.BoolConstantExpr;
import com.graphicsfuzz.common.ast.expr.Expr;
import com.graphicsfuzz.common.ast.expr.IntConstantExpr;
import com.graphicsfuzz.common.ast.expr.TypeConstructorExpr;
import com.graphicsfuzz.common.ast.expr.UIntConstantExpr;
import com.graphicsfuzz.common.ast.expr.VariableIdentifierExpr;
import com.graphicsfuzz.common.ast.type.ArrayType;
import com.graphicsfuzz.common.ast.type.BasicType;
import com.graphicsfuzz.common.util.CannedRandom;
import com.graphicsfuzz.common.util.IRandom;
import com.graphicsfuzz.common.util.SameValueRandom;
import com.graphicsfuzz.common.util.ZeroCannedRandom;
import com.graphicsfuzz.glslsmith.config.ParameterConfiguration;
import com.graphicsfuzz.glslsmith.random.MokeRandomTypeGenerator;
import com.graphicsfuzz.glslsmith.scope.UnifiedTypeProxy;
import com.graphicsfuzz.glslsmith.util.TestHelper;
import java.util.Collections;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Test;

public class ShaderGeneratorTest {

  class MokeShaderGenerator extends ShaderGenerator {
    public MokeShaderGenerator(IRandom random, MokeRandomTypeGenerator randomTypeGenerator) {
      super(random, randomTypeGenerator, new ParameterConfiguration.Builder().getConfig());
      resetProgramState();
    }
  }

  @Test
  public void testGenerateRandomTypedVarDeclsForInt() {
    MokeRandomTypeGenerator randomTypeGenerator = new MokeRandomTypeGenerator();
    randomTypeGenerator.setRandomNewType(new UnifiedTypeProxy(BasicType.INT));
    ShaderGenerator generator = new MokeShaderGenerator(new ZeroCannedRandom(),
        randomTypeGenerator);
    Assert.assertEquals("int var_0 = 0",
        TestHelper.getText(generator.generateRandomTypedVarDecls(1, true)));
    Assert.assertEquals("int var_1, var_2",
        TestHelper.getText(generator.generateRandomTypedVarDecls(2, false)));
  }

  @Test
  public void testGenerateRandomTypedVarDeclsForArray() {
    MokeRandomTypeGenerator randomTypeGenerator = new MokeRandomTypeGenerator();
    ArrayInfo arrayInfo = new ArrayInfo(Collections.singletonList(Optional.of(
        new IntConstantExpr("3"))));
    arrayInfo.setConstantSizeExpr(0, 3);
    randomTypeGenerator.setRandomNewType(new UnifiedTypeProxy(
        new ArrayType(BasicType.UINT, arrayInfo)));
    ShaderGenerator generator = new MokeShaderGenerator(new ZeroCannedRandom(),
        randomTypeGenerator);
    Assert.assertEquals("uint var_0[3] = uint[3](0u, 0u, 0u)",
        TestHelper.getText(generator.generateRandomTypedVarDecls(1, true)));
    Assert.assertEquals("uint var_1[3], var_2[3]",
        TestHelper.getText(generator.generateRandomTypedVarDecls(2, false)));
  }

  @Test
  public void testGenerateRandomTypedVarDeclsForVect() {
    MokeRandomTypeGenerator randomTypeGenerator = new MokeRandomTypeGenerator();
    randomTypeGenerator.setRandomNewType(new UnifiedTypeProxy(BasicType.UVEC3));
    ShaderGenerator generator = new MokeShaderGenerator(new ZeroCannedRandom(),
        randomTypeGenerator);
    Assert.assertEquals("uvec3 var_0 = uvec3(0u)",
        TestHelper.getText(generator.generateRandomTypedVarDecls(1, true)));
    MokeRandomTypeGenerator randomTypeGenerator2 = new MokeRandomTypeGenerator();
    randomTypeGenerator2.setRandomNewType(new UnifiedTypeProxy(BasicType.BVEC2));
    ShaderGenerator generator2 = new MokeShaderGenerator(new ZeroCannedRandom(),
        randomTypeGenerator2);
    Assert.assertEquals("bvec2 var_0 = bvec2(false)",
        TestHelper.getText(generator2.generateRandomTypedVarDecls(1, true)));
  }

  @Test
  public void testGenerateRandomTypedVarDeclsForVectArray() {
    MokeRandomTypeGenerator randomTypeGenerator = new MokeRandomTypeGenerator();
    ArrayInfo arrayInfo = new ArrayInfo(Collections.singletonList(Optional.of(
        new IntConstantExpr("5"))));
    arrayInfo.setConstantSizeExpr(0, 5);
    randomTypeGenerator.setRandomNewType(new UnifiedTypeProxy(new ArrayType(BasicType.IVEC4,
        arrayInfo)));
    ShaderGenerator generator = new MokeShaderGenerator(new ZeroCannedRandom(),
        randomTypeGenerator);
    Assert.assertEquals("ivec4 var_0[5] = ivec4[5](ivec4(0), ivec4(0), "
        + "ivec4(0), ivec4(0), ivec4(0))",
        TestHelper.getText(generator.generateRandomTypedVarDecls(1, true)));
    Assert.assertEquals("ivec4 var_1[5], var_2[5]",
        TestHelper.getText(generator.generateRandomTypedVarDecls(2, false)));
  }

  @Test
  public void testGenerateBaseConstantExpr() {
    ShaderGenerator generator = new MokeShaderGenerator(new ZeroCannedRandom(),
        new MokeRandomTypeGenerator());
    Assert.assertTrue(generator.generateBaseConstantExpr(BasicType.INT) instanceof IntConstantExpr);
    Assert.assertTrue(generator.generateBaseConstantExpr(BasicType.UINT)
        instanceof UIntConstantExpr);
    Assert.assertTrue(generator.generateBaseConstantExpr(BasicType.BOOL)
        instanceof BoolConstantExpr);
    Expr typeConst = generator.generateBaseConstantExpr(BasicType.UVEC2);
    Assert.assertTrue(typeConst instanceof TypeConstructorExpr);
    TypeConstructorExpr expr = (TypeConstructorExpr) typeConst;
    Assert.assertEquals("uvec2", expr.getTypename());
    Assert.assertEquals(expr.getNumArgs(), 1);
  }

  @Test
  public void testGenerateSwizzleWithOneLevel() {
    MokeRandomTypeGenerator randomTypeGenerator = new MokeRandomTypeGenerator();
    ShaderGenerator generator = new MokeShaderGenerator(new SameValueRandom(false, 1, 0L),
        randomTypeGenerator);
    Assert.assertEquals("var_0.gb",
        TestHelper.getText(generator.generateRandomSwizzle(new VariableIdentifierExpr("var_0"),
        BasicType.UVEC4, BasicType.UVEC2, true)));
    Assert.assertEquals("var_2.ggg",
        TestHelper.getText(generator.generateRandomSwizzle(new VariableIdentifierExpr("var_2"),
        BasicType.UVEC3, BasicType.UVEC3, false)));
    Assert.assertEquals("var_1[3].g", TestHelper.getText(generator.generateRandomSwizzle(
        new ArrayIndexExpr(new VariableIdentifierExpr("var_1"),
            new IntConstantExpr("3")),
        BasicType.BVEC2, BasicType.BOOL, true)));
  }

  @Test
  public void testGenerateSwizzleWithMultipleLevels() {
    MokeRandomTypeGenerator randomTypeGenerator = new MokeRandomTypeGenerator();
    ShaderGenerator generator = new MokeShaderGenerator(new CannedRandom(
        true, 3, 0, 0, 0, //outer expression
        false, 1, 0, 1, 0, //inner expression
        true, false, false //parenthesis
    ), randomTypeGenerator);
    Assert.assertEquals("(var_0).rbg.xy",
        TestHelper.getText(generator.generateRandomSwizzle(new VariableIdentifierExpr(
        "var_0"), BasicType.IVEC3, BasicType.IVEC2, true)));
  }
}
