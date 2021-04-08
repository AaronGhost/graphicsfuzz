package org.graphicsfuzz;

import com.graphicsfuzz.common.ast.TranslationUnit;
import com.graphicsfuzz.common.ast.decl.Declaration;
import com.graphicsfuzz.common.ast.decl.DefaultLayout;
import com.graphicsfuzz.common.ast.decl.FunctionDefinition;
import com.graphicsfuzz.common.ast.decl.FunctionPrototype;
import com.graphicsfuzz.common.ast.stmt.BlockStmt;
import com.graphicsfuzz.common.ast.type.BasicType;
import com.graphicsfuzz.common.ast.type.BindingLayoutQualifier;
import com.graphicsfuzz.common.ast.type.LayoutQualifier;
import com.graphicsfuzz.common.ast.type.LayoutQualifierSequence;
import com.graphicsfuzz.common.ast.type.LocalSizeLayoutQualifier;
import com.graphicsfuzz.common.ast.type.Type;
import com.graphicsfuzz.common.ast.type.TypeQualifier;
import com.graphicsfuzz.common.ast.type.VoidType;
import com.graphicsfuzz.common.glslversion.ShadingLanguageVersion;
import com.graphicsfuzz.common.util.IRandom;
import com.graphicsfuzz.common.util.ShaderKind;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.tuple.ImmutablePair;

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
    //TODO handle meaningful upperbound
    for (int bindIndex = 0; bindIndex < randomGenerator.nextPositiveInt(5); bindIndex++) {
      internalGenerateBuffer(true, programState);
    }
  }

  protected void generateOutputBuffers(ProgramState programState) {
    //TODO handle meaningful upperbound
    for (int bindIndex = 0; bindIndex < randomGenerator.nextPositiveInt(5); bindIndex++) {
      internalGenerateBuffer(false, programState);
    }
  }

  private void internalGenerateBuffer(boolean inOut, ProgramState programState) {
    int newMembers = randomGenerator.nextPositiveInt(3);
    List<Number> values = new ArrayList<>();
    List<Type> memberTypes = new ArrayList<>();
    List<String> memberNames = new ArrayList<>();
    for (int memberIndex = 0; memberIndex < newMembers; memberIndex++) {
      ImmutablePair<Type, Integer> randomType = generateRandomIntType();
      memberTypes.add(randomType.left);
      memberNames.add("var_" + (memberIndex + programState.getVariableOffset()));
      for (int valueIndex = 0; valueIndex < randomType.right; valueIndex++) {
        values.add(randomGenerator.nextInt(128));
      }
    }
    programState.addVariableOffset(newMembers);
    Buffer inputBuffer = new Buffer("buffer_" + programState.getBindingOffset(),
        new LayoutQualifierSequence(new BindingLayoutQualifier(programState.getBindingOffset())),
        values, TypeQualifier.BUFFER, memberNames, memberTypes, "",
        inOut, programState.getBindingOffset());
    programState.addBindingOffset(1);
    programState.addSymbol(inputBuffer);
  }

  protected void generateEmptyShader(ProgramState programState) {
    List<LayoutQualifier> localSizes = Arrays.asList(
        new LocalSizeLayoutQualifier("x", randomGenerator.nextPositiveInt(64)),
        new LocalSizeLayoutQualifier("y", randomGenerator.nextPositiveInt(64)),
        new LocalSizeLayoutQualifier("z", randomGenerator.nextPositiveInt(64)));
    DefaultLayout inVariable = new DefaultLayout(new LayoutQualifierSequence(localSizes),
        TypeQualifier.SHADER_INPUT);
    List<Declaration> declList = new ArrayList<>();
    declList.add(inVariable);
    for (Symbol bufferSymbol : programState.getSymbolByType("Buffer")) {
      declList.add(generateInterfaceBlockFromBuffer(bufferSymbol));
    }
    FunctionDefinition mainFunction = new FunctionDefinition(new FunctionPrototype("main",
        VoidType.VOID, new ArrayList<>()), new BlockStmt(new ArrayList<>(), true));
    declList.add(mainFunction);
    TranslationUnit translationUnit = new TranslationUnit(ShaderKind.COMPUTE,
        Optional.of(ShadingLanguageVersion.ESSL_320), declList);
    programState.programInitialization(translationUnit, ShaderKind.COMPUTE);
  }
}
