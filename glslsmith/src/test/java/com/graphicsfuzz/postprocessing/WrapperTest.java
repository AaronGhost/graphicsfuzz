package com.graphicsfuzz.postprocessing;

import com.graphicsfuzz.common.ast.type.BasicType;
import com.graphicsfuzz.shadergenerators.TestHelper;
import org.junit.Assert;
import org.junit.Test;

public class WrapperTest {

  @Test
  public void testGenerateVectorComparisonWrapper() {
    String ivec4returnText = "any(equal(A, ivec4(0)))";
    String uvec3returnText = "any(equal(B, uvec3(0u)))";
    Assert.assertEquals(ivec4returnText,
        TestHelper.getText(Wrapper.generateVectorComparison(BasicType.IVEC4, "A", "0")));
    Assert.assertEquals(uvec3returnText,
        TestHelper.getText(Wrapper.generateVectorComparison(BasicType.UVEC3, "B", "0")));

  }

  @Test
  public void testGenerateDivWrapper() {

    String intDivText = "int SAFE_DIV(int A, int B)\n"
        + "{\n"
        + " return B == 0 || A == -2147483648 && B == -1 ? A / 2 : A / B;\n"
        + "}\n";

    String uintDivText = "uint SAFE_DIV(uint A, uint B)\n"
        + "{\n"
        + " return B == 0u ? A / 2u : A / B;\n"
        + "}\n";
    String vec3intDivText = "ivec3 SAFE_DIV(ivec3 A, int B)\n"
        + "{\n"
        + " return B == 0 || any(equal(A, ivec3(-2147483648))) && B == -1 ? A / 2 : A / B;\n"
        + "}\n";
    String vec2uintDivText = "uvec2 SAFE_DIV(uint A, uvec2 B)\n"
        + "{\n"
        + " return any(equal(B, uvec2(0u))) ? A / uvec2(2u) : A / B;\n"
        + "}\n";
    Assert.assertEquals(TestHelper.getText(Wrapper.generateDivWrapper(BasicType.INT,
        BasicType.INT)), intDivText);
    Assert.assertEquals(TestHelper.getText(Wrapper.generateDivWrapper(BasicType.UINT,
        BasicType.UINT)), uintDivText);
    Assert.assertEquals(TestHelper.getText(Wrapper.generateDivWrapper(BasicType.IVEC3,
        BasicType.INT)), vec3intDivText);
    Assert.assertEquals(TestHelper.getText(Wrapper.generateDivWrapper(BasicType.UINT,
        BasicType.UVEC2)), vec2uintDivText);
  }

  @Test
  public void testGenerateDivAssignWrapper() {
    String uintDivAssignText = "uint SAFE_DIV_ASSIGN(inout uint A, uint B)\n"
        + "{\n"
        + " return B == 0u ? (A /= 2u) : (A /= B);\n"
        + "}\n";

    String intDivAssignText = "int SAFE_DIV_ASSIGN(inout int A, int B)\n"
        + "{\n"
        + " return B == 0 || A == -2147483648 && B == -1 ? (A /= 2) : (A /= B);\n"
        + "}\n";

    String vec4DivAssignText = "ivec4 SAFE_DIV_ASSIGN(inout ivec4 A, int B)\n"
        + "{\n"
        + " return B == 0 || any(equal(A, ivec4(-2147483648))) && B == -1 ? (A /= 2) : (A /= B);\n"
        + "}\n";
    Assert.assertEquals(TestHelper.getText(Wrapper.generateDivAssignWrapper(BasicType.INT,
        BasicType.INT)), intDivAssignText);
    Assert.assertEquals(TestHelper.getText(Wrapper.generateDivAssignWrapper(BasicType.UINT,
        BasicType.UINT)), uintDivAssignText);
    Assert.assertEquals(TestHelper.getText(Wrapper.generateDivAssignWrapper(BasicType.IVEC4,
        BasicType.INT)),
        vec4DivAssignText);
  }

