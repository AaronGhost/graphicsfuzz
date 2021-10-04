package com.graphicsfuzz.config;

import com.graphicsfuzz.common.glslversion.ShadingLanguageVersion;
import com.graphicsfuzz.common.util.ShaderKind;

public interface ConfigInterface {
  boolean allowEmptySwitch();

  boolean enforceSingleTypePerBuffer();

  boolean addTypeQualifierOnBuffers();

  boolean allowMultipleWriteAccessInInitializers();

  boolean enforceDefaultCase();

  int getMaxArrayLength();

  int getMaxBufferElements();

  int getMaxExprDepth();

  Long getMaxForIncrement();

  int getMaxForLength();

  int getMaxGlobalDecls();

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

  boolean enforceFloatsAsConst();

  ShadingLanguageVersion getShadingLanguageVersion();

  ShaderKind getShaderKind();
}
