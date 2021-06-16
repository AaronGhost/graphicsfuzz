package com.graphicsfuzz.config;

import com.graphicsfuzz.common.glslversion.ShadingLanguageVersion;

public class DefaultConfig implements ConfigInterface {

  //Language version
  @Override
  public ShadingLanguageVersion getShadingLanguageVersion() {
    return ShadingLanguageVersion.ESSL_310;
  }

  //Compute shader Local size parameters
  @Override
  public int getMaxLocalSizeX() {
    return 2;
  }

  @Override
  public int getMaxLocalSizeY() {
    return 2;
  }

  @Override
  public int getMaxLocalSizeZ() {
    return 2;
  }

  //buffer generations parameters
  @Override
  public int getMaxInputBuffers() {
    return 5;
  }

  @Override
  public int getMaxOutputBuffers() {
    return 3;
  }

  @Override
  public int getMaxBufferElements() {
    return 4;
  }

  //Arrays parameters
  @Override
  public int getMaxArrayLength() {
    return 10;
  }

  @Override
  public boolean allowMultipleWriteAccessInInitializers() {
    return false;
  }

  @Override
  public boolean allowArrayAbsAccess() {
    return true;
  }

  //Program size parameters
  @Override
  public int getMaxMainLength() {
    return 15;
  }

  @Override
  public int getMaxScopeDepth() {
    return 3;
  }

  @Override
  public int getMaxExprDepth() {
    return 4;
  }

  @Override
  public int getMaxSwizzleDepth() {
    return 3;
  }

  @Override
  public int getMaxVardeclElements() {
    return 2;
  }

  //Switch parameters
  @Override
  public int getMaxSwitchScopeLength() {
    return 5;
  }

  @Override
  public int getMaxSwitchCases() {
    return 10;
  }

  @Override
  public boolean allowEmptySwitch() {
    return true;
  }

  @Override
  public boolean enforceDefaultCase() {
    return false;
  }

  //Loop parameters
  @Override
  public int getMaxWhileScopeLength() {
    return 10;
  }
}
