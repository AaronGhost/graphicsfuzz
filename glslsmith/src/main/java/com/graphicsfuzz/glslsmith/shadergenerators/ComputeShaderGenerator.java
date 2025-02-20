package com.graphicsfuzz.glslsmith.shadergenerators;

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
import com.graphicsfuzz.glslsmith.Buffer;
import com.graphicsfuzz.glslsmith.config.ConfigInterface;
import com.graphicsfuzz.glslsmith.config.FuzzerConstants;
import com.graphicsfuzz.glslsmith.scope.UnifiedTypeInterface;
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
    //TODO add random uniform
    List<Declaration> globalDecls = generateGlobalDecls();
    List<Stmt> statements = generateShaderMain();
    generateShaderSkeleton(globalDecls, statements);
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

  private void internalGenerateBuffer(boolean isInBuffer) {
    final int newMembers = randGen.nextPositiveInt(configuration.getMaxBufferElements());

    //Buffer internal values holders
    final List<Number> values = new ArrayList<>();
    final List<Type> memberTypes = new ArrayList<>();
    final List<TypeQualifier> qualifiers = new ArrayList<>();
    final List<String> memberNames = new ArrayList<>();

    // Buffers that are bound can be coherent, readonly or writeonly (reandonly / writeonly is
    // permitted but not useful at current step)
    // For convenience we choose to have no readonly output buffers

    final boolean coherentBuffer =
        configuration.addTypeQualifierOnBuffers() && randGen.nextBoolean();
    if (coherentBuffer) {
      qualifiers.add(TypeQualifier.COHERENT);
    }
    final boolean readonlyBuffer =
        configuration.addTypeQualifierOnBuffers()
            && isInBuffer && randGen.nextBoolean();
    if (readonlyBuffer) {
      qualifiers.add(TypeQualifier.READONLY);
    }
    final boolean writeonlyBuffer =
        configuration.addTypeQualifierOnBuffers()
            && !readonlyBuffer && randGen.nextBoolean();
    if (writeonlyBuffer) {
      qualifiers.add(TypeQualifier.WRITEONLY);
    }

    BasicType type = randomTypeGenerator.getRandomBaseType(true);

    //Randomly populate internal values
    for (int memberIndex = 0; memberIndex < newMembers; memberIndex++) {
      UnifiedTypeInterface proxy =
          randomTypeGenerator.getBufferElementType(!isInBuffer || writeonlyBuffer, type);
      // Elements can be declared as coherent, readonly or/ and writeonly
      if (!configuration.enforceSingleTypePerBuffer()) {
        type = randomTypeGenerator.getRandomBaseType(true);
      }
      memberTypes.add(proxy.getRealType());
      String name = programState.getNextUniformBufferName();
      memberNames.add(name);
      for (int valueIndex = 0; valueIndex < proxy.getElementSize(); valueIndex++) {
        Type baseType = proxy.getBaseType();
        if (baseType.equals(BasicType.INT)) {
          values.add(randGen.nextInt(FuzzerConstants.MIN_INT_VALUE, FuzzerConstants.MAX_INT_VALUE));
        } else if (baseType.equals(BasicType.UINT)) {
          values.add(randGen.nextLong(FuzzerConstants.MAX_UINT_VALUE));
        } else {
          //TODO add Fuzzer constants
          values.add(randGen.nextFloat(-(1 << 12), (1 << 12)));
        }
      }

      //Adds the buffer qualifiers to the member qualifiers for scope purpose
      if (coherentBuffer) {
        proxy.setCoherent(true);
      }
      if (readonlyBuffer) {
        proxy.setReadOnly(true);
      }
      if (writeonlyBuffer) {
        proxy.setWriteOnly(true);
      }
      programState.addUniformBufferVariable(name, proxy);
    }

    qualifiers.add(TypeQualifier.BUFFER);

    //Create the correct buffer object
    Buffer inputBuffer = new Buffer("buffer_" + programState.getBindingOffset(),
        //Std430 ensures that all buffers are always stored the same way in the interface blocks
        new LayoutQualifierSequence(new Std430LayoutQualifier(),
            new BindingLayoutQualifier(programState.getBindingOffset())),
        values, qualifiers, memberNames, memberTypes, "",
        isInBuffer, programState.getBindingOffset());
    programState.addBuffer(inputBuffer);
  }

  protected List<Declaration> generateGlobalDecls() {
    List<Declaration> globalDecls = new ArrayList<>();
    if (configuration.getMaxGlobalDecls() == 0) {
      return globalDecls;
    }
    for (int i = 0; i  < randGen.nextPositiveInt(configuration.getMaxGlobalDecls()); i++) {
      globalDecls.add(generateRandomTypedVarDecls(
          randGen.nextPositiveInt(configuration.getMaxVardeclElements()),
          true));
    }
    return globalDecls;
  }

  protected List<Stmt> generateShaderMain() {
    return generateScope();
  }

  protected void generateShaderSkeleton(List<Declaration> globalDecls, List<Stmt> mainStatements) {
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

    // Add global statements
    declList.addAll(globalDecls);

    //Generate an empty main function
    FunctionDefinition mainFunction = new FunctionDefinition(new FunctionPrototype("main",
        VoidType.VOID, new ArrayList<>()), new BlockStmt(mainStatements, true));
    declList.add(mainFunction);

    //Generate the translation unit and add it to the programState
    TranslationUnit translationUnit = new TranslationUnit(ShaderKind.COMPUTE,
        Optional.of(configuration.getShadingLanguageVersion()), declList);
    programState.programInitialization(translationUnit);
  }
}