  @Test
  public void testGenerateLShiftWrapper() {
    String lshiftIntIntText = "int SAFE_LSHIFT(int A, int B)\n"
        + "{\n"
        + " return B >= 32 || B < 0 ? A << 16 : A << B;\n"
        + "}\n";

    String lshiftIntUintText = "int SAFE_LSHIFT(int A, uint B)\n"
        + "{\n"
        + " return B >= 32u ? A << 16u : A << B;\n"
        + "}\n";

    String lshiftUintIntText = "uint SAFE_LSHIFT(uint A, int B)\n"
        + "{\n"
        + " return B >= 32 || B < 0 ? A << 16 : A << B;\n"
        + "}\n";

    String lshiftUintUintText = "uint SAFE_LSHIFT(uint A, uint B)\n"
        + "{\n"
        + " return B >= 32u ? A << 16u : A << B;\n"
        + "}\n";
    String lshiftVec2IntText = "ivec2 SAFE_LSHIFT(ivec2 A, int B)\n"
        + "{\n"
        + " return B >= 32 || B < 0 ? A << 16 : A << B;\n"
        + "}\n";
    String lshiftuvec3ivec3Text = "uvec3 SAFE_LSHIFT(uvec3 A, ivec3 B)\n"
        + "{\n"
        + " return any(greaterThan(B, ivec3(32))) || any(lessThan(B, ivec3(0))) ? A << ivec3(16) :"
        + " A << B;\n"
        + "}\n";
    Assert.assertEquals(TestHelper.getText(Wrapper.generateLShiftWrapper(BasicType.INT,
        BasicType.UINT)), lshiftIntUintText);
    Assert.assertEquals(TestHelper.getText(Wrapper.generateLShiftWrapper(BasicType.INT,
        BasicType.INT)), lshiftIntIntText);
    Assert.assertEquals(TestHelper.getText(Wrapper.generateLShiftWrapper(BasicType.UINT,
        BasicType.UINT)), lshiftUintUintText);
    Assert.assertEquals(TestHelper.getText(Wrapper.generateLShiftWrapper(BasicType.UINT,
        BasicType.INT)), lshiftUintIntText);
    Assert.assertEquals(TestHelper.getText(Wrapper.generateLShiftWrapper(BasicType.IVEC2,
        BasicType.INT)), lshiftVec2IntText);
    Assert.assertEquals(TestHelper.getText(Wrapper.generateLShiftWrapper(BasicType.UVEC3,
        BasicType.IVEC3)), lshiftuvec3ivec3Text);
  }

  @Test
  public void testGenerateRShiftWrapper() {
    String rshiftIntIntText = "int SAFE_RSHIFT(int A, int B)\n"
        + "{\n"
        + " return B >= 32 || B < 0 ? A >> 16 : A >> B;\n"
        + "}\n";

    String rshiftIntUintText = "int SAFE_RSHIFT(int A, uint B)\n"
        + "{\n"
        + " return B >= 32u ? A >> 16u : A >> B;\n"
        + "}\n";

    String rshiftUintIntText = "uint SAFE_RSHIFT(uint A, int B)\n"
        + "{\n"
        + " return B >= 32 || B < 0 ? A >> 16 : A >> B;\n"
        + "}\n";

    String rshiftUintUintText = "uint SAFE_RSHIFT(uint A, uint B)\n"
        + "{\n"
        + " return B >= 32u ? A >> 16u : A >> B;\n"
        + "}\n";
    Assert.assertEquals(TestHelper.getText(Wrapper.generateRShiftWrapper(BasicType.INT,
        BasicType.UINT)), rshiftIntUintText);
    Assert.assertEquals(TestHelper.getText(Wrapper.generateRShiftWrapper(BasicType.INT,
        BasicType.INT)), rshiftIntIntText);
    Assert.assertEquals(TestHelper.getText(Wrapper.generateRShiftWrapper(BasicType.UINT,
        BasicType.UINT)), rshiftUintUintText);
    Assert.assertEquals(TestHelper.getText(Wrapper.generateRShiftWrapper(BasicType.UINT,
        BasicType.INT)), rshiftUintIntText);
  }

  @Test
  public void testGenerateLShiftAssignWrapper() {

    String lshiftIntIntText = "int SAFE_LSHIFT_ASSIGN(inout int A, int B)\n"
        + "{\n"
        + " return B >= 32 || B < 0 ? (A <<= 16) : (A <<= B);\n"
        + "}\n";

    String lshiftIntUintText = "int SAFE_LSHIFT_ASSIGN(inout int A, uint B)\n"
        + "{\n"
        + " return B >= 32u ? (A <<= 16u) : (A <<= B);\n"
        + "}\n";

    String lshiftUintIntText = "uint SAFE_LSHIFT_ASSIGN(inout uint A, int B)\n"
        + "{\n"
        + " return B >= 32 || B < 0 ? (A <<= 16) : (A <<= B);\n"
        + "}\n";

    String lshiftUintUintText = "uint SAFE_LSHIFT_ASSIGN(inout uint A, uint B)\n"
        + "{\n"
        + " return B >= 32u ? (A <<= 16u) : (A <<= B);\n"
        + "}\n";

    Assert.assertEquals(TestHelper.getText(Wrapper.generateLShiftAssignWrapper(BasicType.INT,
        BasicType.UINT)),
        lshiftIntUintText);
    Assert.assertEquals(TestHelper.getText(Wrapper.generateLShiftAssignWrapper(BasicType.INT,
        BasicType.INT)), lshiftIntIntText);
    Assert.assertEquals(TestHelper.getText(Wrapper.generateLShiftAssignWrapper(BasicType.UINT,
        BasicType.UINT)), lshiftUintUintText);
    Assert.assertEquals(TestHelper.getText(Wrapper.generateLShiftAssignWrapper(BasicType.UINT,
        BasicType.INT)), lshiftUintIntText);
  }

