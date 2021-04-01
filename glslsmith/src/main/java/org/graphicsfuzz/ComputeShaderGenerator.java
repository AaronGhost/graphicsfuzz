package org.graphicsfuzz;

import com.graphicsfuzz.common.ast.TranslationUnit;
import com.graphicsfuzz.common.ast.decl.Declaration;
import com.graphicsfuzz.common.ast.decl.DefaultLayout;
import com.graphicsfuzz.common.ast.decl.FunctionDefinition;
import com.graphicsfuzz.common.ast.decl.FunctionPrototype;
import com.graphicsfuzz.common.ast.stmt.BlockStmt;
import com.graphicsfuzz.common.ast.type.LayoutQualifier;
import com.graphicsfuzz.common.ast.type.LayoutQualifierSequence;
import com.graphicsfuzz.common.ast.type.LocalSizeLayoutQualifier;
import com.graphicsfuzz.common.ast.type.TypeQualifier;
import com.graphicsfuzz.common.ast.type.VoidType;
import com.graphicsfuzz.common.glslversion.ShadingLanguageVersion;
import com.graphicsfuzz.common.util.IRandom;
import com.graphicsfuzz.common.util.ShaderKind;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ComputeShaderGenerator extends ShaderGenerator {

  public ComputeShaderGenerator(IRandom randomGenerator) {
    super(randomGenerator);
  }

  @Override
  public void generateShader(ProgramState programState) {
    generateInputBuffers(programState);
    generateOutputBuffers(programState);
    generateEmptyShader(programState);
    //Generate random instructions
  }

  protected void generateInputBuffers(ProgramState programState) {
    List<Buffer> inputBuffers = Arrays.asList(new Buffer(), new Buffer());
    programState.setInputBuffers(inputBuffers);
  }

  protected void generateOutputBuffers(ProgramState programState) {

  }

  protected void generateEmptyShader(ProgramState programState) {
    List<LayoutQualifier> localSizes = Arrays.asList(
        new LocalSizeLayoutQualifier("x", randomGenerator.nextPositiveInt(64)),
        new LocalSizeLayoutQualifier("y", randomGenerator.nextPositiveInt(64)),
        new LocalSizeLayoutQualifier("z", randomGenerator.nextPositiveInt(64)));
    DefaultLayout inVariable = new DefaultLayout(new LayoutQualifierSequence(localSizes),
        TypeQualifier.SHADER_INPUT);
    FunctionDefinition mainFunction = new FunctionDefinition(new FunctionPrototype("main",
        VoidType.VOID, new ArrayList<>()), new BlockStmt(new ArrayList<>(), true));
    List<Declaration> declList = Arrays.asList(inVariable, mainFunction);
    TranslationUnit translationUnit = new TranslationUnit(ShaderKind.COMPUTE,
        Optional.of(ShadingLanguageVersion.ESSL_320), declList);
    programState.programInitialization(translationUnit, ShaderKind.COMPUTE);
  }
}
