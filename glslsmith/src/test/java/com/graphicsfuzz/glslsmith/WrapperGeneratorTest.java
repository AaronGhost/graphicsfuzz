package com.graphicsfuzz.glslsmith;

import com.graphicsfuzz.common.ast.type.BasicType;
import com.graphicsfuzz.glslsmith.config.ConfigInterface;
import com.graphicsfuzz.glslsmith.postprocessing.WrapperGenerator;
import com.graphicsfuzz.glslsmith.util.TestHelper;
import org.junit.Assert;
import org.junit.Test;

public class WrapperGeneratorTest {

  @Test
  public void testGenerateVectorComparisonWrapper() {
    String ivec4returnText = "any(equal(A, ivec4(0)))";
    String uvec3returnText = "any(equal(B, uvec3(0u)))";

    Assert.assertEquals(ivec4returnText,
        TestHelper.getText(WrapperGenerator.generateVectorComparison(BasicType.IVEC4, "A", "0")));
    Assert.assertEquals(uvec3returnText,
        TestHelper.getText(WrapperGenerator.generateVectorComparison(BasicType.UVEC3, "B", "0")));

  }

  @Test
  public void testGenerateAbsWrapper() {
    System.out.println(TestHelper.getText(WrapperGenerator.generateDivWrapper(BasicType.INT,
        BasicType.INT, ConfigInterface.RunType.STANDARD)));
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

    Assert.assertEquals(TestHelper.getText(WrapperGenerator.generateDivWrapper(BasicType.INT,
        BasicType.INT, ConfigInterface.RunType.STANDARD)), intDivText);
    Assert.assertEquals(TestHelper.getText(WrapperGenerator.generateDivWrapper(BasicType.UINT,
        BasicType.UINT, ConfigInterface.RunType.STANDARD)), uintDivText);
    Assert.assertEquals(TestHelper.getText(WrapperGenerator.generateDivWrapper(BasicType.IVEC3,
        BasicType.INT, ConfigInterface.RunType.STANDARD)), vec3intDivText);
    Assert.assertEquals(TestHelper.getText(WrapperGenerator.generateDivWrapper(BasicType.UINT,
        BasicType.UVEC2, ConfigInterface.RunType.STANDARD)), vec2uintDivText);
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

    Assert.assertEquals(TestHelper.getText(WrapperGenerator.generateDivAssignWrapper(BasicType.INT,
        BasicType.INT, ConfigInterface.RunType.STANDARD)), intDivAssignText);
    Assert.assertEquals(TestHelper.getText(WrapperGenerator.generateDivAssignWrapper(BasicType.UINT,
        BasicType.UINT, ConfigInterface.RunType.STANDARD)), uintDivAssignText);
    Assert.assertEquals(TestHelper.getText(WrapperGenerator.generateDivAssignWrapper(
        BasicType.IVEC4,
        BasicType.INT, ConfigInterface.RunType.STANDARD)),
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

    Assert.assertEquals(TestHelper.getText(WrapperGenerator.generateLShiftWrapper(BasicType.INT,
        BasicType.UINT, ConfigInterface.RunType.STANDARD)), lshiftIntUintText);
    Assert.assertEquals(TestHelper.getText(WrapperGenerator.generateLShiftWrapper(BasicType.INT,
        BasicType.INT, ConfigInterface.RunType.STANDARD)), lshiftIntIntText);
    Assert.assertEquals(TestHelper.getText(WrapperGenerator.generateLShiftWrapper(BasicType.UINT,
        BasicType.UINT, ConfigInterface.RunType.STANDARD)), lshiftUintUintText);
    Assert.assertEquals(TestHelper.getText(WrapperGenerator.generateLShiftWrapper(BasicType.UINT,
        BasicType.INT, ConfigInterface.RunType.STANDARD)), lshiftUintIntText);
    Assert.assertEquals(TestHelper.getText(WrapperGenerator.generateLShiftWrapper(BasicType.IVEC2,
        BasicType.INT, ConfigInterface.RunType.STANDARD)), lshiftVec2IntText);
    Assert.assertEquals(TestHelper.getText(WrapperGenerator.generateLShiftWrapper(BasicType.UVEC3,
        BasicType.IVEC3, ConfigInterface.RunType.STANDARD)), lshiftuvec3ivec3Text);
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

    Assert.assertEquals(TestHelper.getText(WrapperGenerator.generateRShiftWrapper(BasicType.INT,
        BasicType.UINT, ConfigInterface.RunType.STANDARD)), rshiftIntUintText);
    Assert.assertEquals(TestHelper.getText(WrapperGenerator.generateRShiftWrapper(BasicType.INT,
        BasicType.INT, ConfigInterface.RunType.STANDARD)), rshiftIntIntText);
    Assert.assertEquals(TestHelper.getText(WrapperGenerator.generateRShiftWrapper(BasicType.UINT,
        BasicType.UINT, ConfigInterface.RunType.STANDARD)), rshiftUintUintText);
    Assert.assertEquals(TestHelper.getText(WrapperGenerator.generateRShiftWrapper(BasicType.UINT,
        BasicType.INT, ConfigInterface.RunType.STANDARD)), rshiftUintIntText);
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

    Assert.assertEquals(TestHelper.getText(WrapperGenerator.generateLShiftAssignWrapper(
        BasicType.INT,
        BasicType.UINT, ConfigInterface.RunType.STANDARD)),
        lshiftIntUintText);
    Assert.assertEquals(TestHelper.getText(WrapperGenerator.generateLShiftAssignWrapper(
        BasicType.INT,
        BasicType.INT, ConfigInterface.RunType.STANDARD)), lshiftIntIntText);
    Assert.assertEquals(TestHelper.getText(WrapperGenerator.generateLShiftAssignWrapper(
        BasicType.UINT,
        BasicType.UINT, ConfigInterface.RunType.STANDARD)), lshiftUintUintText);
    Assert.assertEquals(TestHelper.getText(WrapperGenerator.generateLShiftAssignWrapper(
        BasicType.UINT,
        BasicType.INT, ConfigInterface.RunType.STANDARD)), lshiftUintIntText);
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

    Assert.assertEquals(TestHelper.getText(WrapperGenerator.generateRShiftAssignWrapper(
        BasicType.INT,
        BasicType.UINT, ConfigInterface.RunType.STANDARD)), lshiftIntUintText);
    Assert.assertEquals(TestHelper.getText(WrapperGenerator.generateRShiftAssignWrapper(
        BasicType.INT,
        BasicType.INT, ConfigInterface.RunType.STANDARD)), lshiftIntIntText);
    Assert.assertEquals(TestHelper.getText(WrapperGenerator.generateRShiftAssignWrapper(
        BasicType.UINT,
        BasicType.UINT, ConfigInterface.RunType.STANDARD)), lshiftUintUintText);
    Assert.assertEquals(TestHelper.getText(WrapperGenerator.generateRShiftAssignWrapper(
        BasicType.UINT,
        BasicType.INT, ConfigInterface.RunType.STANDARD)), lshiftUintIntText);
    Assert.assertEquals(TestHelper.getText(WrapperGenerator.generateRShiftAssignWrapper(
        BasicType.UVEC4,
        BasicType.INT, ConfigInterface.RunType.STANDARD)), lshiftuvec4IntText);
    Assert.assertEquals(TestHelper.getText(WrapperGenerator.generateRShiftAssignWrapper(
        BasicType.UVEC2,
        BasicType.IVEC2, ConfigInterface.RunType.STANDARD)), lshiftuvec2ivec2Text);
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

    Assert.assertEquals(TestHelper.getText(WrapperGenerator.generateModWrapper(BasicType.INT,
        BasicType.INT, ConfigInterface.RunType.STANDARD)), intModText);
    Assert.assertEquals(TestHelper.getText(WrapperGenerator.generateModWrapper(BasicType.UINT,
        BasicType.UINT, ConfigInterface.RunType.STANDARD)), uintModText);
    Assert.assertEquals(TestHelper.getText(WrapperGenerator.generateModWrapper(BasicType.UVEC3,
        BasicType.UINT, ConfigInterface.RunType.STANDARD)), uvec3uintModText);
    Assert.assertEquals(TestHelper.getText(WrapperGenerator.generateModWrapper(BasicType.INT,
        BasicType.IVEC4, ConfigInterface.RunType.STANDARD)), intivec4ModText);
  }

  @Test
  public void testGenerateModAssignWrapper() {
    String intModAssignText = "int SAFE_MOD_ASSIGN(inout int A, int B)\n"
        + "{\n"
        + " A = SAFE_ABS(A);\n"
        + " int tmpB = SAFE_ABS(B);\n"
        + " return B == 0 ? (A %= 2147483646) : (A %= tmpB);\n"
        + "}\n";
    String uintModAssignText = "uint SAFE_MOD_ASSIGN(inout uint A, uint B)\n"
        + "{\n"
        + " return B == 0u ? (A %= 2147483646u) : (A %= B);\n"
        + "}\n";

    Assert.assertEquals(TestHelper.getText(WrapperGenerator.generateModAssignWrapper(BasicType.INT,
        BasicType.INT, ConfigInterface.RunType.STANDARD)), intModAssignText);
    Assert.assertEquals(TestHelper.getText(WrapperGenerator.generateModAssignWrapper(BasicType.UINT,
        BasicType.UINT, ConfigInterface.RunType.STANDARD)), uintModAssignText);
  }

  @Test
  public void testGenerateBitfieldInsertWrapper() {
    String intBitfieldInsertText = "int SAFE_BITFIELD_INSERT(int base, int insert, int offset, int "
        + "bits)\n"
        + "{\n"
        + " int safe_offset = SAFE_ABS(offset) % 32;\n"
        + " int safe_bits = SAFE_ABS(bits) % (32 - safe_offset);\n"
        + " return bitfieldInsert(base, insert, safe_offset, safe_bits);\n"
        + "}\n";
    String vec4BitfieldInsertText = "ivec4 SAFE_BITFIELD_INSERT(ivec4 base, ivec4 insert,"
        + " int offset, int bits)\n"
        + "{\n"
        + " int safe_offset = SAFE_ABS(offset) % 32;\n"
        + " int safe_bits = SAFE_ABS(bits) % (32 - safe_offset);\n"
        + " return bitfieldInsert(base, insert, safe_offset, safe_bits);\n"
        + "}\n";

    Assert.assertEquals(TestHelper.getText(WrapperGenerator.generateBitInsertWrapper(
        BasicType.INT, null, ConfigInterface.RunType.STANDARD)),
        intBitfieldInsertText);
    Assert.assertEquals(TestHelper.getText(WrapperGenerator.generateBitInsertWrapper(
        BasicType.IVEC4, null, ConfigInterface.RunType.STANDARD)),
        vec4BitfieldInsertText);
  }

  @Test
  public void testGenerateBitfieldExtractWrapper() {
    String intBitfieldExtractText = "int SAFE_BITFIELD_EXTRACT(int value, int offset, int bits)\n"
        + "{\n"
        + " int safe_offset = SAFE_ABS(offset) % 32;\n"
        + " int safe_bits = SAFE_ABS(bits) % (32 - safe_offset);\n"
        + " return bitfieldExtract(value, safe_offset, safe_bits);\n"
        + "}\n";
    String vec4BitfieldExtractText = "ivec4 SAFE_BITFIELD_EXTRACT(ivec4 value,"
        + " int offset, int bits)\n"
        + "{\n"
        + " int safe_offset = SAFE_ABS(offset) % 32;\n"
        + " int safe_bits = SAFE_ABS(bits) % (32 - safe_offset);\n"
        + " return bitfieldExtract(value, safe_offset, safe_bits);\n"
        + "}\n";

    Assert.assertEquals(TestHelper.getText(WrapperGenerator.generateBitExtractWrapper(
        BasicType.INT, null, ConfigInterface.RunType.STANDARD)),
        intBitfieldExtractText);
    Assert.assertEquals(TestHelper.getText(WrapperGenerator.generateBitExtractWrapper(
        BasicType.IVEC4, null, ConfigInterface.RunType.STANDARD)),
        vec4BitfieldExtractText);
  }

  @Test
  public void testGenerateClampWrapper() {
    String intText = "ivec4 SAFE_CLAMP(ivec4 value, int minVal, int maxVal)\n"
        + "{\n"
        + " return minVal > maxVal ? clamp(value, min(minVal, maxVal), max(minVal, maxVal)) : "
        + "clamp(value, minVal, maxVal);\n"
        + "}\n";
    String uintText = "uvec2 SAFE_CLAMP(uvec2 value, uvec2 minVal, uvec2 maxVal)\n"
        + "{\n"
        + " return any(greaterThan(minVal, maxVal)) ? clamp(value, min(minVal, maxVal), max"
        + "(minVal, maxVal)) : clamp(value, minVal, maxVal);\n"
        + "}\n";

    Assert.assertEquals(TestHelper.getText(WrapperGenerator.generateClampWrapper(BasicType.IVEC4,
        BasicType.INT, ConfigInterface.RunType.STANDARD)), intText);
    Assert.assertEquals(TestHelper.getText(WrapperGenerator.generateClampWrapper(BasicType.UVEC2,
        BasicType.UVEC2, ConfigInterface.RunType.STANDARD)), uintText);
  }

  @Test
  public void testGeneratePreIncWrapper() {
    String floatText = "float SAFE_PRE_INC(inout float A)\n"
        + "{\n"
        + " return abs(A + 1.0f) >= 16777216.0 ? A = 7.0f : ++ A;\n"
        + "}\n";

    Assert.assertEquals(TestHelper.getText(WrapperGenerator.generatePreIncWrapper(BasicType.FLOAT,
        null, ConfigInterface.RunType.STANDARD)), floatText);
  }

  @Test
  public void testGeneratePostIncWrapper() {
    String floatText = "float SAFE_POST_INC(inout float A)\n"
        + "{\n"
        + " return abs(A + 1.0f) >= 16777216.0 ? A = 1.0f : A ++;\n"
        + "}\n";

    Assert.assertEquals(TestHelper.getText(WrapperGenerator.generatePostIncWrapper(BasicType.FLOAT,
        null, ConfigInterface.RunType.STANDARD)), floatText);
  }

  @Test
  public void testGeneratePostDecWrapper() {
    String floatText = "float SAFE_POST_DEC(inout float A)\n"
        + "{\n"
        + " return abs(A - 1.0f) >= 16777216.0 ? A = 2.0f : A --;\n"
        + "}\n";

    Assert.assertEquals(TestHelper.getText(WrapperGenerator.generatePostDecWrapper(BasicType.FLOAT,
        null, ConfigInterface.RunType.STANDARD)), floatText);
  }

  @Test
  public void testGeneratePreDecWrapper() {
    String floatText = "float SAFE_PRE_DEC(inout float A)\n"
        + "{\n"
        + " return abs(A - 1.0f) >= 16777216.0 ? A = 3.0f : -- A;\n"
        + "}\n";
    String vec2Text = "vec2 SAFE_PRE_DEC(inout vec2 A)\n"
        + "{\n"
        + " return any(greaterThanEqual(abs(A - 1.0f), vec2(16777216.0f))) ? A = vec2(3.0f) : -- "
        + "A;\n"
        + "}\n";

    Assert.assertEquals(TestHelper.getText(WrapperGenerator.generatePreDecWrapper(BasicType.FLOAT,
        null, ConfigInterface.RunType.STANDARD)), floatText);
    Assert.assertEquals(TestHelper.getText(WrapperGenerator.generatePreDecWrapper(BasicType.VEC2,
        null, ConfigInterface.RunType.STANDARD)), vec2Text);
  }

  @Test
  public void testGenerateAddAssignWrapper() {
    String floatText = "float SAFE_ADD_ASSIGN(inout float A, float B)\n"
        + "{\n"
        + " return abs(A + B) >= 16777216.0 ? A = 8.0f : (A += B);\n"
        + "}\n";

    Assert.assertEquals(TestHelper.getText(WrapperGenerator.generateAddAssignWrapper(
        BasicType.FLOAT, BasicType.FLOAT, ConfigInterface.RunType.STANDARD)), floatText);
  }

  @Test
  public void testGenerateSubAssignWrapper() {
    String floatText = "float SAFE_SUB_ASSIGN(inout float A, float B)\n"
        + "{\n"
        + " return abs(A - B) >= 16777216.0 ? A = 5.0f : (A -= B);\n"
        + "}\n";
    String vec2Text = "vec2 SAFE_SUB_ASSIGN(inout vec2 A, vec2 B)\n"
        + "{\n"
        + " return any(greaterThanEqual(abs(A - B), vec2(16777216.0f))) ? A = vec2(5.0f) : (A -= "
        + "B);\n"
        + "}\n";

    Assert.assertEquals(TestHelper.getText(WrapperGenerator.generateSubAssignWrapper(
        BasicType.FLOAT, BasicType.FLOAT, ConfigInterface.RunType.STANDARD)), floatText);
    Assert.assertEquals(TestHelper.getText(WrapperGenerator.generateSubAssignWrapper(
        BasicType.VEC2, BasicType.VEC2, ConfigInterface.RunType.STANDARD)), vec2Text);
  }

  @Test
  public void testGenerateMulAssignWrapper() {
    String floatText = "float SAFE_MUL_ASSIGN(inout float A, float B)\n"
        + "{\n"
        + " return abs(A * B) >= 16777216.0 ? A = 12.0f : (A *= B);\n"
        + "}\n";
    String vec3Text = "vec3 SAFE_MUL_ASSIGN(inout vec3 A, float B)\n"
        + "{\n"
        + " return any(greaterThanEqual(abs(A * B), vec3(16777216.0f))) ? A = vec3(12.0f) : (A *= "
        + "B);\n"
        + "}\n";

    Assert.assertEquals(TestHelper.getText(WrapperGenerator.generateMulAssignWrapper(
        BasicType.FLOAT, BasicType.FLOAT, ConfigInterface.RunType.STANDARD)), floatText);
    Assert.assertEquals(TestHelper.getText(WrapperGenerator.generateMulAssignWrapper(
        BasicType.VEC3, BasicType.FLOAT, ConfigInterface.RunType.STANDARD)), vec3Text);
  }

  @Test
  public void testGenerateFloatResultWrapper() {
    String floatText = "float SAFE_FLOAT_RESULT(float A)\n"
        + "{\n"
        + " return abs(A) >= 16777216.0 ? 10.0f : A;\n"
        + "}\n";
    String vec4Text = "vec4 SAFE_FLOAT_RESULT(vec4 A)\n"
        + "{\n"
        + " return any(greaterThanEqual(abs(A), vec4(16777216.0f))) ? "
        + "vec4(10.0f) : A;\n"
        + "}\n";

    Assert.assertEquals(TestHelper.getText(WrapperGenerator.generateFloatResultWrapper(
        BasicType.FLOAT, null, ConfigInterface.RunType.STANDARD)), floatText);
    Assert.assertEquals(TestHelper.getText(WrapperGenerator.generateFloatResultWrapper(
        BasicType.VEC4, null, ConfigInterface.RunType.STANDARD)), vec4Text);
  }
}


