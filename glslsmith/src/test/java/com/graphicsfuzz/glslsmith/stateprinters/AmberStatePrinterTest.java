package com.graphicsfuzz.glslsmith.stateprinters;

import com.graphicsfuzz.common.ast.decl.ArrayInfo;
import com.graphicsfuzz.common.ast.expr.IntConstantExpr;
import com.graphicsfuzz.common.ast.type.ArrayType;
import com.graphicsfuzz.common.ast.type.BasicType;
import com.graphicsfuzz.common.ast.type.BindingLayoutQualifier;
import com.graphicsfuzz.common.ast.type.LayoutQualifier;
import com.graphicsfuzz.common.ast.type.LayoutQualifierSequence;
import com.graphicsfuzz.common.ast.type.Std430LayoutQualifier;
import com.graphicsfuzz.common.ast.type.Type;
import com.graphicsfuzz.common.ast.type.TypeQualifier;
import com.graphicsfuzz.glslsmith.Buffer;
import com.graphicsfuzz.glslsmith.ProgramState;
import com.graphicsfuzz.glslsmith.util.TestHelper;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class AmberStatePrinterTest {

  static Buffer buffer2;
  static String buffer2Text = "BUFFER buffer_2 DATA_TYPE int32 DATA\n"
      + " # DATA_SIZE 1 1 1\n"
      + " 12 -45 25\n"
      + "END\n";
  static Buffer buffer3;
  static String buffer3Text = "BUFFER buffer_3 DATA_TYPE float DATA\n"
      + " # DATA_SIZE 2 1 1 1\n"
      + " 1.0 2.0 -10.0 3.0 5.0\n"
      + "END\n";

  static Buffer buffer4;
  static String buffer4Text = "BUFFER buffer_4 DATA_TYPE int32 DATA\n"
      + " # DATA_SIZE 2 7\n"
      + " 72 48 78 32 26 21 24 121 110\n"
      + "END\n";

  static String minimalShaderText = "#version 310 es\n"
      + "layout(local_size_x = 1, local_size_y = 1, local_size_z = 1) in;\n"
      + "void main()\n"
      + "{\n"
      + "}\n";
  static String minimalProgramText = "#!amber\n"
      + "SHADER compute computeShader GLSL\n"
      + minimalShaderText
      + "END\n"
      + "\n"
      + "PIPELINE compute computePipeline\n"
      + "  ATTACH computeShader\n"
      + "END\n"
      + "\n"
      + "RUN computePipeline 1 1 1";
  static String programWithBuffers = "#!amber\n"
      + "SHADER compute computeShader GLSL\n"
      + minimalShaderText
      + "END\n"
      + buffer2Text
      + buffer3Text
      + buffer4Text
      + "\n"
      + "PIPELINE compute computePipeline\n"
      + "  ATTACH computeShader\n"
      + "  BIND BUFFER buffer_2 AS storage DESCRIPTOR_SET 0 BINDING 2\n"
      + "  BIND BUFFER buffer_3 AS storage DESCRIPTOR_SET 0 BINDING 3\n"
      + "  BIND BUFFER buffer_4 AS storage DESCRIPTOR_SET 0 BINDING 4\n"
      + "END\n"
      + "\n"
      + "RUN computePipeline 1 1 1";

  static String bufferIdText = "BUFFER buffer_id DATA_TYPE int32 DATA\n"
      + " # DATA_SIZE 3\n"
      + " 0 0 0\n"
      + "END\n";

  static String programWithIdBuffer = "#!amber\n"
      + "SHADER compute computeShader GLSL\n"
      + minimalShaderText
      + "END\n"
      + bufferIdText
      + buffer2Text
      + buffer3Text
      + buffer4Text
      + "\n"
      + "PIPELINE compute computePipeline\n"
      + "  ATTACH computeShader\n"
      + "  BIND BUFFER buffer_id AS storage DESCRIPTOR_SET 0 BINDING 0\n"
      + "  BIND BUFFER buffer_2 AS storage DESCRIPTOR_SET 0 BINDING 2\n"
      + "  BIND BUFFER buffer_3 AS storage DESCRIPTOR_SET 0 BINDING 3\n"
      + "  BIND BUFFER buffer_4 AS storage DESCRIPTOR_SET 0 BINDING 4\n"
      + "END\n"
      + "\n"
      + "RUN computePipeline 1 1 1";


  @BeforeClass
  public static void setup() {
    // Buffer 2
    List<? extends Number> valueList2 = Arrays.asList(12, -45, 25);
    buffer2 = new Buffer("buffer_2", new LayoutQualifierSequence(new BindingLayoutQualifier(2)),
        valueList2, Collections.singletonList(TypeQualifier.BUFFER),
        Arrays.asList("int1", "int2", "int3"),
        Arrays.asList(BasicType.INT, BasicType.INT, BasicType.INT), "", true, 2);

    // Buffer 3
    List<? extends Number> valueList3 = Arrays.asList(1., 2., -10., 3., 5.);
    ArrayInfo floatInfo3 = new ArrayInfo(Collections.singletonList(Optional.of(
        new IntConstantExpr("2"))));
    floatInfo3.setConstantSizeExpr(0, 2);
    List<Type> memberTypes3 = Arrays.asList(new ArrayType(BasicType.FLOAT, floatInfo3),
        BasicType.FLOAT, BasicType.FLOAT, BasicType.FLOAT);
    buffer3 = new Buffer("buffer_3", new LayoutQualifierSequence(new BindingLayoutQualifier(3)),
        valueList3, Collections.singletonList(TypeQualifier.BUFFER),
        Arrays.asList("float3", "float4", "float5", "float6"),
        memberTypes3, "", false, 3);

    // Buffer 4
    List<? extends Number> valueList4 = Arrays.asList(72, 48, 78, 32, 26, 21, 24, 121, 110);
    ArrayInfo arrayInfo1 = new ArrayInfo(Collections.singletonList(Optional.of(
        new IntConstantExpr(String.valueOf(2)))));
    arrayInfo1.setConstantSizeExpr(0, 2);
    ArrayInfo arrayInfo2 = new ArrayInfo(Collections.singletonList(Optional.of(
        new IntConstantExpr(String.valueOf(7)))));
    arrayInfo2.setConstantSizeExpr(0, 7);
    List<Type> memberTypes = Arrays.asList(new ArrayType(BasicType.INT, arrayInfo1),
        new ArrayType(BasicType.INT, arrayInfo2));
    buffer4 = new Buffer("buffer_4", new LayoutQualifierSequence(new BindingLayoutQualifier(4)),
        valueList4, Collections.singletonList(TypeQualifier.BUFFER),
        Arrays.asList("int4", "int5"), memberTypes, "", false,
        4);

  }

  @Test
  public void testPrintWrapperWithEmptyProgram() {
    ProgramState programState = TestHelper.generateProgramStateForCode(minimalShaderText);
    Assert.assertEquals(new AmberStatePrinter().printHarness(programState),
        minimalProgramText);
  }

  @Test
  public void testPrintWrapperWithEmptyProgramAndBuffers() {
    ProgramState programState = TestHelper.generateProgramStateForCode(minimalShaderText,
        Arrays.asList(buffer2, buffer3, buffer4));
    Assert.assertEquals(new AmberStatePrinter().printHarness(programState),
        programWithBuffers);
  }

  @Test
  public void testPrintBufferWrapper() {
    Assert.assertEquals(new AmberStatePrinter().printBufferWrapper(buffer2), buffer2Text);
    Assert.assertEquals(new AmberStatePrinter().printBufferWrapper(buffer3), buffer3Text);
    Assert.assertEquals(new AmberStatePrinter().printBufferWrapper(buffer4), buffer4Text);
  }

  @Test
  public void testGetShaderCodeFromHarness() {
    Assert.assertEquals(new AmberStatePrinter().getShaderCodeFromHarness(minimalProgramText),
        minimalShaderText);
  }

  @Test
  public void testAddBufferToHarness() {
    // Buffer id
    List<? extends Number> valueList = Arrays.asList(0, 0, 0);
    ArrayInfo arrayInfoId = new ArrayInfo(Collections.singletonList(Optional.of(
        new IntConstantExpr(String.valueOf(3)))));
    arrayInfoId.setConstantSizeExpr(0, 3);
    Buffer bufferId = new Buffer("buffer_id",
        new LayoutQualifierSequence(new BindingLayoutQualifier(0)),
        valueList, Collections.singletonList(TypeQualifier.BUFFER),
        Collections.singletonList("ids"),
        Collections.singletonList(new ArrayType(BasicType.INT, arrayInfoId)), "", false, 0);

    Assert.assertEquals(new AmberStatePrinter().addBufferToHarness(programWithBuffers, bufferId),
        programWithIdBuffer);
  }

  @Test
  public void testGetBuffersFromHarness() {
    //Test with buffer 2
    List<Buffer> buffers =  new AmberStatePrinter().getBuffersFromHarness(
        programWithBuffers);
    Buffer buffer2 = buffers.get(0);
    Assert.assertEquals(buffer2.getName(), "buffer_2");
    Assert.assertEquals(buffer2.getBinding(), 2);
    List<LayoutQualifier> layoutSequence = buffer2.getLayoutQualifiers().getLayoutQualifiers();
    Assert.assertTrue(layoutSequence.get(0) instanceof Std430LayoutQualifier);
    Assert.assertTrue(layoutSequence.get(1) instanceof BindingLayoutQualifier);
    Assert.assertEquals(((BindingLayoutQualifier)layoutSequence.get(1)).getIndex(), 2);
    Assert.assertEquals(buffer2.getMemberNames(), Arrays.asList("ext_0", "ext_1", "ext_2"));
    List<Type> memberTypes = buffer2.getMemberTypes();
    Assert.assertEquals(memberTypes.get(0), BasicType.INT);
    Assert.assertEquals(memberTypes.get(1), BasicType.INT);
    Assert.assertEquals(memberTypes.get(2), BasicType.INT);

    //Test with buffer 3
    Buffer buffer3 = buffers.get(1);
    Assert.assertEquals(buffer3.getName(), "buffer_3");
    Assert.assertEquals(buffer3.getBinding(), 3);
    layoutSequence = buffer3.getLayoutQualifiers().getLayoutQualifiers();
    Assert.assertTrue(layoutSequence.get(0) instanceof Std430LayoutQualifier);
    Assert.assertTrue(layoutSequence.get(1) instanceof BindingLayoutQualifier);
    Assert.assertEquals(((BindingLayoutQualifier)layoutSequence.get(1)).getIndex(), 3);
    Assert.assertEquals(buffer3.getMemberNames(),
        Arrays.asList("ext_3", "ext_4", "ext_5", "ext_6"));
    memberTypes = buffer3.getMemberTypes();
    ArrayInfo member1Info =
        new ArrayInfo(Collections.singletonList(Optional.of(new IntConstantExpr("2"))));
    member1Info.setConstantSizeExpr(0, 2);
    Assert.assertEquals(memberTypes.get(0), new ArrayType(BasicType.FLOAT, member1Info));
    Assert.assertEquals(memberTypes.get(1), BasicType.FLOAT);
    Assert.assertEquals(memberTypes.get(2), BasicType.FLOAT);
    Assert.assertEquals(memberTypes.get(3), BasicType.FLOAT);

    //Test with buffer 4
    Buffer buffer4 = buffers.get(2);
    Assert.assertEquals(buffer4.getName(), "buffer_4");
    Assert.assertEquals(buffer4.getBinding(), 4);
    layoutSequence = buffer4.getLayoutQualifiers().getLayoutQualifiers();
    Assert.assertTrue(layoutSequence.get(0) instanceof Std430LayoutQualifier);
    Assert.assertTrue(layoutSequence.get(1) instanceof BindingLayoutQualifier);
    Assert.assertEquals(((BindingLayoutQualifier)layoutSequence.get(1)).getIndex(), 4);
    Assert.assertEquals(buffer4.getMemberNames(), Arrays.asList("ext_7", "ext_8"));
    memberTypes = buffer4.getMemberTypes();
    member1Info = new ArrayInfo(Collections.singletonList(Optional.of(new IntConstantExpr("2"))));
    member1Info.setConstantSizeExpr(0, 2);
    ArrayInfo member2Info =
        new ArrayInfo(Collections.singletonList(Optional.of(new IntConstantExpr("7"))));
    member2Info.setConstantSizeExpr(0, 7);
    Assert.assertEquals(memberTypes.get(0), new ArrayType(BasicType.INT, member1Info));
    Assert.assertEquals(memberTypes.get(1), new ArrayType(BasicType.INT, member2Info));
  }

}
