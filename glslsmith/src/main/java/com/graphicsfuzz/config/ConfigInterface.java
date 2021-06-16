package com.graphicsfuzz.config;

import com.graphicsfuzz.common.glslversion.ShadingLanguageVersion;

public interface ConfigInterface {
  boolean allowEmptySwitch();

  boolean allowMultipleWriteAccessInInitializers();

  boolean enforceDefaultCase();

  boolean allowArrayAbsAccess();

  int getMaxArrayLength();

  int getMaxBufferElements();

  int getMaxExprDepth();

  int getMaxInputBuffers();

  int getMaxLocalSizeX();

  int getMaxLocalSizeY();

  int getMaxLocalSizeZ();

  int getMaxMainLength();

  int getMaxOutputBuffers();

  int getMaxScopeDepth();

  int getMaxSwitchCases();

  int getMaxSwitchScopeLength();

  int getMaxSwizzleDepth();

  int getMaxVardeclElements();

  int getMaxWhileScopeLength();

  ShadingLanguageVersion getShadingLanguageVersion();
}
