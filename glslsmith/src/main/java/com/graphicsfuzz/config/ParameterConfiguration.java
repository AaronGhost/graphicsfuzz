package com.graphicsfuzz.config;

import com.graphicsfuzz.common.glslversion.ShadingLanguageVersion;
import com.graphicsfuzz.common.util.ShaderKind;

public class ParameterConfiguration implements ConfigInterface {
  private ShadingLanguageVersion shadingLanguageVersion;
  private ShaderKind shaderKind;
  private int maxLocalSizeX;
  private int maxLocalSizeY;
  private int maxLocalSizeZ;
  private int maxInputBuffers;
  private int maxOutputBuffers;
  private int maxBufferElements;
  private int maxArrayLength;
  private boolean multipleInitializerWriteAccesses;
  private int maxMainLength;
  private int maxScopeDepth;
  private int maxExprDepth;
  private int maxVarDeclElements;
  private long maxForIncrement;
  private int maxForLength;
  private int maxGlobalDecls;
  private int maxSwizzleDepth;
  private int maxSwitchScopeLength;
  private int maxSwitchCases;
  private boolean emptySwitch;
  private boolean defaultSwitchCase;
  private int maxWhileScopeLength;
  private boolean floatAsConst;
  private boolean singleTypePerBuffer;
  private boolean typeDecoratorsOnBuffers;

  //Language version
  @Override
  public ShadingLanguageVersion getShadingLanguageVersion() {
    return shadingLanguageVersion;
  }

  @Override
  public ShaderKind getShaderKind() {
    return shaderKind;
  }

  //Compute shader Local size parameters
  @Override
  public int getMaxLocalSizeX() {
    return maxLocalSizeX;
  }

  @Override
  public int getMaxLocalSizeY() {
    return maxLocalSizeY;
  }

  @Override
  public int getMaxLocalSizeZ() {
    return maxLocalSizeZ;
  }

  //buffer generations parameters
  @Override
  public int getMaxInputBuffers() {
    return maxInputBuffers;
  }

  @Override
  public int getMaxOutputBuffers() {
    return maxOutputBuffers;
  }

  @Override
  public int getMaxBufferElements() {
    return maxBufferElements;
  }

  //Arrays parameters
  @Override
  public int getMaxArrayLength() {
    return maxArrayLength;
  }

  @Override
  public boolean allowMultipleWriteAccessInInitializers() {
    return multipleInitializerWriteAccesses;
  }

  //Program size parameters
  @Override
  public int getMaxMainLength() {
    return maxMainLength;
  }

  @Override
  public int getMaxScopeDepth() {
    return maxScopeDepth;
  }

  @Override
  public int getMaxExprDepth() {
    return maxExprDepth;
  }

  @Override
  public Long getMaxForIncrement() {
    return maxForIncrement;
  }

  @Override
  public int getMaxForLength() {
    return maxForLength;
  }

  @Override
  public int getMaxGlobalDecls() {
    return maxGlobalDecls;
  }

  @Override
  public int getMaxSwizzleDepth() {
    return maxSwizzleDepth;
  }

  @Override
  public int getMaxVardeclElements() {
    return maxVarDeclElements;
  }

  //Switch parameters
  @Override
  public int getMaxSwitchScopeLength() {
    return maxSwitchScopeLength;
  }

  @Override
  public int getMaxSwitchCases() {
    return maxSwitchCases;
  }

  @Override
  public boolean allowEmptySwitch() {
    return emptySwitch;
  }

  @Override
  public boolean enforceDefaultCase() {
    return defaultSwitchCase;
  }

  //Loop parameters
  @Override
  public int getMaxWhileScopeLength() {
    return maxWhileScopeLength;
  }

  @Override
  public boolean enforceFloatsAsConst() {
    return floatAsConst;
  }

  @Override
  public boolean enforceSingleTypePerBuffer() {
    return singleTypePerBuffer;
  }

  @Override
  public boolean addTypeQualifierOnBuffers() {
    return typeDecoratorsOnBuffers;
  }

  public static class Builder {
    private ShadingLanguageVersion shadingLanguageVersion = ShadingLanguageVersion.ESSL_310;
    private ShaderKind shaderKind = ShaderKind.COMPUTE;
    private int maxLocalSizeX = 2;
    private int maxLocalSizeY = 2;
    private int maxLocalSizeZ = 2;
    private int maxInputBuffers = 5;
    private int maxOutputBuffers = 3;
    private int maxBufferElements = 4;
    private int maxArrayLength = 10;
    private boolean multipleInitializerWriteAccesses = false;
    private int maxMainLength = 25;
    private int maxScopeDepth = 3;
    private int maxExprDepth = 4;
    private int maxVarDeclElements = 2;
    private long maxForIncrement = 10L;
    private int maxForLength = 25;
    private int maxGlobalDecls = 0;
    private int maxSwizzleDepth = 3;
    private int maxSwitchScopeLength = 5;
    private int maxSwitchCases = 10;
    private boolean emptySwitch = false;
    private boolean defaultSwitchCase = false;
    private int maxWhileScopeLength = 10;
    private boolean floatAsConst = false;
    private boolean singleTypePerBuffer = false;
    private boolean typeDecoratorsOnBuffers = true;

    public Builder getBuilder() {
      return new Builder();
    }

    public Builder withShadingLanguageVersion(ShadingLanguageVersion shadingLanguageVersion) {
      this.shadingLanguageVersion = shadingLanguageVersion;
      return this;
    }

    public Builder withShaderKind(ShaderKind shaderKind) {
      this.shaderKind = shaderKind;
      return this;
    }

