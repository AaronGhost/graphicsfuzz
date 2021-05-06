package com.graphicsfuzz.stateprinters;

import com.graphicsfuzz.Buffer;
import com.graphicsfuzz.ProgramState;
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
import com.graphicsfuzz.common.ast.type.Type;
import com.graphicsfuzz.common.ast.type.TypeQualifier;
import com.graphicsfuzz.common.ast.type.VoidType;
import com.graphicsfuzz.common.glslversion.ShadingLanguageVersion;
import com.graphicsfuzz.common.util.ShaderKind;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class ShaderTrapStatePrinterTest {

  Buffer buffer2;
  String buffer2Text = "CREATE_BUFFER buffer_2 SIZE_BYTES 12 INIT_VALUES uint 12 int 45 int 25\n"
      + "BIND_SHADER_STORAGE_BUFFER BUFFER buffer_2 BINDING 2\n\n";
  Buffer buffer4;
  String buffer4Text = "CREATE_BUFFER buffer_4 SIZE_BYTES 36 INIT_VALUES int 72 48 uint 78 32 26 "
      + "21 24 121 110\n"
      + "BIND_SHADER_STORAGE_BUFFER BUFFER buffer_4 BINDING 4\n\n";
  String minimalProgramText = "DECLARE_SHADER shader KIND COMPUTE\n"
      + "#version 320 es\n"
      + "layout(local_size_x = 1, local_size_y = 1, local_size_z = 1) in;\n"
      + "void main()\n"
      + "{\n"
      + "}\n"
      + "END\n\n"
      + "COMPILE_SHADER shader_compiled SHADER shader\n"
      + "CREATE_PROGRAM compute_prog SHADERS shader_compiled\n"
      + "RUN_COMPUTE PROGRAM compute_prog NUM_GROUPS 1 1 1\n";

  @Before
  public void setup() {
    List<? extends Number> valueList2 = Arrays.asList(12, 45, 25);
    buffer2 = new Buffer("buffer_2", new LayoutQualifierSequence(new BindingLayoutQualifier(2)),
        valueList2, TypeQualifier.BUFFER, Arrays.asList("uint1", "int1", "int2"),
        Arrays.asList(BasicType.UINT, BasicType.INT, BasicType.INT), "", true, 2);
    List<? extends Number> valueList4 = Arrays.asList(72, 48, 78, 32, 26, 21, 24, 121, 110);
    ArrayInfo arrayInfo1 = new ArrayInfo(new IntConstantExpr(String.valueOf(2)));
    arrayInfo1.setConstantSizeExpr(2);
    ArrayInfo arrayInfo2 = new ArrayInfo(new IntConstantExpr(String.valueOf(7)));
    arrayInfo2.setConstantSizeExpr(7);
    List<Type> memberTypes = Arrays.asList(new ArrayType(BasicType.INT, arrayInfo1),
        new ArrayType(BasicType.UINT, arrayInfo2));
    buffer4 = new Buffer("buffer_4", new LayoutQualifierSequence(new BindingLayoutQualifier(4)),
        valueList4, TypeQualifier.BUFFER, Arrays.asList("int4", "uint4"), memberTypes, "", false,
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

    ProgramState programState = new ProgramState();
    programState.programInitialization(new TranslationUnit(ShaderKind.COMPUTE,
        Optional.of(ShadingLanguageVersion.ESSL_320), Arrays.asList(inVariable, mainFunction)),
        ShaderKind.COMPUTE);
    Assert.assertEquals(new ShaderTrapStatePrinter().printWrapper(programState),
        minimalProgramText);
  }

  @Test
  public void testPrintBufferWrapper() {
    Assert.assertEquals(new ShaderTrapStatePrinter().printBufferWrapper(buffer2), buffer2Text);
    Assert.assertEquals(new ShaderTrapStatePrinter().printBufferWrapper(buffer4), buffer4Text);
  }

  @Test
  @Ignore
  public void testDumpBufferWithInputBuffer() {
    Assert.assertEquals(new ShaderTrapStatePrinter().printDumpBuffer(buffer2), "");
  }

  @Test
  @Ignore
  public void testDumBufferWithOutputBuffer() {
    Assert.assertEquals(new ShaderTrapStatePrinter().printDumpBuffer(buffer4), "DUMP_BUFFER "
        + "buffer_4\n");
  }

}