  @Test
  public void testGenerateRShiftAssignWrapper() {

    String lshiftIntIntText = "int SAFE_RSHIFT_ASSIGN(inout int A, int B)\n"
        + "{\n"
        + " return B >= 32 || B < 0 ? (A >>= 16) : (A >>= B);\n"
        + "}\n";

    String lshiftIntUintText = "int SAFE_RSHIFT_ASSIGN(inout int A, uint B)\n"
        + "{\n"
        + " return B >= 32u ? (A >>= 16u) : (A >>= B);\n"
        + "}\n";

    String lshiftUintIntText = "uint SAFE_RSHIFT_ASSIGN(inout uint A, int B)\n"
        + "{\n"
        + " return B >= 32 || B < 0 ? (A >>= 16) : (A >>= B);\n"
        + "}\n";

    String lshiftUintUintText = "uint SAFE_RSHIFT_ASSIGN(inout uint A, uint B)\n"
        + "{\n"
        + " return B >= 32u ? (A >>= 16u) : (A >>= B);\n"
        + "}\n";

    String lshiftuvec4IntText = "uvec4 SAFE_RSHIFT_ASSIGN(inout uvec4 A, int B)\n"
        + "{\n"
        + " return B >= 32 || B < 0 ? (A >>= 16) : (A >>= B);\n"
        + "}\n";
    String lshiftuvec2ivec2Text = "uvec2 SAFE_RSHIFT_ASSIGN(inout uvec2 A, ivec2 B)\n"
        + "{\n"
        + " return any(greaterThan(B, ivec2(32))) || any(lessThan(B, ivec2(0))) ? (A >>= ivec2(16))"
        + " : (A >>= B);\n"
        + "}\n";
    Assert.assertEquals(TestHelper.getText(Wrapper.generateRShiftAssignWrapper(BasicType.INT,
        BasicType.UINT)), lshiftIntUintText);
    Assert.assertEquals(TestHelper.getText(Wrapper.generateRShiftAssignWrapper(BasicType.INT,
        BasicType.INT)), lshiftIntIntText);
    Assert.assertEquals(TestHelper.getText(Wrapper.generateRShiftAssignWrapper(BasicType.UINT,
        BasicType.UINT)), lshiftUintUintText);
    Assert.assertEquals(TestHelper.getText(Wrapper.generateRShiftAssignWrapper(BasicType.UINT,
        BasicType.INT)), lshiftUintIntText);
    Assert.assertEquals(TestHelper.getText(Wrapper.generateRShiftAssignWrapper(BasicType.UVEC4,
        BasicType.INT)), lshiftuvec4IntText);
    Assert.assertEquals(TestHelper.getText(Wrapper.generateRShiftAssignWrapper(BasicType.UVEC2,
        BasicType.IVEC2)), lshiftuvec2ivec2Text);
  }

  @Test
  public void testGenerateModWrapper() {
    String intModText = "int SAFE_MOD(int A, int B)\n"
        + "{\n"
        + " return B == 0 ? SAFE_ABS(A) % 2147483646 : SAFE_ABS(A) % SAFE_ABS(B);\n"
        + "}\n";
    String uintModText = "uint SAFE_MOD(uint A, uint B)\n"
        + "{\n"
        + " return B == 0u ? A % 2147483646u : A % B;\n"
        + "}\n";
    String uvec3uintModText = "uvec3 SAFE_MOD(uvec3 A, uint B)\n"
        + "{\n"
        + " return B == 0u ? A % 2147483646u : A % B;\n"
        + "}\n";
    String intivec4ModText = "ivec4 SAFE_MOD(int A, ivec4 B)\n"
        + "{\n"
        + " return any(equal(B, ivec4(0))) ? SAFE_ABS(A) % ivec4(2147483646)"
        + " : SAFE_ABS(A) % SAFE_ABS(B);\n"
        + "}\n";
    Assert.assertEquals(TestHelper.getText(Wrapper.generateModWrapper(BasicType.INT,
        BasicType.INT)), intModText);
    Assert.assertEquals(TestHelper.getText(Wrapper.generateModWrapper(BasicType.UINT,
        BasicType.UINT)), uintModText);
    Assert.assertEquals(TestHelper.getText(Wrapper.generateModWrapper(BasicType.UVEC3,
        BasicType.UINT)), uvec3uintModText);
    Assert.assertEquals(TestHelper.getText(Wrapper.generateModWrapper(BasicType.INT,
        BasicType.IVEC4)), intivec4ModText);
  }

  @Test
  public void testGenerateModAssignWrapper() {
    String intModAssignText = "int SAFE_MOD_ASSIGN(inout int A, int B)\n"
        + "{\n"
        + " A = SAFE_ABS(A);\n"
        + " return B == 0 ? (A %= 2147483646) : (A %= SAFE_ABS(B));\n"
        + "}\n";
    String uintModAssignText = "uint SAFE_MOD_ASSIGN(inout uint A, uint B)\n"
        + "{\n"
        + " return B == 0u ? (A %= 2147483646u) : (A %= B);\n"
        + "}\n";
    Assert.assertEquals(TestHelper.getText(Wrapper.generateModAssignWrapper(BasicType.INT,
        BasicType.INT)), intModAssignText);
    Assert.assertEquals(TestHelper.getText(Wrapper.generateModAssignWrapper(BasicType.UINT,
        BasicType.UINT)), uintModAssignText);
  }
}