    public Builder withMaxLocalSizeX(int maxLocalSizeX) {
      this.maxLocalSizeX = maxLocalSizeX;
      return this;
    }

    public Builder withMaxLocalSizeY(int maxLocalSizeY) {
      this.maxLocalSizeY = maxLocalSizeY;
      return this;
    }

    public Builder withMaxLocalSizeZ(int maxLocalSizeZ) {
      this.maxLocalSizeZ = maxLocalSizeZ;
      return this;
    }

    public Builder withMaxInputBuffers(int maxInputBuffers) {
      this.maxInputBuffers = maxInputBuffers;
      return this;
    }

    public Builder withMaxOutputBuffers(int maxOutputBuffers) {
      this.maxOutputBuffers = maxOutputBuffers;
      return this;
    }

    public Builder withMaxBufferElements(int maxBufferElements) {
      this.maxBufferElements = maxBufferElements;
      return this;
    }

    public Builder withMaxArrayLength(int maxArrayLength) {
      this.maxArrayLength = maxArrayLength;
      return this;
    }

    public Builder withMultipleInitializerWriteAccesses(boolean multipleInitializerWriteAccesses) {
      this.multipleInitializerWriteAccesses = multipleInitializerWriteAccesses;
      return this;
    }

    public Builder withMaxMainLength(int maxMainLength) {
      this.maxMainLength = maxMainLength;
      return this;
    }

    public Builder withMaxScopeDepth(int maxScopeDepth) {
      this.maxScopeDepth = maxScopeDepth;
      return this;
    }

    public Builder withMaxExprDepth(int maxExprDepth) {
      this.maxExprDepth = maxExprDepth;
      return this;
    }

    public Builder withMaxVarDeclElements(int maxVarDeclElements) {
      this.maxVarDeclElements = maxVarDeclElements;
      return this;
    }

    public Builder withMaxForIncrement(long maxForIncrement) {
      this.maxForIncrement = maxForIncrement;
      return this;
    }

    public Builder withMaxForLength(int maxForLength) {
      this.maxForLength = maxForLength;
      return this;
    }

    public Builder withMaxGlobalDecls(int maxGlobalDecls) {
      this.maxGlobalDecls = maxGlobalDecls;
      return this;
    }

    public Builder withMaxSwizzleDepth(int maxSwizzleDepth) {
      this.maxSwizzleDepth = maxSwizzleDepth;
      return this;
    }

    public Builder withMaxSwitchScopeLength(int maxSwitchScopeLength) {
      this.maxSwitchScopeLength = maxSwitchScopeLength;
      return this;
    }

    public Builder withMaxSwitchCases(int maxSwitchCases) {
      this.maxSwitchCases = maxSwitchCases;
      return this;
    }

    public Builder withEmptySwitch(boolean emptySwitch) {
      this.emptySwitch = emptySwitch;
      return this;
    }

    public Builder withDefaultSwitchCase(boolean defaultSwitchCase) {
      this.defaultSwitchCase = defaultSwitchCase;
      return this;
    }

    public Builder withMaxWhileScopeLength(int maxWhileScopeLength) {
      this.maxWhileScopeLength = maxWhileScopeLength;
      return this;
    }

    public Builder withFloatAsConst(boolean floatAsConst) {
      this.floatAsConst = floatAsConst;
      return this;
    }

    public Builder withSingleTypePerBuffer(boolean singleTypePerBuffer) {
      this.singleTypePerBuffer = singleTypePerBuffer;
      return this;
    }

    public Builder withTypeDecoratorsOnBuffers(boolean typeDecoratorsOnBuffers) {
      this.typeDecoratorsOnBuffers = typeDecoratorsOnBuffers;
      return this;
    }

    public ParameterConfiguration getConfig() {
      ParameterConfiguration configuration = new ParameterConfiguration();
      configuration.shadingLanguageVersion = this.shadingLanguageVersion;
      configuration.shaderKind = this.shaderKind;
      configuration.maxLocalSizeX = this.maxLocalSizeX;
      configuration.maxLocalSizeY = this.maxLocalSizeY;
      configuration.maxLocalSizeZ = this.maxLocalSizeZ;
      configuration.maxInputBuffers = this.maxInputBuffers;
      configuration.maxOutputBuffers = this.maxOutputBuffers;
      configuration.maxBufferElements = this.maxBufferElements;
      configuration.maxArrayLength = this.maxArrayLength;
      configuration.multipleInitializerWriteAccesses = this.multipleInitializerWriteAccesses;
      configuration.maxMainLength = this.maxMainLength;
      configuration.maxScopeDepth = this.maxScopeDepth;
      configuration.maxExprDepth = this.maxExprDepth;
      configuration.maxVarDeclElements = this.maxVarDeclElements;
      configuration.maxForIncrement = this.maxForIncrement;
      configuration.maxForLength = this.maxForLength;
      configuration.maxGlobalDecls = this.maxGlobalDecls;
      configuration.maxSwizzleDepth = this.maxSwizzleDepth;
      configuration.maxSwitchScopeLength = this.maxSwitchScopeLength;
      configuration.maxSwitchCases = this.maxSwitchCases;
      configuration.emptySwitch = this.emptySwitch;
      configuration.defaultSwitchCase = this.defaultSwitchCase;
      configuration.maxWhileScopeLength = this.maxWhileScopeLength;
      configuration.floatAsConst = this.floatAsConst;
      configuration.singleTypePerBuffer = this.singleTypePerBuffer;
      configuration.typeDecoratorsOnBuffers = this.typeDecoratorsOnBuffers;
      return configuration;
    }
  }

  private ParameterConfiguration(){

  }
}
