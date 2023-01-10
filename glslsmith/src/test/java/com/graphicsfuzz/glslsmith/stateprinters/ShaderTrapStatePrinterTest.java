package com.graphicsfuzz.glslsmith.stateprinters;

import com.graphicsfuzz.common.ast.TranslationUnit;
import com.graphicsfuzz.common.ast.decl.ArrayInfo;
import com.graphicsfuzz.common.ast.decl.DefaultLayout;
import com.graphicsfuzz.common.ast.decl.FunctionDefinition;
import com.graphicsfuzz.common.ast.decl.FunctionPrototype;
import com.graphicsfuzz.common.ast.expr.IntConstantExpr;
import com.graphicsfuzz.common.ast.stmt.BlockStmt;
import com.graphicsfuzz.common.ast.type.ArrayType;
import com.graphicsfuzz.common.ast.type.BasicType;
import com.graphicsfuzz.common.ast.type.BindingLayoutQualifier;
import com.graphicsfuzz.common.ast.type.LayoutQualifier;
import com.graphicsfuzz.common.ast.type.LayoutQualifierSequence;
import com.graphicsfuzz.common.ast.type.LocalSizeLayoutQualifier;
import com.graphicsfuzz.common.ast.type.Std430LayoutQualifier;
import com.graphicsfuzz.common.ast.type.Type;
import com.graphicsfuzz.common.ast.type.TypeQualifier;
import com.graphicsfuzz.common.ast.type.VoidType;
import com.graphicsfuzz.common.glslversion.ShadingLanguageVersion;
import com.graphicsfuzz.common.util.ShaderKind;
import com.graphicsfuzz.glslsmith.Buffer;
import com.graphicsfuzz.glslsmith.ProgramState;
import com.graphicsfuzz.glslsmith.config.ParameterConfiguration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class ShaderTrapStatePrinterTest {

  static Buffer buffer2;
  static String buffer2Text = "CREATE_BUFFER buffer_2 SIZE_BYTES 12 INIT_VALUES uint 12 int -45 "
      + "int 25\n"
      + "BIND_SHADER_STORAGE_BUFFER BUFFER buffer_2 BINDING 2\n\n";

  static Buffer buffer3;
  static String buffer3Text = "CREATE_BUFFER buffer_3 SIZE_BYTES 20 INIT_VALUES float 1.0 2.0 int "
      + "-10 uint "
      + "3 float 5.0\n"
      + "BIND_SHADER_STORAGE_BUFFER BUFFER buffer_3 BINDING 3\n\n";

  static Buffer buffer4;
  static String buffer4Text = "CREATE_BUFFER buffer_4 SIZE_BYTES 36 INIT_VALUES int 72 48 uint 78 "
      + "32 26 "
      + "21 24 121 110\n"
      + "BIND_SHADER_STORAGE_BUFFER BUFFER buffer_4 BINDING 4\n\n";

  static String minimalShaderText = "#version 310 es\n"
      + "layout(local_size_x = 1, local_size_y = 1, local_size_z = 1) in;\n"
      + "void main()\n"
      + "{\n"
      + "}\n";

  static String minimalProgramText = "GLES 3.1\n"
      + "DECLARE_SHADER shader KIND COMPUTE\n"
      + minimalShaderText
      + "END\n\n"
      + "COMPILE_SHADER shader_compiled SHADER shader\n"
      + "CREATE_PROGRAM compute_prog SHADERS shader_compiled\n"
      + "RUN_COMPUTE PROGRAM compute_prog NUM_GROUPS 1 1 1\n";

  static String minimalProgramWithBuffers = "GLES 3.1\n"
      + buffer2Text
      + buffer3Text
      + buffer4Text
      + "DECLARE_SHADER shader KIND COMPUTE\n"
      + minimalShaderText
      + "END\n\n"
      + "COMPILE_SHADER shader_compiled SHADER shader\n"
      + "CREATE_PROGRAM compute_prog SHADERS shader_compiled\n"
      + "RUN_COMPUTE PROGRAM compute_prog NUM_GROUPS 1 1 1\n"
      + "DUMP_BUFFER_TEXT BUFFER buffer_3 FILE \"buffer_3.txt\" FORMAT \"buffer_3 \" float 2 \" "
          + "\" int 1 \" \" uint 1 \" \" float 1\n"
      + "DUMP_BUFFER_TEXT BUFFER buffer_4 FILE \"buffer_4.txt\" FORMAT \"buffer_4 \" int 2 \" \" "
      + "uint 7\n";

  static String bufferIdText = "CREATE_BUFFER buffer_id SIZE_BYTES 12 INIT_VALUES int 0 0 0\n"
      + "BIND_SHADER_STORAGE_BUFFER BUFFER buffer_id BINDING 0\n\n";

  static String minimalProgramWithIdBuffer =  "GLES 3.1\n"
      + bufferIdText
      + buffer2Text
      + buffer3Text
      + buffer4Text
      + "DECLARE_SHADER shader KIND COMPUTE\n"
      + minimalShaderText
      + "END\n\n"
      + "COMPILE_SHADER shader_compiled SHADER shader\n"
      + "CREATE_PROGRAM compute_prog SHADERS shader_compiled\n"
      + "RUN_COMPUTE PROGRAM compute_prog NUM_GROUPS 1 1 1\n"
      + "DUMP_BUFFER_TEXT BUFFER buffer_3 FILE \"buffer_3.txt\" FORMAT \"buffer_3 \" float 2 \" "
          + "\" int 1 \" \" uint 1 \" \" float 1\n"
      + "DUMP_BUFFER_TEXT BUFFER buffer_4 FILE \"buffer_4.txt\" FORMAT \"buffer_4 \" int 2 \" \" "
          + "uint 7\n"
      + "DUMP_BUFFER_TEXT BUFFER buffer_id FILE \"buffer_id.txt\" FORMAT \"buffer_id \" int 3\n";

  @BeforeClass
  public static void setup() {
    // Buffer 2
    List<? extends Number> valueList2 = Arrays.asList(12, -45, 25);
    buffer2 = new Buffer("buffer_2", new LayoutQualifierSequence(new BindingLayoutQualifier(2)),
        valueList2, Collections.singletonList(TypeQualifier.BUFFER),
        Arrays.asList("uint1", "int1", "int2"),
        Arrays.asList(BasicType.UINT, BasicType.INT, BasicType.INT), "", true, 2);

    // Buffer 3
    List<? extends Number> valueList3 = Arrays.asList(1., 2., -10, 3, 5.);
    ArrayInfo floatInfo3 = new ArrayInfo(Collections.singletonList(Optional.of(
        new IntConstantExpr("2"))));
    floatInfo3.setConstantSizeExpr(0, 2);
    List<Type> memberTypes3 = Arrays.asList(new ArrayType(BasicType.FLOAT, floatInfo3),
        BasicType.INT, BasicType.UINT, BasicType.FLOAT);
    buffer3 = new Buffer("buffer_3", new LayoutQualifierSequence(new BindingLayoutQualifier(3)),
        valueList3, Collections.singletonList(TypeQualifier.BUFFER),
        Arrays.asList("float3", "int3", "uint3", "float4"),
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
        new ArrayType(BasicType.UINT, arrayInfo2));
    buffer4 = new Buffer("buffer_4", new LayoutQualifierSequence(new BindingLayoutQualifier(4)),
        valueList4, Collections.singletonList(TypeQualifier.BUFFER),
        Arrays.asList("int4", "uint4"), memberTypes, "", false,
        4);

  }

  @Test
  public void testPrintWrapperWithEmptyProgram() {
    List<LayoutQualifier> localSizes = Arrays.asList(
        new LocalSizeLayoutQualifier("x", 1),
        new LocalSizeLayoutQualifier("y", 1),
        new LocalSizeLayoutQualifier("z", 1));
    DefaultLayout inVariable = new DefaultLayout(new LayoutQualifierSequence(localSizes),
        TypeQualifier.SHADER_INPUT);

    //Generate an empty main function
    FunctionDefinition mainFunction = new FunctionDefinition(new FunctionPrototype("main",
        VoidType.VOID, new ArrayList<>()), new BlockStmt(new ArrayList<>(), true));

    ProgramState programState = new ProgramState(
        new ParameterConfiguration.Builder().getConfig());
    programState.programInitialization(new TranslationUnit(ShaderKind.COMPUTE,
        Optional.of(ShadingLanguageVersion.ESSL_310), Arrays.asList(inVariable, mainFunction)));
    Assert.assertEquals(new ShaderTrapStatePrinter().printHarness(programState),
        minimalProgramText);

    programState.addBuffer(buffer2);
    programState.addBuffer(buffer3);
    programState.addBuffer(buffer4);
    Assert.assertEquals(new ShaderTrapStatePrinter().printHarness(programState),
        minimalProgramWithBuffers);
  }

  @Test
  public void testParseVersion() {
    ShaderTrapStatePrinter printer = new ShaderTrapStatePrinter();
    Assert.assertEquals("GLES 3.1", printer.parseVersion("310 es"));
    Assert.assertEquals("GLES 3.2", printer.parseVersion("320 es"));
    Assert.assertEquals("GL 4.5", printer.parseVersion("450"));
  }

  @Test
  public void testPrintBufferWrapper() {
    Assert.assertEquals(new ShaderTrapStatePrinter().printBufferWrapper(buffer2), buffer2Text);
    Assert.assertEquals(new ShaderTrapStatePrinter().printBufferWrapper(buffer3), buffer3Text);
    Assert.assertEquals(new ShaderTrapStatePrinter().printBufferWrapper(buffer4), buffer4Text);
  }

  @Test
  public void testDumpBufferWithInputBuffer() {
    Assert.assertEquals(new ShaderTrapStatePrinter().printDumpBuffer(buffer2), "");
  }

  @Test
  public void testDumBufferWithOutputBuffer() {
    Assert.assertEquals(new ShaderTrapStatePrinter().printDumpBuffer(buffer3),
        "DUMP_BUFFER_TEXT BUFFER buffer_3 FILE \"buffer_3.txt\" FORMAT \"buffer_3 \" float 2 \" "
            + "\" int 1 \" \" uint 1 \" \" float 1\n");
    Assert.assertEquals(new ShaderTrapStatePrinter().printDumpBuffer(buffer4),
        "DUMP_BUFFER_TEXT BUFFER buffer_4 FILE \"buffer_4.txt\" FORMAT \"buffer_4 \" int 2 \" \" "
            + "uint 7\n");
  }

  @Test
  public void testGetShaderCodeFromHarness() {
    Assert.assertEquals(new ShaderTrapStatePrinter()
        .getShaderCodeFromHarness(minimalProgramWithBuffers), minimalShaderText);
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

    Assert.assertEquals(new ShaderTrapStatePrinter()
            .addBufferToHarness(minimalProgramWithBuffers, bufferId),
        minimalProgramWithIdBuffer);
  }

  @Test
  public void testGetBuffersFromHarness() {
    //Test with buffer 2
    List<Buffer> buffers =  new ShaderTrapStatePrinter().getBuffersFromHarness(
        buffer2Text + buffer3Text + buffer4Text);
    Buffer buffer2 = buffers.get(0);
    Assert.assertEquals(buffer2.getName(), "buffer_2");
    Assert.assertEquals(buffer2.getBinding(), 2);
    List<LayoutQualifier> layoutSequence = buffer2.getLayoutQualifiers().getLayoutQualifiers();
    Assert.assertTrue(layoutSequence.get(0) instanceof Std430LayoutQualifier);
    Assert.assertTrue(layoutSequence.get(1) instanceof BindingLayoutQualifier);
    Assert.assertEquals(((BindingLayoutQualifier)layoutSequence.get(1)).getIndex(), 2);
    Assert.assertEquals(buffer2.getMemberNames(), Arrays.asList("ext_0", "ext_1", "ext_2"));
    List<Type> memberTypes = buffer2.getMemberTypes();
    Assert.assertEquals(memberTypes.get(0), BasicType.UINT);
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
    Assert.assertEquals(memberTypes.get(1), BasicType.INT);
    Assert.assertEquals(memberTypes.get(2), BasicType.UINT);
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
    Assert.assertEquals(memberTypes.get(1), new ArrayType(BasicType.UINT, member2Info));
  }

}
