package com.graphicsfuzz.glslsmith.functions;

import com.graphicsfuzz.common.ast.type.BasicType;
import com.graphicsfuzz.common.ast.type.QualifiedType;
import com.graphicsfuzz.common.ast.type.TypeQualifier;
import com.graphicsfuzz.common.ast.type.VoidType;
import com.graphicsfuzz.common.util.IRandom;
import com.graphicsfuzz.common.util.ShaderKind;
import com.graphicsfuzz.glslsmith.scope.UnifiedTypeProxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FunctionRegistry {
  private final IRandom randGen;
  private final Map<UnifiedTypeProxy, List<FunctionStruct>> stdFunctions = new HashMap<>();
  private final Map<UnifiedTypeProxy, List<FunctionStruct>> userDefinedFunctions = new HashMap<>();
  private final ShaderKind shaderKind;

  public FunctionRegistry(IRandom randGen, ShaderKind shaderKind) {
    this.randGen = randGen;
    this.shaderKind = shaderKind;
    final List<FunctionStruct> intReturnFunctions = new ArrayList<>();
    final List<FunctionStruct> uintReturnFunctions = new ArrayList<>();
    final List<FunctionStruct> boolReturnFunctions = new ArrayList<>();
    final List<FunctionStruct> uvec2ReturnFunctions = new ArrayList<>();
    final List<FunctionStruct> uvec3ReturnFunctions = new ArrayList<>();
    final List<FunctionStruct> uvec4ReturnFunctions = new ArrayList<>();
    final List<FunctionStruct> ivec2ReturnFunctions = new ArrayList<>();
    final List<FunctionStruct> ivec3ReturnFunctions = new ArrayList<>();
    final List<FunctionStruct> ivec4ReturnFunctions = new ArrayList<>();
    final List<FunctionStruct> bvec2ReturnFunctions = new ArrayList<>();
    final List<FunctionStruct> bvec3ReturnFunctions = new ArrayList<>();
    final List<FunctionStruct> bvec4ReturnFunctions = new ArrayList<>();
    final List<FunctionStruct> floatReturnFunctions = new ArrayList<>();
    final List<FunctionStruct> vec2ReturnFunctions = new ArrayList<>();
    final List<FunctionStruct> vec3ReturnFunctions = new ArrayList<>();
    final List<FunctionStruct> vec4ReturnFunctions = new ArrayList<>();
    final List<FunctionStruct> voidReturnFunctions = new ArrayList<>();

    // Build the std registry

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Explicit conversions
    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Conversiosn to element types from all types ok
    List<BasicType> allTypesExceptMatrices = BasicType.allNonMatrixNumericTypes();
    allTypesExceptMatrices.addAll(BasicType.allBoolTypes());
    for (BasicType type : allTypesExceptMatrices) {
      intReturnFunctions.add(new FunctionStruct("int", new UnifiedTypeProxy(BasicType.INT),
          true, new UnifiedTypeProxy(type)));
      uintReturnFunctions.add(new FunctionStruct("uint", new UnifiedTypeProxy(BasicType.UINT),
          true, new UnifiedTypeProxy(type)));
      boolReturnFunctions.add(new FunctionStruct("bool", new UnifiedTypeProxy(BasicType.BOOL),
          true, new UnifiedTypeProxy(type)));
      floatReturnFunctions.add(new FunctionStruct("float", new UnifiedTypeProxy(BasicType.FLOAT),
          true, new UnifiedTypeProxy(type)));
    }

    // conversions to 2 elements vectors from base elements
    List<BasicType> allScalar = BasicType.allScalarTypes();
    for (BasicType elementOne : allScalar) {
      for (BasicType elementTwo : allTypesExceptMatrices) {
        ivec2ReturnFunctions.add(new FunctionStruct("ivec2", new UnifiedTypeProxy(BasicType.IVEC2),
            true, new UnifiedTypeProxy(elementOne), new UnifiedTypeProxy(elementTwo)));
        uvec2ReturnFunctions.add(new FunctionStruct("uvec2",
            new UnifiedTypeProxy(BasicType.UVEC2), true, new UnifiedTypeProxy(elementOne),
            new UnifiedTypeProxy(elementTwo)));
        bvec2ReturnFunctions.add(new FunctionStruct("bvec2",
            new UnifiedTypeProxy(BasicType.BVEC2), true, new UnifiedTypeProxy(elementOne),
            new UnifiedTypeProxy(elementTwo)));
        vec2ReturnFunctions.add(new FunctionStruct("vec2",
            new UnifiedTypeProxy(BasicType.VEC2), true, new UnifiedTypeProxy(elementOne),
            new UnifiedTypeProxy(elementTwo)));
      }
    }

    // Conversions to 2 elements vectors from all other types (vectors are truncated)
    List<BasicType> allVectorTypes = BasicType.allVectorTypes();
    for (BasicType parameter : allVectorTypes) {
      ivec2ReturnFunctions.add(new FunctionStruct("ivec2", new UnifiedTypeProxy(BasicType.IVEC2),
          true, new UnifiedTypeProxy(parameter)));
      uvec2ReturnFunctions.add(new FunctionStruct("uvec2", new UnifiedTypeProxy(BasicType.UVEC2),
          true, new UnifiedTypeProxy(parameter)));
      bvec2ReturnFunctions.add(new FunctionStruct("bvec2", new UnifiedTypeProxy(BasicType.BVEC2),
          true, new UnifiedTypeProxy(parameter)));
      vec2ReturnFunctions.add(new FunctionStruct("vec2", new UnifiedTypeProxy(BasicType.VEC2),
          true, new UnifiedTypeProxy(parameter)));
    }

    // Conversions to 3 elements vectors from base elements
    for (BasicType parameterOne : allScalar) {
      for (BasicType parameterTwo : allScalar) {
        for (BasicType parameterThree : allTypesExceptMatrices) {
          ivec3ReturnFunctions.add(new FunctionStruct("ivec3",
              new UnifiedTypeProxy(BasicType.IVEC3), true, new UnifiedTypeProxy(parameterOne),
              new UnifiedTypeProxy(parameterTwo), new UnifiedTypeProxy(parameterThree)));
          uvec3ReturnFunctions.add(new FunctionStruct("uvec3",
              new UnifiedTypeProxy(BasicType.UVEC3), true, new UnifiedTypeProxy(parameterOne),
              new UnifiedTypeProxy(parameterTwo), new UnifiedTypeProxy(parameterThree)));
          bvec3ReturnFunctions.add(new FunctionStruct("bvec3",
              new UnifiedTypeProxy(BasicType.BVEC3), true, new UnifiedTypeProxy(parameterOne),
              new UnifiedTypeProxy(parameterTwo), new UnifiedTypeProxy(parameterThree)));
          vec3ReturnFunctions.add(new FunctionStruct("vec3",
              new UnifiedTypeProxy(BasicType.VEC3), true, new UnifiedTypeProxy(parameterOne),
              new UnifiedTypeProxy(parameterTwo), new UnifiedTypeProxy(parameterThree)));
        }
      }
    }

    List<BasicType> xvec2 = Arrays.asList(BasicType.IVEC2, BasicType.UVEC2,
        BasicType.BVEC2, BasicType.VEC2);

    List<BasicType> xvec3AndXvec4 = Arrays.asList(BasicType.IVEC3, BasicType.UVEC3,
        BasicType.BVEC3, BasicType.IVEC4, BasicType.UVEC4, BasicType.BVEC4, BasicType.VEC3,
        BasicType.VEC4);
    // Conversions to 3 elements vectors from a base element and a vector element
    for (BasicType parameterOne : allScalar) {
      for (BasicType parameterTwo : allVectorTypes) {
        ivec3ReturnFunctions.add(new FunctionStruct("ivec3",
            new UnifiedTypeProxy(BasicType.IVEC3), true, new UnifiedTypeProxy(parameterOne),
            new UnifiedTypeProxy(parameterTwo)));
        uvec3ReturnFunctions.add(new FunctionStruct("uvec3",
            new UnifiedTypeProxy(BasicType.UVEC3), true, new UnifiedTypeProxy(parameterOne),
            new UnifiedTypeProxy(parameterTwo)));
        bvec3ReturnFunctions.add(new FunctionStruct("bvec3",
            new UnifiedTypeProxy(BasicType.BVEC3), true, new UnifiedTypeProxy(parameterOne),
            new UnifiedTypeProxy(parameterTwo)));
        vec3ReturnFunctions.add(new FunctionStruct("vec3",
            new UnifiedTypeProxy(BasicType.VEC3), true, new UnifiedTypeProxy(parameterOne),
            new UnifiedTypeProxy(parameterTwo)));
      }
    }

    // Conversions to 3 elements vectors from a 2-vector on the left and a vector element on the
    // right
    for (BasicType parameterOne : xvec2) {
      for (BasicType parameterTwo : allTypesExceptMatrices) {
        ivec3ReturnFunctions.add(new FunctionStruct("ivec3",
            new UnifiedTypeProxy(BasicType.IVEC3), true, new UnifiedTypeProxy(parameterOne),
            new UnifiedTypeProxy(parameterTwo)));
        uvec3ReturnFunctions.add(new FunctionStruct("uvec3",
            new UnifiedTypeProxy(BasicType.UVEC3), true, new UnifiedTypeProxy(parameterOne),
            new UnifiedTypeProxy(parameterTwo)));
        bvec3ReturnFunctions.add(new FunctionStruct("bvec3",
            new UnifiedTypeProxy(BasicType.BVEC3), true, new UnifiedTypeProxy(parameterOne),
            new UnifiedTypeProxy(parameterTwo)));
        vec3ReturnFunctions.add(new FunctionStruct("vec3",
            new UnifiedTypeProxy(BasicType.VEC3), true, new UnifiedTypeProxy(parameterOne),
            new UnifiedTypeProxy(parameterTwo)));
      }
    }

    // Conversions to 3 elements vector from a 3 or 4 element vector
    for (BasicType parameter : xvec3AndXvec4) {
      ivec3ReturnFunctions.add(new FunctionStruct("ivec3", new UnifiedTypeProxy(BasicType.IVEC3),
          true, new UnifiedTypeProxy(parameter)));
      uvec3ReturnFunctions.add(new FunctionStruct("uvec3", new UnifiedTypeProxy(BasicType.UVEC3),
          true, new UnifiedTypeProxy(parameter)));
      bvec3ReturnFunctions.add(new FunctionStruct("bvec3", new UnifiedTypeProxy(BasicType.BVEC3),
          true, new UnifiedTypeProxy(parameter)));
      vec3ReturnFunctions.add(new FunctionStruct("vec3", new UnifiedTypeProxy(BasicType.VEC3),
          true, new UnifiedTypeProxy(parameter)));
    }

    // Conversions to 4 elements vectors from base elements
    for (BasicType parameterOne : allScalar) {
      for (BasicType parameterTwo : allScalar) {
        for (BasicType parameterThree : allScalar) {
          for (BasicType parameterFour : allTypesExceptMatrices) {
            ivec4ReturnFunctions.add(new FunctionStruct("ivec4",
                new UnifiedTypeProxy(BasicType.IVEC4), true, new UnifiedTypeProxy(parameterOne),
                new UnifiedTypeProxy(parameterTwo), new UnifiedTypeProxy(parameterThree),
                new UnifiedTypeProxy(parameterFour)));
            uvec4ReturnFunctions.add(new FunctionStruct("uvec4",
                new UnifiedTypeProxy(BasicType.UVEC4), true, new UnifiedTypeProxy(parameterOne),
                new UnifiedTypeProxy(parameterTwo), new UnifiedTypeProxy(parameterThree),
                new UnifiedTypeProxy(parameterFour)));
            bvec4ReturnFunctions.add(new FunctionStruct("bvec4",
                new UnifiedTypeProxy(BasicType.BVEC4), true, new UnifiedTypeProxy(parameterOne),
                new UnifiedTypeProxy(parameterTwo), new UnifiedTypeProxy(parameterThree),
                new UnifiedTypeProxy(parameterFour)));
            bvec4ReturnFunctions.add(new FunctionStruct("vec4",
                new UnifiedTypeProxy(BasicType.VEC4), true, new UnifiedTypeProxy(parameterOne),
                new UnifiedTypeProxy(parameterTwo), new UnifiedTypeProxy(parameterThree),
                new UnifiedTypeProxy(parameterFour)));
          }
        }
      }
    }

    // Conversions to 4 elements from 2 base elements + a vector element
    for (BasicType parameterOne : allScalar) {
      for (BasicType parameterTwo : allScalar) {
        for (BasicType parameterThree : allVectorTypes) {
          ivec4ReturnFunctions.add(new FunctionStruct("ivec4",
              new UnifiedTypeProxy(BasicType.IVEC4), true, new UnifiedTypeProxy(parameterOne),
              new UnifiedTypeProxy(parameterTwo), new UnifiedTypeProxy(parameterThree)));
          uvec4ReturnFunctions.add(new FunctionStruct("uvec4",
              new UnifiedTypeProxy(BasicType.UVEC4), true, new UnifiedTypeProxy(parameterOne),
              new UnifiedTypeProxy(parameterTwo), new UnifiedTypeProxy(parameterThree)));
          bvec4ReturnFunctions.add(new FunctionStruct("bvec4",
              new UnifiedTypeProxy(BasicType.BVEC4), true, new UnifiedTypeProxy(parameterOne),
              new UnifiedTypeProxy(parameterTwo), new UnifiedTypeProxy(parameterThree)));
          vec4ReturnFunctions.add(new FunctionStruct("vec4",
              new UnifiedTypeProxy(BasicType.VEC4), true, new UnifiedTypeProxy(parameterOne),
              new UnifiedTypeProxy(parameterTwo), new UnifiedTypeProxy(parameterThree)));
        }
      }
    }

    // Conversion to a 4 elements vector from a 2-element vector + base element + something
    for (BasicType parameterOne : allScalar) {
      for (BasicType parameterTwo : xvec2) {
        for (BasicType parameterThree : allTypesExceptMatrices) {
          // Scalar + 2-vec + something
          ivec4ReturnFunctions.add(new FunctionStruct("ivec4",
              new UnifiedTypeProxy(BasicType.IVEC4), true, new UnifiedTypeProxy(parameterOne),
              new UnifiedTypeProxy(parameterTwo), new UnifiedTypeProxy(parameterThree)));
          uvec4ReturnFunctions.add(new FunctionStruct("uvec4",
              new UnifiedTypeProxy(BasicType.UVEC4), true, new UnifiedTypeProxy(parameterOne),
              new UnifiedTypeProxy(parameterTwo), new UnifiedTypeProxy(parameterThree)));
          bvec4ReturnFunctions.add(new FunctionStruct("bvec4",
              new UnifiedTypeProxy(BasicType.BVEC4), true, new UnifiedTypeProxy(parameterOne),
              new UnifiedTypeProxy(parameterTwo), new UnifiedTypeProxy(parameterThree)));
          vec4ReturnFunctions.add(new FunctionStruct("vec4",
              new UnifiedTypeProxy(BasicType.VEC4), true, new UnifiedTypeProxy(parameterOne),
              new UnifiedTypeProxy(parameterTwo), new UnifiedTypeProxy(parameterThree)));
          // 2-vec + Scalar + something
          ivec4ReturnFunctions.add(new FunctionStruct("ivec4",
              new UnifiedTypeProxy(BasicType.IVEC4), true, new UnifiedTypeProxy(parameterTwo),
              new UnifiedTypeProxy(parameterOne), new UnifiedTypeProxy(parameterThree)));
          uvec4ReturnFunctions.add(new FunctionStruct("uvec4",
              new UnifiedTypeProxy(BasicType.UVEC4), true, new UnifiedTypeProxy(parameterTwo),
              new UnifiedTypeProxy(parameterOne), new UnifiedTypeProxy(parameterThree)));
          bvec4ReturnFunctions.add(new FunctionStruct("bvec4",
              new UnifiedTypeProxy(BasicType.BVEC4), true, new UnifiedTypeProxy(parameterTwo),
              new UnifiedTypeProxy(parameterOne), new UnifiedTypeProxy(parameterThree)));
          vec4ReturnFunctions.add(new FunctionStruct("vec4",
              new UnifiedTypeProxy(BasicType.VEC4), true, new UnifiedTypeProxy(parameterTwo),
              new UnifiedTypeProxy(parameterOne), new UnifiedTypeProxy(parameterThree)));
        }
      }
    }

    // Conversion to a 4 elements vector from a 2-element vector + vector
    for (BasicType parameterOne : xvec2) {
      for (BasicType parameterTwo : allVectorTypes) {
        ivec4ReturnFunctions.add(new FunctionStruct("ivec4",
            new UnifiedTypeProxy(BasicType.IVEC4), true, new UnifiedTypeProxy(parameterOne),
            new UnifiedTypeProxy(parameterTwo)));
        uvec4ReturnFunctions.add(new FunctionStruct("uvec4",
            new UnifiedTypeProxy(BasicType.UVEC4), true, new UnifiedTypeProxy(parameterOne),
            new UnifiedTypeProxy(parameterTwo)));
        bvec4ReturnFunctions.add(new FunctionStruct("bvec4",
            new UnifiedTypeProxy(BasicType.BVEC4), true, new UnifiedTypeProxy(parameterOne),
            new UnifiedTypeProxy(parameterTwo)));
        vec4ReturnFunctions.add(new FunctionStruct("vec4",
            new UnifiedTypeProxy(BasicType.VEC4), true, new UnifiedTypeProxy(parameterOne),
            new UnifiedTypeProxy(parameterTwo)));
      }
    }
    List<BasicType> xvec3 = Arrays.asList(BasicType.IVEC3, BasicType.UVEC3,
        BasicType.BVEC3, BasicType.VEC3);

    // Conversion to a 4 elements vector from a 3 elements vector + something
    for (BasicType parameterOne : xvec3) {
      for (BasicType parameterTwo : allTypesExceptMatrices) {
        ivec4ReturnFunctions.add(new FunctionStruct("ivec4",
            new UnifiedTypeProxy(BasicType.IVEC4), true, new UnifiedTypeProxy(parameterOne),
            new UnifiedTypeProxy(parameterTwo)));
        uvec4ReturnFunctions.add(new FunctionStruct("uvec4",
            new UnifiedTypeProxy(BasicType.UVEC4), true, new UnifiedTypeProxy(parameterOne),
            new UnifiedTypeProxy(parameterTwo)));
        bvec4ReturnFunctions.add(new FunctionStruct("bvec4",
            new UnifiedTypeProxy(BasicType.BVEC4), true, new UnifiedTypeProxy(parameterOne),
            new UnifiedTypeProxy(parameterTwo)));
        vec4ReturnFunctions.add(new FunctionStruct("vec4",
            new UnifiedTypeProxy(BasicType.VEC4), true, new UnifiedTypeProxy(parameterOne),
            new UnifiedTypeProxy(parameterTwo)));
      }
    }

    // Conversion to a 4 elements vector from a base element + 3/4 elements vector
    for (BasicType parameterOne : allScalar) {
      for (BasicType parameterTwo : xvec3AndXvec4) {
        ivec4ReturnFunctions.add(new FunctionStruct("ivec4",
            new UnifiedTypeProxy(BasicType.IVEC4), true, new UnifiedTypeProxy(parameterOne),
            new UnifiedTypeProxy(parameterTwo)));
        uvec4ReturnFunctions.add(new FunctionStruct("uvec4",
            new UnifiedTypeProxy(BasicType.UVEC4), true, new UnifiedTypeProxy(parameterOne),
            new UnifiedTypeProxy(parameterTwo)));
        bvec4ReturnFunctions.add(new FunctionStruct("bvec4",
            new UnifiedTypeProxy(BasicType.BVEC4), true, new UnifiedTypeProxy(parameterOne),
            new UnifiedTypeProxy(parameterTwo)));
        vec4ReturnFunctions.add(new FunctionStruct("vec4",
            new UnifiedTypeProxy(BasicType.VEC4), true, new UnifiedTypeProxy(parameterOne),
            new UnifiedTypeProxy(parameterTwo)));
      }
    }

    List<BasicType> xvec4 = Arrays.asList(BasicType.IVEC4, BasicType.UVEC4,
        BasicType.BVEC4, BasicType.VEC4);
    // Conversion to a 4 elements vector from another 4 elements vector
    for (BasicType parameter : xvec4) {
      ivec4ReturnFunctions.add(new FunctionStruct("ivec4", new UnifiedTypeProxy(BasicType.IVEC4),
          true, new UnifiedTypeProxy(parameter)));
      uvec4ReturnFunctions.add(new FunctionStruct("uvec4", new UnifiedTypeProxy(BasicType.UVEC4),
          true, new UnifiedTypeProxy(parameter)));
      bvec4ReturnFunctions.add(new FunctionStruct("bvec4", new UnifiedTypeProxy(BasicType.BVEC4),
          true, new UnifiedTypeProxy(parameter)));
      vec4ReturnFunctions.add(new FunctionStruct("vec4", new UnifiedTypeProxy(BasicType.VEC4),
          true, new UnifiedTypeProxy(parameter)));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Common Functions
    ////////////////////////////////////////////////////////////////////////////////////////////////

    for (String callee : Arrays.asList("abs", "sign")) {
      // abs, sign functions for integers
      intReturnFunctions.add(new FunctionStruct(callee, new UnifiedTypeProxy(BasicType.INT),
          true, new UnifiedTypeProxy(BasicType.INT)));
      ivec2ReturnFunctions.add(new FunctionStruct(callee, new UnifiedTypeProxy(BasicType.IVEC2),
          true, new UnifiedTypeProxy(BasicType.IVEC2)));
      ivec3ReturnFunctions.add(new FunctionStruct(callee, new UnifiedTypeProxy(BasicType.IVEC3),
          true, new UnifiedTypeProxy(BasicType.IVEC3)));
      ivec4ReturnFunctions.add(new FunctionStruct(callee, new UnifiedTypeProxy(BasicType.IVEC4),
          true, new UnifiedTypeProxy(BasicType.IVEC4)));

      // abs, sign functions for float
      floatReturnFunctions.add(new FunctionStruct(callee, new UnifiedTypeProxy(BasicType.FLOAT),
          true, new UnifiedTypeProxy(BasicType.FLOAT)));
      vec2ReturnFunctions.add(new FunctionStruct(callee, new UnifiedTypeProxy(BasicType.VEC2),
          true, new UnifiedTypeProxy(BasicType.VEC2)));
      vec3ReturnFunctions.add(new FunctionStruct(callee, new UnifiedTypeProxy(BasicType.VEC3),
          true, new UnifiedTypeProxy(BasicType.VEC3)));
      vec4ReturnFunctions.add(new FunctionStruct(callee, new UnifiedTypeProxy(BasicType.VEC4),
          true, new UnifiedTypeProxy(BasicType.VEC4)));
    }


    // Min, Max functions for integers
    intReturnFunctions.add(new FunctionStruct("min", new UnifiedTypeProxy(BasicType.INT),
        true, new UnifiedTypeProxy(BasicType.INT), new UnifiedTypeProxy(BasicType.INT)));
    ivec2ReturnFunctions.add(new FunctionStruct("min", new UnifiedTypeProxy(BasicType.IVEC2),
        true, new UnifiedTypeProxy(BasicType.IVEC2),
        new UnifiedTypeProxy(BasicType.IVEC2)));
    ivec2ReturnFunctions.add(new FunctionStruct("min", new UnifiedTypeProxy(BasicType.IVEC2),
        true, new UnifiedTypeProxy(BasicType.IVEC2),
        new UnifiedTypeProxy(BasicType.INT)));
    ivec3ReturnFunctions.add(new FunctionStruct("min", new UnifiedTypeProxy(BasicType.IVEC3),
        true, new UnifiedTypeProxy(BasicType.IVEC3),
        new UnifiedTypeProxy(BasicType.IVEC3)));
    ivec3ReturnFunctions.add(new FunctionStruct("min", new UnifiedTypeProxy(BasicType.IVEC3),
        true, new UnifiedTypeProxy(BasicType.IVEC3),
        new UnifiedTypeProxy(BasicType.INT)));
    ivec4ReturnFunctions.add(new FunctionStruct("min", new UnifiedTypeProxy(BasicType.IVEC4),
        true, new UnifiedTypeProxy(BasicType.IVEC4),
        new UnifiedTypeProxy(BasicType.IVEC4)));
    ivec4ReturnFunctions.add(new FunctionStruct("min", new UnifiedTypeProxy(BasicType.IVEC4),
        true, new UnifiedTypeProxy(BasicType.IVEC4),
        new UnifiedTypeProxy(BasicType.INT)));

    // Min, Max functions for unsigned integers
    for (String callee : Arrays.asList("min", "max")) {
      uintReturnFunctions.add(new FunctionStruct(callee, new UnifiedTypeProxy(BasicType.UINT),
          true, new UnifiedTypeProxy(BasicType.UINT),
          new UnifiedTypeProxy(BasicType.UINT)));
      uvec2ReturnFunctions.add(new FunctionStruct(callee, new UnifiedTypeProxy(BasicType.UVEC2),
          true, new UnifiedTypeProxy(BasicType.UVEC2),
          new UnifiedTypeProxy(BasicType.UVEC2)));
      uvec2ReturnFunctions.add(new FunctionStruct(callee, new UnifiedTypeProxy(BasicType.UVEC2),
          true, new UnifiedTypeProxy(BasicType.UVEC2),
          new UnifiedTypeProxy(BasicType.UINT)));
      uvec3ReturnFunctions.add(new FunctionStruct(callee, new UnifiedTypeProxy(BasicType.UVEC3),
          true, new UnifiedTypeProxy(BasicType.UVEC3),
          new UnifiedTypeProxy(BasicType.UVEC3)));
      uvec3ReturnFunctions.add(new FunctionStruct(callee, new UnifiedTypeProxy(BasicType.UVEC3),
          true, new UnifiedTypeProxy(BasicType.UVEC3),
          new UnifiedTypeProxy(BasicType.UINT)));
      uvec4ReturnFunctions.add(new FunctionStruct(callee, new UnifiedTypeProxy(BasicType.UVEC4),
          true, new UnifiedTypeProxy(BasicType.UVEC4),
          new UnifiedTypeProxy(BasicType.UVEC4)));
      uvec4ReturnFunctions.add(new FunctionStruct(callee, new UnifiedTypeProxy(BasicType.UVEC4),
          true, new UnifiedTypeProxy(BasicType.UVEC4),
          new UnifiedTypeProxy(BasicType.UINT)));

      // Min, Max functions for floats
      floatReturnFunctions.add(new FunctionStruct(callee, new UnifiedTypeProxy(BasicType.FLOAT),
          true, new UnifiedTypeProxy(BasicType.FLOAT),
          new UnifiedTypeProxy(BasicType.FLOAT)));
      vec2ReturnFunctions.add(new FunctionStruct(callee, new UnifiedTypeProxy(BasicType.VEC2),
          true, new UnifiedTypeProxy(BasicType.VEC2),
          new UnifiedTypeProxy(BasicType.VEC2)));
      vec2ReturnFunctions.add(new FunctionStruct(callee, new UnifiedTypeProxy(BasicType.VEC2),
          true, new UnifiedTypeProxy(BasicType.VEC2),
          new UnifiedTypeProxy(BasicType.FLOAT)));
      vec3ReturnFunctions.add(new FunctionStruct(callee, new UnifiedTypeProxy(BasicType.VEC3),
          true, new UnifiedTypeProxy(BasicType.VEC3),
          new UnifiedTypeProxy(BasicType.VEC3)));
      vec3ReturnFunctions.add(new FunctionStruct(callee, new UnifiedTypeProxy(BasicType.VEC3),
          true, new UnifiedTypeProxy(BasicType.VEC3),
          new UnifiedTypeProxy(BasicType.FLOAT)));
      vec4ReturnFunctions.add(new FunctionStruct(callee, new UnifiedTypeProxy(BasicType.VEC4),
          true, new UnifiedTypeProxy(BasicType.VEC4),
          new UnifiedTypeProxy(BasicType.VEC4)));
      vec4ReturnFunctions.add(new FunctionStruct(callee, new UnifiedTypeProxy(BasicType.VEC4),
          true, new UnifiedTypeProxy(BasicType.VEC4),
          new UnifiedTypeProxy(BasicType.FLOAT)));
    }

    // Clamp functions for signed integers
    intReturnFunctions.add(new FunctionStruct("clamp", new UnifiedTypeProxy(BasicType.INT),
        true, new UnifiedTypeProxy(BasicType.INT), new UnifiedTypeProxy(BasicType.INT),
        new UnifiedTypeProxy(BasicType.INT)));
    ivec2ReturnFunctions.add(new FunctionStruct("clamp", new UnifiedTypeProxy(BasicType.IVEC2),
        true, new UnifiedTypeProxy(BasicType.IVEC2), new UnifiedTypeProxy(BasicType.IVEC2),
        new UnifiedTypeProxy(BasicType.IVEC2)));
    ivec2ReturnFunctions.add(new FunctionStruct("clamp", new UnifiedTypeProxy(BasicType.IVEC2),
        true, new UnifiedTypeProxy(BasicType.IVEC2), new UnifiedTypeProxy(BasicType.INT),
        new UnifiedTypeProxy(BasicType.INT)));
    ivec3ReturnFunctions.add(new FunctionStruct("clamp", new UnifiedTypeProxy(BasicType.IVEC3),
        true, new UnifiedTypeProxy(BasicType.IVEC3), new UnifiedTypeProxy(BasicType.IVEC3),
        new UnifiedTypeProxy(BasicType.IVEC3)));
    ivec3ReturnFunctions.add(new FunctionStruct("clamp", new UnifiedTypeProxy(BasicType.IVEC3),
        true, new UnifiedTypeProxy(BasicType.IVEC3), new UnifiedTypeProxy(BasicType.INT),
        new UnifiedTypeProxy(BasicType.INT)));
    ivec4ReturnFunctions.add(new FunctionStruct("clamp", new UnifiedTypeProxy(BasicType.IVEC4),
        true, new UnifiedTypeProxy(BasicType.IVEC4), new UnifiedTypeProxy(BasicType.IVEC4),
        new UnifiedTypeProxy(BasicType.IVEC4)));
    ivec4ReturnFunctions.add(new FunctionStruct("clamp", new UnifiedTypeProxy(BasicType.IVEC4),
        true, new UnifiedTypeProxy(BasicType.IVEC4), new UnifiedTypeProxy(BasicType.INT),
        new UnifiedTypeProxy(BasicType.INT)));

    // Clamp functions for unsigned integers
    uintReturnFunctions.add(new FunctionStruct("clamp", new UnifiedTypeProxy(BasicType.UINT),
        true, new UnifiedTypeProxy(BasicType.UINT), new UnifiedTypeProxy(BasicType.UINT),
        new UnifiedTypeProxy(BasicType.UINT)));
    uvec2ReturnFunctions.add(new FunctionStruct("clamp", new UnifiedTypeProxy(BasicType.UVEC2),
        true, new UnifiedTypeProxy(BasicType.UVEC2), new UnifiedTypeProxy(BasicType.UVEC2),
        new UnifiedTypeProxy(BasicType.UVEC2)));
    uvec2ReturnFunctions.add(new FunctionStruct("clamp", new UnifiedTypeProxy(BasicType.UVEC2),
        true, new UnifiedTypeProxy(BasicType.UVEC2), new UnifiedTypeProxy(BasicType.UINT),
        new UnifiedTypeProxy(BasicType.UINT)));
    uvec3ReturnFunctions.add(new FunctionStruct("clamp", new UnifiedTypeProxy(BasicType.UVEC3),
        true, new UnifiedTypeProxy(BasicType.UVEC3), new UnifiedTypeProxy(BasicType.UVEC3),
        new UnifiedTypeProxy(BasicType.UVEC3)));
    uvec3ReturnFunctions.add(new FunctionStruct("clamp", new UnifiedTypeProxy(BasicType.UVEC3),
        true, new UnifiedTypeProxy(BasicType.UVEC3), new UnifiedTypeProxy(BasicType.UINT),
        new UnifiedTypeProxy(BasicType.UINT)));
    uvec4ReturnFunctions.add(new FunctionStruct("clamp", new UnifiedTypeProxy(BasicType.UVEC4),
        true, new UnifiedTypeProxy(BasicType.UVEC4), new UnifiedTypeProxy(BasicType.UVEC4),
        new UnifiedTypeProxy(BasicType.UVEC4)));
    uvec4ReturnFunctions.add(new FunctionStruct("clamp", new UnifiedTypeProxy(BasicType.UVEC4),
        true, new UnifiedTypeProxy(BasicType.UVEC4), new UnifiedTypeProxy(BasicType.UINT),
        new UnifiedTypeProxy(BasicType.UINT)));

    // Mix functions for integers
    intReturnFunctions.add(new FunctionStruct("mix", new UnifiedTypeProxy(BasicType.INT),
        true, new UnifiedTypeProxy(BasicType.INT), new UnifiedTypeProxy(BasicType.INT),
        new UnifiedTypeProxy(BasicType.BOOL)));
    ivec2ReturnFunctions.add(new FunctionStruct("mix", new UnifiedTypeProxy(BasicType.IVEC2),
        true, new UnifiedTypeProxy(BasicType.IVEC2), new UnifiedTypeProxy(BasicType.IVEC2),
        new UnifiedTypeProxy(BasicType.BVEC2)));
    ivec3ReturnFunctions.add(new FunctionStruct("mix", new UnifiedTypeProxy(BasicType.IVEC3),
        true, new UnifiedTypeProxy(BasicType.IVEC3), new UnifiedTypeProxy(BasicType.IVEC3),
        new UnifiedTypeProxy(BasicType.BVEC3)));
    ivec4ReturnFunctions.add(new FunctionStruct("mix", new UnifiedTypeProxy(BasicType.IVEC4),
        true, new UnifiedTypeProxy(BasicType.IVEC4), new UnifiedTypeProxy(BasicType.IVEC4),
        new UnifiedTypeProxy(BasicType.BVEC4)));

    // Mix functions for unsigned integers
    uintReturnFunctions.add(new FunctionStruct("mix", new UnifiedTypeProxy(BasicType.UINT),
        true, new UnifiedTypeProxy(BasicType.UINT), new UnifiedTypeProxy(BasicType.UINT),
        new UnifiedTypeProxy(BasicType.BOOL)));
    uvec2ReturnFunctions.add(new FunctionStruct("mix", new UnifiedTypeProxy(BasicType.UVEC2),
        true, new UnifiedTypeProxy(BasicType.UVEC2), new UnifiedTypeProxy(BasicType.UVEC2),
        new UnifiedTypeProxy(BasicType.BVEC2)));
    uvec3ReturnFunctions.add(new FunctionStruct("mix", new UnifiedTypeProxy(BasicType.UVEC3),
        true, new UnifiedTypeProxy(BasicType.UVEC3), new UnifiedTypeProxy(BasicType.UVEC3),
        new UnifiedTypeProxy(BasicType.BVEC3)));
    uvec4ReturnFunctions.add(new FunctionStruct("mix", new UnifiedTypeProxy(BasicType.UVEC4),
        true, new UnifiedTypeProxy(BasicType.UVEC4), new UnifiedTypeProxy(BasicType.UVEC4),
        new UnifiedTypeProxy(BasicType.BVEC4)));

    // Mix functions for booleans
    boolReturnFunctions.add(new FunctionStruct("mix", new UnifiedTypeProxy(BasicType.BOOL),
        true, new UnifiedTypeProxy(BasicType.BOOL), new UnifiedTypeProxy(BasicType.BOOL),
        new UnifiedTypeProxy(BasicType.BOOL)));
    bvec2ReturnFunctions.add(new FunctionStruct("mix", new UnifiedTypeProxy(BasicType.BVEC2),
        true, new UnifiedTypeProxy(BasicType.BVEC2), new UnifiedTypeProxy(BasicType.BVEC2),
        new UnifiedTypeProxy(BasicType.BVEC2)));
    bvec3ReturnFunctions.add(new FunctionStruct("mix", new UnifiedTypeProxy(BasicType.BVEC3),
        true, new UnifiedTypeProxy(BasicType.BVEC3), new UnifiedTypeProxy(BasicType.BVEC3),
        new UnifiedTypeProxy(BasicType.BVEC3)));
    bvec4ReturnFunctions.add(new FunctionStruct("mix", new UnifiedTypeProxy(BasicType.BVEC4),
        true, new UnifiedTypeProxy(BasicType.BVEC4), new UnifiedTypeProxy(BasicType.BVEC4),
        new UnifiedTypeProxy(BasicType.BVEC4)));

    // Mix functions for floats
    floatReturnFunctions.add(new FunctionStruct("mix", new UnifiedTypeProxy(BasicType.FLOAT),
        true, new UnifiedTypeProxy(BasicType.FLOAT), new UnifiedTypeProxy(BasicType.FLOAT),
        new UnifiedTypeProxy(BasicType.BOOL)));
    vec2ReturnFunctions.add(new FunctionStruct("mix", new UnifiedTypeProxy(BasicType.VEC2),
        true, new UnifiedTypeProxy(BasicType.VEC2), new UnifiedTypeProxy(BasicType.VEC2),
        new UnifiedTypeProxy(BasicType.BVEC2)));
    vec3ReturnFunctions.add(new FunctionStruct("mix", new UnifiedTypeProxy(BasicType.VEC3),
        true, new UnifiedTypeProxy(BasicType.VEC3), new UnifiedTypeProxy(BasicType.VEC3),
        new UnifiedTypeProxy(BasicType.BVEC3)));
    vec4ReturnFunctions.add(new FunctionStruct("mix", new UnifiedTypeProxy(BasicType.VEC4),
        true, new UnifiedTypeProxy(BasicType.VEC4), new UnifiedTypeProxy(BasicType.VEC4),
        new UnifiedTypeProxy(BasicType.BVEC4)));

    // step functions for floats
    floatReturnFunctions.add(new FunctionStruct("step", new UnifiedTypeProxy(BasicType.FLOAT),
        true, new UnifiedTypeProxy(BasicType.FLOAT), new UnifiedTypeProxy(BasicType.FLOAT)));
    vec2ReturnFunctions.add(new FunctionStruct("step", new UnifiedTypeProxy(BasicType.VEC2),
        true, new UnifiedTypeProxy(BasicType.VEC2), new UnifiedTypeProxy(BasicType.VEC2)));
    vec3ReturnFunctions.add(new FunctionStruct("step", new UnifiedTypeProxy(BasicType.VEC3),
        true, new UnifiedTypeProxy(BasicType.VEC3), new UnifiedTypeProxy(BasicType.VEC3)));
    vec4ReturnFunctions.add(new FunctionStruct("step", new UnifiedTypeProxy(BasicType.VEC4),
        true, new UnifiedTypeProxy(BasicType.VEC4), new UnifiedTypeProxy(BasicType.VEC4)));
    vec2ReturnFunctions.add(new FunctionStruct("step", new UnifiedTypeProxy(BasicType.VEC2),
        true, new UnifiedTypeProxy(BasicType.FLOAT), new UnifiedTypeProxy(BasicType.VEC2)));
    vec3ReturnFunctions.add(new FunctionStruct("step", new UnifiedTypeProxy(BasicType.VEC3),
        true, new UnifiedTypeProxy(BasicType.FLOAT), new UnifiedTypeProxy(BasicType.VEC3)));
    vec4ReturnFunctions.add(new FunctionStruct("step", new UnifiedTypeProxy(BasicType.VEC4),
        true, new UnifiedTypeProxy(BasicType.FLOAT), new UnifiedTypeProxy(BasicType.VEC4)));

    // isnan and isinf functions
    for (String callee : Arrays.asList("isnan", "isinf")) {
      boolReturnFunctions.add(new FunctionStruct(callee, new UnifiedTypeProxy(BasicType.BOOL),
          true, new UnifiedTypeProxy(BasicType.FLOAT)));
      bvec2ReturnFunctions.add(new FunctionStruct(callee, new UnifiedTypeProxy(BasicType.BVEC2),
          true, new UnifiedTypeProxy(BasicType.VEC2)));
      bvec3ReturnFunctions.add(new FunctionStruct(callee, new UnifiedTypeProxy(BasicType.BVEC3),
          true, new UnifiedTypeProxy(BasicType.VEC3)));
      bvec4ReturnFunctions.add(new FunctionStruct(callee, new UnifiedTypeProxy(BasicType.BVEC4),
          true, new UnifiedTypeProxy(BasicType.VEC4)));
    }

    // floatBitsToInt and floatBitsToUint functions
    intReturnFunctions.add(new FunctionStruct("floatBitsToInt",
        new UnifiedTypeProxy(BasicType.INT),
        true, new UnifiedTypeProxy(BasicType.FLOAT)));
    ivec2ReturnFunctions.add(new FunctionStruct("floatBitsToInt",
        new UnifiedTypeProxy(BasicType.IVEC2),
        true, new UnifiedTypeProxy(BasicType.VEC2)));
    ivec3ReturnFunctions.add(new FunctionStruct("floatBitsToInt",
        new UnifiedTypeProxy(BasicType.IVEC3),
        true, new UnifiedTypeProxy(BasicType.VEC3)));
    ivec4ReturnFunctions.add(new FunctionStruct("floatBitsToInt",
        new UnifiedTypeProxy(BasicType.IVEC4),
        true, new UnifiedTypeProxy(BasicType.VEC4)));
    uintReturnFunctions.add(new FunctionStruct("floatBitsToUint",
        new UnifiedTypeProxy(BasicType.UINT),
        true, new UnifiedTypeProxy(BasicType.FLOAT)));
    uvec2ReturnFunctions.add(new FunctionStruct("floatBitsToUint",
        new UnifiedTypeProxy(BasicType.UVEC2),
        true, new UnifiedTypeProxy(BasicType.VEC2)));
    uvec3ReturnFunctions.add(new FunctionStruct("floatBitsToUint",
        new UnifiedTypeProxy(BasicType.UVEC3),
        true, new UnifiedTypeProxy(BasicType.VEC3)));
    uvec4ReturnFunctions.add(new FunctionStruct("floatBitsToUint",
        new UnifiedTypeProxy(BasicType.UVEC4),
        true, new UnifiedTypeProxy(BasicType.VEC4)));

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Geometric functions
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // dot functions
    for (BasicType type : BasicType.allGenTypes()) {
      floatReturnFunctions.add(new FunctionStruct("dot", new UnifiedTypeProxy(BasicType.FLOAT),
          true, new UnifiedTypeProxy(type), new UnifiedTypeProxy(type)));
    }

    // cross function
    vec3ReturnFunctions.add(new FunctionStruct("cross", new UnifiedTypeProxy(BasicType.VEC3),
        true, new UnifiedTypeProxy(BasicType.VEC3), new UnifiedTypeProxy(BasicType.VEC3)));

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Vector relational functions
    ////////////////////////////////////////////////////////////////////////////////////////////////

    for (String functionText : Arrays.asList("lessThan", "lessThanEqual", "greaterThan",
        "greaterThanEqual", "equal", "notEqual")) {
      // Comparison functions for integers
      bvec2ReturnFunctions.add(new FunctionStruct(functionText,
          new UnifiedTypeProxy(BasicType.BVEC2), true,
          new UnifiedTypeProxy(BasicType.IVEC2), new UnifiedTypeProxy(BasicType.IVEC2)));
      bvec3ReturnFunctions.add(new FunctionStruct(functionText,
          new UnifiedTypeProxy(BasicType.BVEC3), true,
          new UnifiedTypeProxy(BasicType.IVEC3), new UnifiedTypeProxy(BasicType.IVEC3)));
      bvec4ReturnFunctions.add(new FunctionStruct(functionText,
          new UnifiedTypeProxy(BasicType.BVEC4),true,
          new UnifiedTypeProxy(BasicType.IVEC4), new UnifiedTypeProxy(BasicType.IVEC4)));

      // Comparison functions for unsigned integers
      bvec2ReturnFunctions.add(new FunctionStruct(functionText,
          new UnifiedTypeProxy(BasicType.BVEC2),true,
          new UnifiedTypeProxy(BasicType.UVEC2), new UnifiedTypeProxy(BasicType.UVEC2)));
      bvec3ReturnFunctions.add(new FunctionStruct(functionText,
          new UnifiedTypeProxy(BasicType.BVEC3),true,
          new UnifiedTypeProxy(BasicType.UVEC3), new UnifiedTypeProxy(BasicType.UVEC3)));
      bvec4ReturnFunctions.add(new FunctionStruct(functionText,
          new UnifiedTypeProxy(BasicType.BVEC4), true,
          new UnifiedTypeProxy(BasicType.UVEC4), new UnifiedTypeProxy(BasicType.UVEC4)));

      // Comparison functions for floats
      bvec2ReturnFunctions.add(new FunctionStruct(functionText,
          new UnifiedTypeProxy(BasicType.BVEC2),true,
          new UnifiedTypeProxy(BasicType.VEC2), new UnifiedTypeProxy(BasicType.VEC2)));
      bvec3ReturnFunctions.add(new FunctionStruct(functionText,
          new UnifiedTypeProxy(BasicType.BVEC3),true,
          new UnifiedTypeProxy(BasicType.VEC3), new UnifiedTypeProxy(BasicType.VEC3)));
      bvec4ReturnFunctions.add(new FunctionStruct(functionText,
          new UnifiedTypeProxy(BasicType.BVEC4), true,
          new UnifiedTypeProxy(BasicType.VEC4), new UnifiedTypeProxy(BasicType.VEC4)));
    }

    // Comparison functions on booleans
    for (String functionText : Arrays.asList("equal", "notEqual")) {
      bvec2ReturnFunctions.add(new FunctionStruct(functionText,
          new UnifiedTypeProxy(BasicType.BVEC2),true,
          new UnifiedTypeProxy(BasicType.BVEC2), new UnifiedTypeProxy(BasicType.BVEC2)));
      bvec3ReturnFunctions.add(new FunctionStruct(functionText,
          new UnifiedTypeProxy(BasicType.BVEC3), true,
          new UnifiedTypeProxy(BasicType.BVEC3), new UnifiedTypeProxy(BasicType.BVEC3)));
      bvec4ReturnFunctions.add(new FunctionStruct(functionText,
          new UnifiedTypeProxy(BasicType.BVEC4), true,
          new UnifiedTypeProxy(BasicType.BVEC4), new UnifiedTypeProxy(BasicType.BVEC4)));
    }

    // Any functions for booleans
    boolReturnFunctions.add(new FunctionStruct("any", new UnifiedTypeProxy(BasicType.BOOL),
        true, new UnifiedTypeProxy(BasicType.BVEC2)));
    boolReturnFunctions.add(new FunctionStruct("any", new UnifiedTypeProxy(BasicType.BOOL),
        true, new UnifiedTypeProxy(BasicType.BVEC3)));
    boolReturnFunctions.add(new FunctionStruct("any", new UnifiedTypeProxy(BasicType.BOOL),
        true, new UnifiedTypeProxy(BasicType.BVEC4)));

    // All functions for booleans
    boolReturnFunctions.add(new FunctionStruct("all", new UnifiedTypeProxy(BasicType.BOOL),
        true, new UnifiedTypeProxy(BasicType.BVEC2)));
    boolReturnFunctions.add(new FunctionStruct("all", new UnifiedTypeProxy(BasicType.BOOL),
        true, new UnifiedTypeProxy(BasicType.BVEC3)));
    boolReturnFunctions.add(new FunctionStruct("all", new UnifiedTypeProxy(BasicType.BOOL),
        true, new UnifiedTypeProxy(BasicType.BVEC4)));

    //Not function
    bvec2ReturnFunctions.add(new FunctionStruct("not", new UnifiedTypeProxy(BasicType.BVEC2),
        true, new UnifiedTypeProxy(BasicType.BVEC2)));
    bvec3ReturnFunctions.add(new FunctionStruct("not", new UnifiedTypeProxy(BasicType.BVEC3),
        true, new UnifiedTypeProxy(BasicType.BVEC3)));
    bvec4ReturnFunctions.add(new FunctionStruct("not", new UnifiedTypeProxy(BasicType.BVEC4),
        true, new UnifiedTypeProxy(BasicType.BVEC4)));

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Integer functions
    ////////////////////////////////////////////////////////////////////////////////////////////////
    //TODo suport lowp and mediump values

    // UmulExtended
    voidReturnFunctions.add(new FunctionStruct("umulExtended",
        new UnifiedTypeProxy(VoidType.VOID),
        true, new UnifiedTypeProxy(BasicType.UINT), new UnifiedTypeProxy(BasicType.UINT),
        new UnifiedTypeProxy(new QualifiedType(BasicType.UINT,
            Collections.singletonList(TypeQualifier.OUT_PARAM))),
        new UnifiedTypeProxy(new QualifiedType(BasicType.UINT,
            Collections.singletonList(TypeQualifier.OUT_PARAM)))));
    voidReturnFunctions.add(new FunctionStruct("umulExtended",
        new UnifiedTypeProxy(VoidType.VOID),
        true, new UnifiedTypeProxy(BasicType.UVEC2), new UnifiedTypeProxy(BasicType.UVEC2),
        new UnifiedTypeProxy(new QualifiedType(BasicType.UVEC2,
            Collections.singletonList(TypeQualifier.OUT_PARAM))),
        new UnifiedTypeProxy(new QualifiedType(BasicType.UVEC2,
            Collections.singletonList(TypeQualifier.OUT_PARAM)))));
    voidReturnFunctions.add(new FunctionStruct("umulExtended",
        new UnifiedTypeProxy(VoidType.VOID),
        true, new UnifiedTypeProxy(BasicType.UVEC3), new UnifiedTypeProxy(BasicType.UVEC3),
        new UnifiedTypeProxy(new QualifiedType(BasicType.UVEC3,
            Collections.singletonList(TypeQualifier.OUT_PARAM))),
        new UnifiedTypeProxy(new QualifiedType(BasicType.UVEC3,
            Collections.singletonList(TypeQualifier.OUT_PARAM)))));
    voidReturnFunctions.add(new FunctionStruct("umulExtended",
        new UnifiedTypeProxy(VoidType.VOID),
        true, new UnifiedTypeProxy(BasicType.UVEC4), new UnifiedTypeProxy(BasicType.UVEC4),
        new UnifiedTypeProxy(new QualifiedType(BasicType.UVEC4,
            Collections.singletonList(TypeQualifier.OUT_PARAM))),
        new UnifiedTypeProxy(new QualifiedType(BasicType.UVEC4,
          Collections.singletonList(TypeQualifier.OUT_PARAM)))));

    // ImulExtended
    voidReturnFunctions.add(new FunctionStruct("imulExtended",
        new UnifiedTypeProxy(VoidType.VOID),
        true, new UnifiedTypeProxy(BasicType.INT), new UnifiedTypeProxy(BasicType.INT),
        new UnifiedTypeProxy(new QualifiedType(BasicType.INT,
            Collections.singletonList(TypeQualifier.OUT_PARAM))),
        new UnifiedTypeProxy(new QualifiedType(BasicType.INT,
            Collections.singletonList(TypeQualifier.OUT_PARAM)))));
    voidReturnFunctions.add(new FunctionStruct("imulExtended",
        new UnifiedTypeProxy(VoidType.VOID),
        true, new UnifiedTypeProxy(BasicType.IVEC2), new UnifiedTypeProxy(BasicType.IVEC2),
        new UnifiedTypeProxy(new QualifiedType(BasicType.IVEC2,
            Collections.singletonList(TypeQualifier.OUT_PARAM))),
        new UnifiedTypeProxy(new QualifiedType(BasicType.IVEC2,
            Collections.singletonList(TypeQualifier.OUT_PARAM)))));
    voidReturnFunctions.add(new FunctionStruct("imulExtended",
        new UnifiedTypeProxy(VoidType.VOID),
        true, new UnifiedTypeProxy(BasicType.IVEC3), new UnifiedTypeProxy(BasicType.IVEC3),
        new UnifiedTypeProxy(new QualifiedType(BasicType.IVEC3,
            Collections.singletonList(TypeQualifier.OUT_PARAM))),
        new UnifiedTypeProxy(new QualifiedType(BasicType.IVEC3,
            Collections.singletonList(TypeQualifier.OUT_PARAM)))));
    voidReturnFunctions.add(new FunctionStruct("imulExtended",
        new UnifiedTypeProxy(VoidType.VOID),
        true, new UnifiedTypeProxy(BasicType.IVEC4), new UnifiedTypeProxy(BasicType.IVEC4),
        new UnifiedTypeProxy(new QualifiedType(BasicType.IVEC4,
            Collections.singletonList(TypeQualifier.OUT_PARAM))),
        new UnifiedTypeProxy(new QualifiedType(BasicType.IVEC4,
            Collections.singletonList(TypeQualifier.OUT_PARAM)))));

    // Bitfield Extract functions for integers
    intReturnFunctions.add(new FunctionStruct("bitfieldExtract",
        new UnifiedTypeProxy(BasicType.INT),
        true, new UnifiedTypeProxy(BasicType.INT), new UnifiedTypeProxy(BasicType.INT),
        new UnifiedTypeProxy(BasicType.INT)));
    ivec2ReturnFunctions.add(new FunctionStruct("bitfieldExtract",
        new UnifiedTypeProxy(BasicType.IVEC2),
        true, new UnifiedTypeProxy(BasicType.IVEC2), new UnifiedTypeProxy(BasicType.INT),
        new UnifiedTypeProxy(BasicType.INT)));
    ivec3ReturnFunctions.add(new FunctionStruct("bitfieldExtract",
        new UnifiedTypeProxy(BasicType.IVEC3),
        true, new UnifiedTypeProxy(BasicType.IVEC3), new UnifiedTypeProxy(BasicType.INT),
        new UnifiedTypeProxy(BasicType.INT)));
    ivec4ReturnFunctions.add(new FunctionStruct("bitfieldExtract",
        new UnifiedTypeProxy(BasicType.IVEC4),
        true, new UnifiedTypeProxy(BasicType.IVEC4), new UnifiedTypeProxy(BasicType.INT),
        new UnifiedTypeProxy(BasicType.INT)));

    // Bitfield Extract functions for unsigned integers
    uintReturnFunctions.add(new FunctionStruct("bitfieldExtract",
        new UnifiedTypeProxy(BasicType.UINT),
        true, new UnifiedTypeProxy(BasicType.UINT), new UnifiedTypeProxy(BasicType.INT),
        new UnifiedTypeProxy(BasicType.INT)));
    uvec2ReturnFunctions.add(new FunctionStruct("bitfieldExtract",
        new UnifiedTypeProxy(BasicType.UVEC2),
        true, new UnifiedTypeProxy(BasicType.UVEC2), new UnifiedTypeProxy(BasicType.INT),
        new UnifiedTypeProxy(BasicType.INT)));
    uvec3ReturnFunctions.add(new FunctionStruct("bitfieldExtract",
        new UnifiedTypeProxy(BasicType.UVEC3),
        true, new UnifiedTypeProxy(BasicType.UVEC3), new UnifiedTypeProxy(BasicType.INT),
        new UnifiedTypeProxy(BasicType.INT)));
    uvec4ReturnFunctions.add(new FunctionStruct("bitfieldExtract",
        new UnifiedTypeProxy(BasicType.UVEC4),
        true, new UnifiedTypeProxy(BasicType.UVEC4), new UnifiedTypeProxy(BasicType.INT),
        new UnifiedTypeProxy(BasicType.INT)));

    // Bitfield Insert functions for integers
    intReturnFunctions.add(new FunctionStruct("bitfieldInsert", new UnifiedTypeProxy(BasicType.INT),
        true, new UnifiedTypeProxy(BasicType.INT), new UnifiedTypeProxy(BasicType.INT),
        new UnifiedTypeProxy(BasicType.INT), new UnifiedTypeProxy(BasicType.INT)));
    ivec2ReturnFunctions.add(new FunctionStruct("bitfieldInsert",
        new UnifiedTypeProxy(BasicType.IVEC2),
        true, new UnifiedTypeProxy(BasicType.IVEC2), new UnifiedTypeProxy(BasicType.IVEC2),
        new UnifiedTypeProxy(BasicType.INT), new UnifiedTypeProxy(BasicType.INT)));
    ivec3ReturnFunctions.add(new FunctionStruct("bitfieldInsert",
        new UnifiedTypeProxy(BasicType.IVEC3),
        true, new UnifiedTypeProxy(BasicType.IVEC3), new UnifiedTypeProxy(BasicType.IVEC3),
        new UnifiedTypeProxy(BasicType.INT), new UnifiedTypeProxy(BasicType.INT)));
    ivec4ReturnFunctions.add(new FunctionStruct("bitfieldInsert",
        new UnifiedTypeProxy(BasicType.IVEC4),
        true, new UnifiedTypeProxy(BasicType.IVEC4), new UnifiedTypeProxy(BasicType.IVEC4),
        new UnifiedTypeProxy(BasicType.INT), new UnifiedTypeProxy(BasicType.INT)));

    // Bitfield Insert functions for unsigned integers
    uintReturnFunctions.add(new FunctionStruct("bitfieldInsert",
        new UnifiedTypeProxy(BasicType.UINT),
        true, new UnifiedTypeProxy(BasicType.UINT), new UnifiedTypeProxy(BasicType.UINT),
        new UnifiedTypeProxy(BasicType.INT), new UnifiedTypeProxy(BasicType.INT)));
    uvec2ReturnFunctions.add(new FunctionStruct("bitfieldInsert",
        new UnifiedTypeProxy(BasicType.UVEC2),
        true, new UnifiedTypeProxy(BasicType.UVEC2), new UnifiedTypeProxy(BasicType.UVEC2),
        new UnifiedTypeProxy(BasicType.INT), new UnifiedTypeProxy(BasicType.INT)));
    uvec3ReturnFunctions.add(new FunctionStruct("bitfieldInsert",
        new UnifiedTypeProxy(BasicType.UVEC3),
        true, new UnifiedTypeProxy(BasicType.UVEC3), new UnifiedTypeProxy(BasicType.UVEC3),
        new UnifiedTypeProxy(BasicType.INT), new UnifiedTypeProxy(BasicType.INT)));
    uvec4ReturnFunctions.add(new FunctionStruct("bitfieldInsert",
        new UnifiedTypeProxy(BasicType.UVEC4),
        true, new UnifiedTypeProxy(BasicType.UVEC4), new UnifiedTypeProxy(BasicType.UVEC4),
        new UnifiedTypeProxy(BasicType.INT), new UnifiedTypeProxy(BasicType.INT)));


    // Bitfield reverse for integers
    intReturnFunctions.add(new FunctionStruct("bitfieldReverse",
        new UnifiedTypeProxy(BasicType.INT),
        true, new UnifiedTypeProxy(BasicType.INT)));
    ivec2ReturnFunctions.add(new FunctionStruct("bitfieldReverse",
        new UnifiedTypeProxy(BasicType.IVEC2),
        true, new UnifiedTypeProxy(BasicType.IVEC2)));
    ivec3ReturnFunctions.add(new FunctionStruct("bitfieldReverse",
        new UnifiedTypeProxy(BasicType.IVEC3),
        true, new UnifiedTypeProxy(BasicType.IVEC3)));
    ivec4ReturnFunctions.add(new FunctionStruct("bitfieldReverse",
        new UnifiedTypeProxy(BasicType.IVEC4),
        true, new UnifiedTypeProxy(BasicType.IVEC4)));

    // Bitfield reverse for unsigned integers
    uintReturnFunctions.add(new FunctionStruct("bitfieldReverse",
        new UnifiedTypeProxy(BasicType.UINT),
        true, new UnifiedTypeProxy(BasicType.UINT)));
    uvec2ReturnFunctions.add(new FunctionStruct("bitfieldReverse",
        new UnifiedTypeProxy(BasicType.UVEC2),
        true, new UnifiedTypeProxy(BasicType.UVEC2)));
    uvec3ReturnFunctions.add(new FunctionStruct("bitfieldReverse",
        new UnifiedTypeProxy(BasicType.UVEC3),
        true, new UnifiedTypeProxy(BasicType.UVEC3)));
    uvec4ReturnFunctions.add(new FunctionStruct("bitfieldReverse",
        new UnifiedTypeProxy(BasicType.UVEC4),
        true, new UnifiedTypeProxy(BasicType.UVEC4)));

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Shader invocation Control and Shader memory control
    ////////////////////////////////////////////////////////////////////////////////////////////////

    if (shaderKind == ShaderKind.COMPUTE) {
      voidReturnFunctions.add(new FunctionStruct("barrier", new UnifiedTypeProxy(VoidType.VOID),
          true));
    }
    voidReturnFunctions.add(new FunctionStruct("memoryBarrier",
        new UnifiedTypeProxy(VoidType.VOID), true));
    //voidReturnFunctions.add(new FunctionStruct("memoryBarrierAtomicCounter",
    //    new UnifiedTypeProxy(VoidType.VOID)));
    voidReturnFunctions.add(new FunctionStruct("memoryBarrierBuffer",
        new UnifiedTypeProxy(VoidType.VOID), true));
    voidReturnFunctions.add(new FunctionStruct("memoryBarrierShared",
        new UnifiedTypeProxy(VoidType.VOID), true));
    voidReturnFunctions.add(new FunctionStruct("memoryBarrierImage",
        new UnifiedTypeProxy(VoidType.VOID), true));
    voidReturnFunctions.add(new FunctionStruct("groupMemoryBarrier",
        new UnifiedTypeProxy(VoidType.VOID), true));

    // Builds the registry
    stdFunctions.put(new UnifiedTypeProxy(BasicType.INT), intReturnFunctions);
    stdFunctions.put(new UnifiedTypeProxy(BasicType.UINT), uintReturnFunctions);
    stdFunctions.put(new UnifiedTypeProxy(BasicType.BOOL), boolReturnFunctions);
    stdFunctions.put(new UnifiedTypeProxy(BasicType.UVEC2), uvec2ReturnFunctions);
    stdFunctions.put(new UnifiedTypeProxy(BasicType.UVEC3), uvec3ReturnFunctions);
    stdFunctions.put(new UnifiedTypeProxy(BasicType.UVEC4), uvec4ReturnFunctions);
    stdFunctions.put(new UnifiedTypeProxy(BasicType.IVEC2), ivec2ReturnFunctions);
    stdFunctions.put(new UnifiedTypeProxy(BasicType.IVEC3), ivec3ReturnFunctions);
    stdFunctions.put(new UnifiedTypeProxy(BasicType.IVEC4), ivec4ReturnFunctions);
    stdFunctions.put(new UnifiedTypeProxy(BasicType.BVEC2), bvec2ReturnFunctions);
    stdFunctions.put(new UnifiedTypeProxy(BasicType.BVEC3), bvec3ReturnFunctions);
    stdFunctions.put(new UnifiedTypeProxy(BasicType.BVEC4), bvec4ReturnFunctions);
    stdFunctions.put(new UnifiedTypeProxy(BasicType.FLOAT), floatReturnFunctions);
    stdFunctions.put(new UnifiedTypeProxy(BasicType.VEC2), vec2ReturnFunctions);
    stdFunctions.put(new UnifiedTypeProxy(BasicType.VEC3), vec3ReturnFunctions);
    stdFunctions.put(new UnifiedTypeProxy(BasicType.VEC4), vec4ReturnFunctions);
    stdFunctions.put(new UnifiedTypeProxy(VoidType.VOID), voidReturnFunctions);
  }


  public FunctionStruct getRandomStdFunctionStruct(UnifiedTypeProxy returnType) {
    if (stdFunctions.containsKey(returnType)) {
      List<FunctionStruct> availableFunctions = stdFunctions.get(returnType);
      return availableFunctions.get(randGen.nextInt(availableFunctions.size()));
    } else {
      throw new UnsupportedOperationException("No function exists for the given return type: "
          + returnType);
    }
  }

  public List<FunctionStruct> getUserDefinedFunctionsOfGivenType(UnifiedTypeProxy returnType) {
    return userDefinedFunctions.get(returnType).stream().filter(FunctionStruct::hasDefinition)
        .collect(Collectors.toList());
  }

  public boolean hasUserDefinedFunctionOfGivenType(UnifiedTypeProxy returnType) {
    return userDefinedFunctions.containsKey(returnType) && !getUserDefinedFunctionsOfGivenType(returnType).isEmpty();
  }

  public FunctionStruct getRandomFunctionStruct(UnifiedTypeProxy returnType) {
    if (hasUserDefinedFunctionOfGivenType(returnType) && randGen.nextBoolean()) {
      List<FunctionStruct> availableFunctions = getUserDefinedFunctionsOfGivenType(returnType);
      return availableFunctions.get(randGen.nextInt(availableFunctions.size()));
    } else {
      return getRandomStdFunctionStruct(returnType);
    }
  }
}
