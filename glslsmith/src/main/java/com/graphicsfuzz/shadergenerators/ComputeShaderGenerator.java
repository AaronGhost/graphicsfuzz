package com.graphicsfuzz.shadergenerators;

import com.graphicsfuzz.Buffer;
import com.graphicsfuzz.common.ast.TranslationUnit;
import com.graphicsfuzz.common.ast.decl.Declaration;
import com.graphicsfuzz.common.ast.decl.DefaultLayout;
import com.graphicsfuzz.common.ast.decl.FunctionDefinition;
import com.graphicsfuzz.common.ast.decl.FunctionPrototype;
import com.graphicsfuzz.common.ast.stmt.BlockStmt;
import com.graphicsfuzz.common.ast.stmt.Stmt;
import com.graphicsfuzz.common.ast.type.BasicType;
import com.graphicsfuzz.common.ast.type.BindingLayoutQualifier;
import com.graphicsfuzz.common.ast.type.LayoutQualifier;
import com.graphicsfuzz.common.ast.type.LayoutQualifierSequence;
import com.graphicsfuzz.common.ast.type.LocalSizeLayoutQualifier;
import com.graphicsfuzz.common.ast.type.Std430LayoutQualifier;
import com.graphicsfuzz.common.ast.type.Type;
import com.graphicsfuzz.common.ast.type.TypeQualifier;
import com.graphicsfuzz.common.ast.type.VoidType;
import com.graphicsfuzz.common.util.IRandom;
import com.graphicsfuzz.common.util.ShaderKind;
import com.graphicsfuzz.config.ConfigInterface;
import com.graphicsfuzz.config.FuzzerConstants;
import com.graphicsfuzz.scope.UnifiedTypeInterface;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ComputeShaderGenerator extends ShaderGenerator {

  public ComputeShaderGenerator(IRandom randomGenerator, ConfigInterface configuration) {
    super(randomGenerator, configuration);
  }

  @Override
  public void generateShader() {
    super.generateShader();
    generateInputBuffers();
    generateOutputBuffers();
    //TODO generate random uniforms and consts
    List<Stmt> statements = generateShaderMain();
    generateShaderSkeleton(statements);
  }

  public void generateInputBuffers() {
    for (int bindIndex = 0; bindIndex < randGen.nextPositiveInt(configuration.getMaxInputBuffers());
         bindIndex++) {
      internalGenerateBuffer(true);
    }
  }

  public void generateOutputBuffers() {
    for (int bindIndex = 0; bindIndex < randGen.nextPositiveInt(
        configuration.getMaxOutputBuffers());
         bindIndex++) {
      internalGenerateBuffer(false);
    }
  }

  private void internalGenerateBuffer(boolean inOut) {
    int newMembers = randGen.nextPositiveInt(configuration.getMaxBufferElements());

    //Buffer internal values holders
    List<Number> values = new ArrayList<>();
    List<Type> memberTypes = new ArrayList<>();
    List<String> memberNames = new ArrayList<>();

    //Randomly populate internal values
    for (int memberIndex = 0; memberIndex < newMembers; memberIndex++) {
      UnifiedTypeInterface proxy = randomTypeGenerator.getRandomNewType(true);
      memberTypes.add(proxy.getRealType());
      String name = programState.getNextUniformBufferName();
      memberNames.add(name);
      for (int valueIndex = 0; valueIndex < proxy.getElementSize(); valueIndex++) {
        Type baseType = proxy.getBaseType();
        if (baseType.equals(BasicType.INT)) {
          values.add(randGen.nextInt(FuzzerConstants.MIN_INT_VALUE, FuzzerConstants.MAX_INT_VALUE));
        } else {
          values.add(randGen.nextLong(FuzzerConstants.MAX_UINT_VALUE));
        }
      }
      //Adds the variable to the scope
      programState.addUniformBufferVariable(name, proxy);
    }

    //Create the correct buffer object
    Buffer inputBuffer = new Buffer("buffer_" + programState.getBindingOffset(),
        new LayoutQualifierSequence(new Std430LayoutQualifier(),
            new BindingLayoutQualifier(programState.getBindingOffset())),
        values, TypeQualifier.BUFFER, memberNames, memberTypes, "",
        inOut, programState.getBindingOffset());
    programState.addBuffer(inputBuffer);
  }


  protected List<Stmt> generateShaderMain() {
    return generateScope();
  }

  protected void generateShaderSkeleton(List<Stmt> mainStatements) {
    //Generate the mandatory local_size parameters for the defaultLayoutInput
    List<LayoutQualifier> localSizes = Arrays.asList(
        new LocalSizeLayoutQualifier("x",
            randGen.nextPositiveInt(configuration.getMaxLocalSizeX())),
        new LocalSizeLayoutQualifier("y",
            randGen.nextPositiveInt(configuration.getMaxLocalSizeY())),
        new LocalSizeLayoutQualifier("z",
            randGen.nextPositiveInt(configuration.getMaxLocalSizeZ())));
    DefaultLayout inVariable = new DefaultLayout(new LayoutQualifierSequence(localSizes),
        TypeQualifier.SHADER_INPUT);

    //Get the interfaceblock associated with buffer inputs and outputs
    List<Declaration> declList = new ArrayList<>();
    declList.add(inVariable);
    for (Buffer bufferSymbol : programState.getBuffers()) {
      declList.add(generateInterfaceBlockFromBuffer(bufferSymbol));
    }

    //Generate an empty main function
    FunctionDefinition mainFunction = new FunctionDefinition(new FunctionPrototype("main",
        VoidType.VOID, new ArrayList<>()), new BlockStmt(mainStatements, true));
    declList.add(mainFunction);

    //Generate the translation unit and add it to the programState
    TranslationUnit translationUnit = new TranslationUnit(ShaderKind.COMPUTE,
        Optional.of(configuration.getShadingLanguageVersion()), declList);
    programState.programInitialization(translationUnit, ShaderKind.COMPUTE);
  }
}
