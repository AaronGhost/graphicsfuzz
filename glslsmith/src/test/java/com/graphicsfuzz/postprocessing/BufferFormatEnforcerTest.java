package com.graphicsfuzz.postprocessing;

import com.graphicsfuzz.Buffer;
import com.graphicsfuzz.ProgramState;
import com.graphicsfuzz.common.ast.decl.ArrayInfo;
import com.graphicsfuzz.common.ast.expr.IntConstantExpr;
import com.graphicsfuzz.common.ast.type.ArrayType;
import com.graphicsfuzz.common.ast.type.BasicType;
import com.graphicsfuzz.common.ast.type.BindingLayoutQualifier;
import com.graphicsfuzz.common.ast.type.LayoutQualifierSequence;
import com.graphicsfuzz.common.ast.type.Std430LayoutQualifier;
import com.graphicsfuzz.common.ast.type.TypeQualifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class BufferFormatEnforcerTest extends CommonPostProcessingTest {



  Buffer buffer0;

  String bufferWithoutNeedText = "#version 320 es\n"
      + "layout(std430, binding = 1) buffer buffer_0 {\n"
      + " int ext_0;\n"
      + " int ext_1[5];\n"
      + " uint ext_2[3];\n"
      + "};\n"
      + "void main()\n"
      + "{\n"
      + " ivec2 var_0 = ivec2(3) << 5;\n"
      + "}\n";

  String bufferWithOnlyBindingText = "#version 320 es\n"
      + "buffer buffer_0 {\n"
      + " int ext_0;\n"
      + "};\n"
      + "void main()\n"
      + "{\n"
      + " ivec2 var_0 = ivec2(3) << 5;\n"
      + "}\n";


  @Before
  @Override
  public void setup() {
    super.setup();
    ArrayInfo memberInfo2 =
        new ArrayInfo(Collections.singletonList(Optional.of(new IntConstantExpr("5"))));
    memberInfo2.setConstantSizeExpr(0, 5);
    ArrayInfo memberInfo3 =
        new ArrayInfo(Collections.singletonList(Optional.of(new IntConstantExpr("3"))));
    memberInfo3.setConstantSizeExpr(0, 3);
    ArrayType memberType2 = new ArrayType(BasicType.INT, memberInfo2);
    ArrayType memberType3 = new ArrayType(BasicType.UINT, memberInfo3);
    buffer0 = new Buffer("buffer_0",
        new LayoutQualifierSequence(Arrays.asList(new Std430LayoutQualifier(),
            new BindingLayoutQualifier(1))),
        Arrays.asList(1, 1, 1, 1, 1, 1, 1, 1, 1),
        TypeQualifier.BUFFER,
        Arrays.asList("ext_0", "ext_1", "ext_2"),
        Arrays.asList(BasicType.INT, memberType2, memberType3), "", true,
        1);
  }

  @Override
  protected List<PostProcessorInterface> createInstance() {
    return Collections.singletonList(new BufferFormatEnforcer());
  }

  @Test
  public void testProcessWithAlreadyCorrectBufferShader() {
    ProgramState returnState = new BufferFormatEnforcer()
        .process(generateProgramStateForCode(bufferWithoutNeedText,
            Collections.singletonList(buffer0)));
    Assert.assertEquals(returnState.getShaderCode(), bufferWithoutNeedText);
  }

  @Test
  public void testProcessWithBufferToCorrectShader() {
    ProgramState returnState = new BufferFormatEnforcer()
        .process(generateProgramStateForCode(bufferWithOnlyBindingText,
            Collections.singletonList(buffer0)));
    Assert.assertEquals(returnState.getShaderCode(), bufferWithoutNeedText);
  }
}
