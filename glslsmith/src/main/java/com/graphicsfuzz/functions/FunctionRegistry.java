package com.graphicsfuzz.functions;

import com.graphicsfuzz.common.ast.type.BasicType;
import com.graphicsfuzz.common.ast.type.VoidType;
import com.graphicsfuzz.common.util.IRandom;
import com.graphicsfuzz.common.util.ShaderKind;
import com.graphicsfuzz.scope.UnifiedTypeProxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//TODO add float based functions
public class FunctionRegistry {
  private final IRandom randGen;
  private final Map<UnifiedTypeProxy, List<FunctionStruct>> stdFunctions = new HashMap<>();
  private final Map<UnifiedTypeProxy, List<FunctionStruct>> userDefinedFunctions = new HashMap<>();
  private final ShaderKind shaderKind;

  public FunctionRegistry(IRandom randGen, ShaderKind shaderKind) {
    this.randGen = randGen;
    this.shaderKind = shaderKind;
    List<FunctionStruct> intReturnFunctions = new ArrayList<>();
    List<FunctionStruct> uintReturnFunctions = new ArrayList<>();
    List<FunctionStruct> boolReturnFunctions = new ArrayList<>();
    List<FunctionStruct> uvec2ReturnFunctions = new ArrayList<>();
    List<FunctionStruct> uvec3ReturnFunctions = new ArrayList<>();
    List<FunctionStruct> uvec4ReturnFunctions = new ArrayList<>();
    List<FunctionStruct> ivec2ReturnFunctions = new ArrayList<>();
    List<FunctionStruct> ivec3ReturnFunctions = new ArrayList<>();
    List<FunctionStruct> ivec4ReturnFunctions = new ArrayList<>();
    List<FunctionStruct> bvec2ReturnFunctions = new ArrayList<>();
    List<FunctionStruct> bvec3ReturnFunctions = new ArrayList<>();
    List<FunctionStruct> bvec4ReturnFunctions = new ArrayList<>();
    List<FunctionStruct> voidReturnFunctions = new ArrayList<>();

    // Build the std registry

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Explicit conversions
    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Conversiosn to element types
    List<BasicType> allTypesExceptFloatBased = BasicType.allIntegerTypes();
    allTypesExceptFloatBased.addAll(BasicType.allBoolTypes());
    for (BasicType type : allTypesExceptFloatBased) {
      intReturnFunctions.add(new FunctionStruct("int", new UnifiedTypeProxy(BasicType.INT),
          new UnifiedTypeProxy(type)));
      uintReturnFunctions.add(new FunctionStruct("uint", new UnifiedTypeProxy(BasicType.UINT),
          new UnifiedTypeProxy(type)));
      boolReturnFunctions.add(new FunctionStruct("bool", new UnifiedTypeProxy(BasicType.BOOL),
          new UnifiedTypeProxy(type)));
    }

    // conversions to 2 elements vectors from base elements
    List<BasicType> allScalarExceptFloat = BasicType.allScalarTypes();
    allScalarExceptFloat.remove(BasicType.FLOAT);
    for (BasicType elementOne : allScalarExceptFloat) {
      for (BasicType elementTwo : allTypesExceptFloatBased) {
        ivec2ReturnFunctions.add(new FunctionStruct("ivec2", new UnifiedTypeProxy(BasicType.IVEC2),
            new UnifiedTypeProxy(elementOne), new UnifiedTypeProxy(elementTwo)));
        uvec2ReturnFunctions.add(new FunctionStruct("uvec2",
            new UnifiedTypeProxy(BasicType.UVEC2), new UnifiedTypeProxy(elementOne),
            new UnifiedTypeProxy(elementTwo)));
        bvec2ReturnFunctions.add(new FunctionStruct("bvec2",
            new UnifiedTypeProxy(BasicType.BVEC2), new UnifiedTypeProxy(elementOne),
            new UnifiedTypeProxy(elementTwo)));
      }
    }

    // Conversions to 2 elements vectors from all other types (vectors are truncated)
    List<BasicType> allVectorTypesExceptFloatBased = BasicType.allVectorTypes();
    allVectorTypesExceptFloatBased.removeAll(Arrays.asList(BasicType.VEC2,
        BasicType.VEC3, BasicType.VEC4));

    for (BasicType parameter : allVectorTypesExceptFloatBased) {
      ivec2ReturnFunctions.add(new FunctionStruct("ivec2", new UnifiedTypeProxy(BasicType.IVEC2),
          new UnifiedTypeProxy(parameter)));
      uvec2ReturnFunctions.add(new FunctionStruct("uvec2", new UnifiedTypeProxy(BasicType.UVEC2),
          new UnifiedTypeProxy(parameter)));
      bvec2ReturnFunctions.add(new FunctionStruct("bvec2", new UnifiedTypeProxy(BasicType.BVEC2),
          new UnifiedTypeProxy(parameter)));
    }

    // Conversions to 3 elements vectors from base elements
    for (BasicType parameterOne : allScalarExceptFloat) {
      for (BasicType parameterTwo : allScalarExceptFloat) {
        for (BasicType parameterThree : allTypesExceptFloatBased) {
          ivec3ReturnFunctions.add(new FunctionStruct("ivec3",
              new UnifiedTypeProxy(BasicType.IVEC3), new UnifiedTypeProxy(parameterOne),
              new UnifiedTypeProxy(parameterTwo), new UnifiedTypeProxy(parameterThree)));
          uvec3ReturnFunctions.add(new FunctionStruct("uvec3",
              new UnifiedTypeProxy(BasicType.UVEC3), new UnifiedTypeProxy(parameterOne),
              new UnifiedTypeProxy(parameterTwo), new UnifiedTypeProxy(parameterThree)));
          bvec3ReturnFunctions.add(new FunctionStruct("bvec3",
              new UnifiedTypeProxy(BasicType.BVEC3), new UnifiedTypeProxy(parameterOne),
              new UnifiedTypeProxy(parameterTwo), new UnifiedTypeProxy(parameterThree)));
        }
      }
    }

    List<BasicType> vec2ExceptFloats = Arrays.asList(BasicType.IVEC2, BasicType.UVEC2,
        BasicType.BVEC2);

    List<BasicType> vec3and4ExceptFloats = Arrays.asList(BasicType.IVEC3, BasicType.UVEC3,
        BasicType.BVEC3, BasicType.IVEC4, BasicType.UVEC4, BasicType.BVEC4);
    // Conversions to 3 elements vectors from a base element and a vector element
    for (BasicType parameterOne : allScalarExceptFloat) {
      for (BasicType parameterTwo : allVectorTypesExceptFloatBased) {
        ivec3ReturnFunctions.add(new FunctionStruct("ivec3",
            new UnifiedTypeProxy(BasicType.IVEC3), new UnifiedTypeProxy(parameterOne),
            new UnifiedTypeProxy(parameterTwo)));
        uvec3ReturnFunctions.add(new FunctionStruct("uvec3",
            new UnifiedTypeProxy(BasicType.UVEC3), new UnifiedTypeProxy(parameterOne),
            new UnifiedTypeProxy(parameterTwo)));
        bvec3ReturnFunctions.add(new FunctionStruct("bvec3",
            new UnifiedTypeProxy(BasicType.BVEC3), new UnifiedTypeProxy(parameterOne),
            new UnifiedTypeProxy(parameterTwo)));
      }
    }

    // Conversions to 3 elements vectors from a 2-vector on the left and a vector element on the
    // right
    for (BasicType parameterOne : vec2ExceptFloats) {
      for (BasicType parameterTwo : allTypesExceptFloatBased) {
        ivec3ReturnFunctions.add(new FunctionStruct("ivec3",
            new UnifiedTypeProxy(BasicType.IVEC3), new UnifiedTypeProxy(parameterOne),
            new UnifiedTypeProxy(parameterTwo)));
        uvec3ReturnFunctions.add(new FunctionStruct("uvec3",
            new UnifiedTypeProxy(BasicType.UVEC3), new UnifiedTypeProxy(parameterOne),
            new UnifiedTypeProxy(parameterTwo)));
        bvec3ReturnFunctions.add(new FunctionStruct("bvec3",
            new UnifiedTypeProxy(BasicType.BVEC3), new UnifiedTypeProxy(parameterOne),
            new UnifiedTypeProxy(parameterTwo)));
      }
    }

    // Conversions to 3 elements vector from a 3 or 4 element vector
    for (BasicType parameter : vec3and4ExceptFloats) {
      ivec3ReturnFunctions.add(new FunctionStruct("ivec3", new UnifiedTypeProxy(BasicType.IVEC3),
          new UnifiedTypeProxy(parameter)));
      uvec3ReturnFunctions.add(new FunctionStruct("uvec3", new UnifiedTypeProxy(BasicType.UVEC3),
          new UnifiedTypeProxy(parameter)));
      bvec3ReturnFunctions.add(new FunctionStruct("bvec3", new UnifiedTypeProxy(BasicType.BVEC3),
          new UnifiedTypeProxy(parameter)));
    }

    // Conversions to 4 elements vectors from base elements
    for (BasicType parameterOne : allScalarExceptFloat) {
      for (BasicType parameterTwo : allScalarExceptFloat) {
        for (BasicType parameterThree : allScalarExceptFloat) {
          for (BasicType parameterFour : allTypesExceptFloatBased) {
            ivec4ReturnFunctions.add(new FunctionStruct("ivec4",
                new UnifiedTypeProxy(BasicType.IVEC4), new UnifiedTypeProxy(parameterOne),
                new UnifiedTypeProxy(parameterTwo), new UnifiedTypeProxy(parameterThree),
                new UnifiedTypeProxy(parameterFour)));
            uvec4ReturnFunctions.add(new FunctionStruct("uvec4",
                new UnifiedTypeProxy(BasicType.UVEC4), new UnifiedTypeProxy(parameterOne),
                new UnifiedTypeProxy(parameterTwo), new UnifiedTypeProxy(parameterThree),
                new UnifiedTypeProxy(parameterFour)));
            bvec4ReturnFunctions.add(new FunctionStruct("bvec4",
                new UnifiedTypeProxy(BasicType.BVEC4), new UnifiedTypeProxy(parameterOne),
                new UnifiedTypeProxy(parameterTwo), new UnifiedTypeProxy(parameterThree),
                new UnifiedTypeProxy(parameterFour)));
          }
        }
      }
    }

    // Conversions to 4 elements from 2 base elements + a vector element
    for (BasicType parameterOne : allScalarExceptFloat) {
      for (BasicType parameterTwo : allScalarExceptFloat) {
        for (BasicType parameterThree : allVectorTypesExceptFloatBased) {
          ivec4ReturnFunctions.add(new FunctionStruct("ivec4",
              new UnifiedTypeProxy(BasicType.IVEC4), new UnifiedTypeProxy(parameterOne),
              new UnifiedTypeProxy(parameterTwo), new UnifiedTypeProxy(parameterThree)));
          uvec4ReturnFunctions.add(new FunctionStruct("uvec4",
              new UnifiedTypeProxy(BasicType.UVEC4), new UnifiedTypeProxy(parameterOne),
              new UnifiedTypeProxy(parameterTwo), new UnifiedTypeProxy(parameterThree)));
          bvec4ReturnFunctions.add(new FunctionStruct("bvec4",
              new UnifiedTypeProxy(BasicType.BVEC4), new UnifiedTypeProxy(parameterOne),
              new UnifiedTypeProxy(parameterTwo), new UnifiedTypeProxy(parameterThree)));
        }
      }
    }

    // Conversion to a 4 elements vector from a 2-element vector + base element + something
    for (BasicType parameterOne : allScalarExceptFloat) {
      for (BasicType parameterTwo : vec2ExceptFloats) {
        for (BasicType parameterThree : allTypesExceptFloatBased) {
          // Scalar + 2-vec + something
          ivec4ReturnFunctions.add(new FunctionStruct("ivec4",
              new UnifiedTypeProxy(BasicType.IVEC4), new UnifiedTypeProxy(parameterOne),
              new UnifiedTypeProxy(parameterTwo), new UnifiedTypeProxy(parameterThree)));
          uvec4ReturnFunctions.add(new FunctionStruct("uvec4",
              new UnifiedTypeProxy(BasicType.UVEC4), new UnifiedTypeProxy(parameterOne),
              new UnifiedTypeProxy(parameterTwo), new UnifiedTypeProxy(parameterThree)));
          bvec4ReturnFunctions.add(new FunctionStruct("bvec4",
              new UnifiedTypeProxy(BasicType.BVEC4), new UnifiedTypeProxy(parameterOne),
              new UnifiedTypeProxy(parameterTwo), new UnifiedTypeProxy(parameterThree)));
          // 2-vec + Scalar + something
          ivec4ReturnFunctions.add(new FunctionStruct("ivec4",
              new UnifiedTypeProxy(BasicType.IVEC4), new UnifiedTypeProxy(parameterTwo),
              new UnifiedTypeProxy(parameterOne), new UnifiedTypeProxy(parameterThree)));
          uvec4ReturnFunctions.add(new FunctionStruct("uvec4",
              new UnifiedTypeProxy(BasicType.UVEC4), new UnifiedTypeProxy(parameterTwo),
              new UnifiedTypeProxy(parameterOne), new UnifiedTypeProxy(parameterThree)));
          bvec4ReturnFunctions.add(new FunctionStruct("bvec4",
              new UnifiedTypeProxy(BasicType.BVEC4), new UnifiedTypeProxy(parameterTwo),
              new UnifiedTypeProxy(parameterOne), new UnifiedTypeProxy(parameterThree)));
        }
      }
    }

    // Conversion to a 4 elements vector from a 2-element vector + vector
    for (BasicType parameterOne : vec2ExceptFloats) {
      for (BasicType parameterTwo : allVectorTypesExceptFloatBased) {
        ivec4ReturnFunctions.add(new FunctionStruct("ivec4",
            new UnifiedTypeProxy(BasicType.IVEC4), new UnifiedTypeProxy(parameterOne),
            new UnifiedTypeProxy(parameterTwo)));
        uvec4ReturnFunctions.add(new FunctionStruct("uvec4",
            new UnifiedTypeProxy(BasicType.UVEC4), new UnifiedTypeProxy(parameterOne),
            new UnifiedTypeProxy(parameterTwo)));
        bvec4ReturnFunctions.add(new FunctionStruct("bvec4",
            new UnifiedTypeProxy(BasicType.BVEC4), new UnifiedTypeProxy(parameterOne),
            new UnifiedTypeProxy(parameterTwo)));
      }
    }
    List<BasicType> vec3ExceptFloats = Arrays.asList(BasicType.IVEC3, BasicType.UVEC3,
        BasicType.BVEC3);

    // Conversion to a 4 elements vector from a 3 elements vector + something
    for (BasicType parameterOne : vec3ExceptFloats) {
      for (BasicType parameterTwo : allTypesExceptFloatBased) {
        ivec4ReturnFunctions.add(new FunctionStruct("ivec4",
            new UnifiedTypeProxy(BasicType.IVEC4), new UnifiedTypeProxy(parameterOne),
            new UnifiedTypeProxy(parameterTwo)));
        uvec4ReturnFunctions.add(new FunctionStruct("uvec4",
            new UnifiedTypeProxy(BasicType.UVEC4), new UnifiedTypeProxy(parameterOne),
            new UnifiedTypeProxy(parameterTwo)));
        bvec4ReturnFunctions.add(new FunctionStruct("bvec4",
            new UnifiedTypeProxy(BasicType.BVEC4), new UnifiedTypeProxy(parameterOne),
            new UnifiedTypeProxy(parameterTwo)));
      }
    }

    // Conversion to a 4 elements vector from a base element + 3/4 elements vector
    for (BasicType parameterOne : allScalarExceptFloat) {
      for (BasicType parameterTwo : vec3and4ExceptFloats) {
        ivec4ReturnFunctions.add(new FunctionStruct("ivec4",
            new UnifiedTypeProxy(BasicType.IVEC4), new UnifiedTypeProxy(parameterOne),
            new UnifiedTypeProxy(parameterTwo)));
        uvec4ReturnFunctions.add(new FunctionStruct("uvec4",
            new UnifiedTypeProxy(BasicType.UVEC4), new UnifiedTypeProxy(parameterOne),
            new UnifiedTypeProxy(parameterTwo)));
        bvec4ReturnFunctions.add(new FunctionStruct("bvec4",
            new UnifiedTypeProxy(BasicType.BVEC4), new UnifiedTypeProxy(parameterOne),
            new UnifiedTypeProxy(parameterTwo)));
      }
    }

    List<BasicType> vec4ExceptFloats = Arrays.asList(BasicType.IVEC4, BasicType.UVEC4,
        BasicType.BVEC4);
    // Conversion to a 4 elements vector from another 4 elements vector
    for (BasicType parameter : vec4ExceptFloats) {
      ivec4ReturnFunctions.add(new FunctionStruct("ivec4", new UnifiedTypeProxy(BasicType.IVEC4),
          new UnifiedTypeProxy(parameter)));
      uvec4ReturnFunctions.add(new FunctionStruct("uvec4", new UnifiedTypeProxy(BasicType.UVEC4),
          new UnifiedTypeProxy(parameter)));
      bvec4ReturnFunctions.add(new FunctionStruct("bvec4", new UnifiedTypeProxy(BasicType.BVEC4),
          new UnifiedTypeProxy(parameter)));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Common Functions
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // abs functions for integers
    intReturnFunctions.add(new FunctionStruct("abs", new UnifiedTypeProxy(BasicType.INT),
        new UnifiedTypeProxy(BasicType.INT)));
    ivec2ReturnFunctions.add(new FunctionStruct("abs", new UnifiedTypeProxy(BasicType.IVEC2),
        new UnifiedTypeProxy(BasicType.IVEC2)));
    ivec3ReturnFunctions.add(new FunctionStruct("abs", new UnifiedTypeProxy(BasicType.IVEC3),
        new UnifiedTypeProxy(BasicType.IVEC3)));
    ivec4ReturnFunctions.add(new FunctionStruct("abs", new UnifiedTypeProxy(BasicType.IVEC4),
        new UnifiedTypeProxy(BasicType.IVEC4)));

    // sign functions for integers
    intReturnFunctions.add(new FunctionStruct("sign", new UnifiedTypeProxy(BasicType.INT),
        new UnifiedTypeProxy(BasicType.INT)));
    ivec2ReturnFunctions.add(new FunctionStruct("sign", new UnifiedTypeProxy(BasicType.IVEC2),
        new UnifiedTypeProxy(BasicType.IVEC2)));
    ivec3ReturnFunctions.add(new FunctionStruct("sign", new UnifiedTypeProxy(BasicType.IVEC3),
        new UnifiedTypeProxy(BasicType.IVEC3)));
    ivec4ReturnFunctions.add(new FunctionStruct("sign", new UnifiedTypeProxy(BasicType.IVEC4),
        new UnifiedTypeProxy(BasicType.IVEC4)));

    // Min functions for integers
    intReturnFunctions.add(new FunctionStruct("min", new UnifiedTypeProxy(BasicType.INT),
        new UnifiedTypeProxy(BasicType.INT), new UnifiedTypeProxy(BasicType.INT)));
    ivec2ReturnFunctions.add(new FunctionStruct("min", new UnifiedTypeProxy(BasicType.IVEC2),
        new UnifiedTypeProxy(BasicType.IVEC2),
        new UnifiedTypeProxy(BasicType.IVEC2)));
    ivec2ReturnFunctions.add(new FunctionStruct("min", new UnifiedTypeProxy(BasicType.IVEC2),
        new UnifiedTypeProxy(BasicType.IVEC2),
        new UnifiedTypeProxy(BasicType.INT)));
    ivec3ReturnFunctions.add(new FunctionStruct("min", new UnifiedTypeProxy(BasicType.IVEC3),
        new UnifiedTypeProxy(BasicType.IVEC3),
        new UnifiedTypeProxy(BasicType.IVEC3)));
    ivec3ReturnFunctions.add(new FunctionStruct("min", new UnifiedTypeProxy(BasicType.IVEC3),
        new UnifiedTypeProxy(BasicType.IVEC3),
        new UnifiedTypeProxy(BasicType.INT)));
    ivec4ReturnFunctions.add(new FunctionStruct("min", new UnifiedTypeProxy(BasicType.IVEC4),
        new UnifiedTypeProxy(BasicType.IVEC4),
        new UnifiedTypeProxy(BasicType.IVEC4)));
    ivec4ReturnFunctions.add(new FunctionStruct("min", new UnifiedTypeProxy(BasicType.IVEC4),
        new UnifiedTypeProxy(BasicType.IVEC4),
        new UnifiedTypeProxy(BasicType.INT)));

    // Min functions for unsigned integers
    uintReturnFunctions.add(new FunctionStruct("min", new UnifiedTypeProxy(BasicType.UINT),
        new UnifiedTypeProxy(BasicType.UINT),
        new UnifiedTypeProxy(BasicType.UINT)));
    uvec2ReturnFunctions.add(new FunctionStruct("min", new UnifiedTypeProxy(BasicType.UVEC2),
        new UnifiedTypeProxy(BasicType.UVEC2),
        new UnifiedTypeProxy(BasicType.UVEC2)));
    uvec2ReturnFunctions.add(new FunctionStruct("min", new UnifiedTypeProxy(BasicType.UVEC2),
        new UnifiedTypeProxy(BasicType.UVEC2),
        new UnifiedTypeProxy(BasicType.UINT)));
    uvec3ReturnFunctions.add(new FunctionStruct("min", new UnifiedTypeProxy(BasicType.UVEC3),
        new UnifiedTypeProxy(BasicType.UVEC3),
        new UnifiedTypeProxy(BasicType.UVEC3)));
    uvec3ReturnFunctions.add(new FunctionStruct("min", new UnifiedTypeProxy(BasicType.UVEC3),
        new UnifiedTypeProxy(BasicType.UVEC3),
        new UnifiedTypeProxy(BasicType.UINT)));
    uvec4ReturnFunctions.add(new FunctionStruct("min", new UnifiedTypeProxy(BasicType.UVEC4),
        new UnifiedTypeProxy(BasicType.UVEC4),
        new UnifiedTypeProxy(BasicType.UVEC4)));
    uvec4ReturnFunctions.add(new FunctionStruct("min", new UnifiedTypeProxy(BasicType.UVEC4),
        new UnifiedTypeProxy(BasicType.UVEC4),
        new UnifiedTypeProxy(BasicType.UINT)));

    // Max functions for integers
    intReturnFunctions.add(new FunctionStruct("max", new UnifiedTypeProxy(BasicType.INT),
        new UnifiedTypeProxy(BasicType.INT), new UnifiedTypeProxy(BasicType.INT)));
    ivec2ReturnFunctions.add(new FunctionStruct("max", new UnifiedTypeProxy(BasicType.IVEC2),
        new UnifiedTypeProxy(BasicType.IVEC2),
        new UnifiedTypeProxy(BasicType.IVEC2)));
    ivec2ReturnFunctions.add(new FunctionStruct("max", new UnifiedTypeProxy(BasicType.IVEC2),
        new UnifiedTypeProxy(BasicType.IVEC2),
        new UnifiedTypeProxy(BasicType.INT)));
    ivec3ReturnFunctions.add(new FunctionStruct("max", new UnifiedTypeProxy(BasicType.IVEC3),
        new UnifiedTypeProxy(BasicType.IVEC3),
        new UnifiedTypeProxy(BasicType.IVEC3)));
    ivec3ReturnFunctions.add(new FunctionStruct("max", new UnifiedTypeProxy(BasicType.IVEC3),
        new UnifiedTypeProxy(BasicType.IVEC3),
        new UnifiedTypeProxy(BasicType.INT)));
    ivec4ReturnFunctions.add(new FunctionStruct("max", new UnifiedTypeProxy(BasicType.IVEC4),
        new UnifiedTypeProxy(BasicType.IVEC4),
        new UnifiedTypeProxy(BasicType.IVEC4)));
    ivec4ReturnFunctions.add(new FunctionStruct("max", new UnifiedTypeProxy(BasicType.IVEC4),
        new UnifiedTypeProxy(BasicType.IVEC4),
        new UnifiedTypeProxy(BasicType.INT)));

    // Max functions for unsigned integers
    uintReturnFunctions.add(new FunctionStruct("max", new UnifiedTypeProxy(BasicType.UINT),
        new UnifiedTypeProxy(BasicType.UINT),
        new UnifiedTypeProxy(BasicType.UINT)));
    uvec2ReturnFunctions.add(new FunctionStruct("max", new UnifiedTypeProxy(BasicType.UVEC2),
        new UnifiedTypeProxy(BasicType.UVEC2),
        new UnifiedTypeProxy(BasicType.UVEC2)));
    uvec2ReturnFunctions.add(new FunctionStruct("max", new UnifiedTypeProxy(BasicType.UVEC2),
        new UnifiedTypeProxy(BasicType.UVEC2),
        new UnifiedTypeProxy(BasicType.UINT)));
    uvec3ReturnFunctions.add(new FunctionStruct("max", new UnifiedTypeProxy(BasicType.UVEC3),
        new UnifiedTypeProxy(BasicType.UVEC3),
        new UnifiedTypeProxy(BasicType.UVEC3)));
    uvec3ReturnFunctions.add(new FunctionStruct("max", new UnifiedTypeProxy(BasicType.UVEC3),
        new UnifiedTypeProxy(BasicType.UVEC3),
        new UnifiedTypeProxy(BasicType.UINT)));
    uvec4ReturnFunctions.add(new FunctionStruct("max", new UnifiedTypeProxy(BasicType.UVEC4),
        new UnifiedTypeProxy(BasicType.UVEC4),
        new UnifiedTypeProxy(BasicType.UVEC4)));
    uvec4ReturnFunctions.add(new FunctionStruct("max", new UnifiedTypeProxy(BasicType.UVEC4),
        new UnifiedTypeProxy(BasicType.UVEC4),
        new UnifiedTypeProxy(BasicType.UINT)));

    // Clamp functions for signed integers
    intReturnFunctions.add(new FunctionStruct("clamp", new UnifiedTypeProxy(BasicType.INT),
        new UnifiedTypeProxy(BasicType.INT), new UnifiedTypeProxy(BasicType.INT),
        new UnifiedTypeProxy(BasicType.INT)));
    ivec2ReturnFunctions.add(new FunctionStruct("clamp", new UnifiedTypeProxy(BasicType.IVEC2),
        new UnifiedTypeProxy(BasicType.IVEC2), new UnifiedTypeProxy(BasicType.IVEC2),
        new UnifiedTypeProxy(BasicType.IVEC2)));
    ivec2ReturnFunctions.add(new FunctionStruct("clamp", new UnifiedTypeProxy(BasicType.IVEC2),
        new UnifiedTypeProxy(BasicType.IVEC2), new UnifiedTypeProxy(BasicType.INT),
        new UnifiedTypeProxy(BasicType.INT)));
    ivec3ReturnFunctions.add(new FunctionStruct("clamp", new UnifiedTypeProxy(BasicType.IVEC3),
        new UnifiedTypeProxy(BasicType.IVEC3), new UnifiedTypeProxy(BasicType.IVEC3),
        new UnifiedTypeProxy(BasicType.IVEC3)));
    ivec3ReturnFunctions.add(new FunctionStruct("clamp", new UnifiedTypeProxy(BasicType.IVEC3),
        new UnifiedTypeProxy(BasicType.IVEC3), new UnifiedTypeProxy(BasicType.INT),
        new UnifiedTypeProxy(BasicType.INT)));
    ivec4ReturnFunctions.add(new FunctionStruct("clamp", new UnifiedTypeProxy(BasicType.IVEC4),
        new UnifiedTypeProxy(BasicType.IVEC4), new UnifiedTypeProxy(BasicType.IVEC4),
        new UnifiedTypeProxy(BasicType.IVEC4)));
    ivec4ReturnFunctions.add(new FunctionStruct("clamp", new UnifiedTypeProxy(BasicType.IVEC4),
        new UnifiedTypeProxy(BasicType.IVEC4), new UnifiedTypeProxy(BasicType.INT),
        new UnifiedTypeProxy(BasicType.INT)));

    // Clamp functions for unsigned integers
    uintReturnFunctions.add(new FunctionStruct("clamp", new UnifiedTypeProxy(BasicType.UINT),
        new UnifiedTypeProxy(BasicType.UINT), new UnifiedTypeProxy(BasicType.UINT),
        new UnifiedTypeProxy(BasicType.UINT)));
    uvec2ReturnFunctions.add(new FunctionStruct("clamp", new UnifiedTypeProxy(BasicType.UVEC2),
        new UnifiedTypeProxy(BasicType.UVEC2), new UnifiedTypeProxy(BasicType.UVEC2),
        new UnifiedTypeProxy(BasicType.UVEC2)));
    uvec2ReturnFunctions.add(new FunctionStruct("clamp", new UnifiedTypeProxy(BasicType.UVEC2),
        new UnifiedTypeProxy(BasicType.UVEC2), new UnifiedTypeProxy(BasicType.UINT),
        new UnifiedTypeProxy(BasicType.UINT)));
    uvec3ReturnFunctions.add(new FunctionStruct("clamp", new UnifiedTypeProxy(BasicType.UVEC3),
        new UnifiedTypeProxy(BasicType.UVEC3), new UnifiedTypeProxy(BasicType.UVEC3),
        new UnifiedTypeProxy(BasicType.UVEC3)));
    uvec3ReturnFunctions.add(new FunctionStruct("clamp", new UnifiedTypeProxy(BasicType.UVEC3),
        new UnifiedTypeProxy(BasicType.UVEC3), new UnifiedTypeProxy(BasicType.UINT),
        new UnifiedTypeProxy(BasicType.UINT)));
    uvec4ReturnFunctions.add(new FunctionStruct("clamp", new UnifiedTypeProxy(BasicType.UVEC4),
        new UnifiedTypeProxy(BasicType.UVEC4), new UnifiedTypeProxy(BasicType.UVEC4),
        new UnifiedTypeProxy(BasicType.UVEC4)));
    uvec4ReturnFunctions.add(new FunctionStruct("clamp", new UnifiedTypeProxy(BasicType.UVEC4),
        new UnifiedTypeProxy(BasicType.UVEC4), new UnifiedTypeProxy(BasicType.UINT),
        new UnifiedTypeProxy(BasicType.UINT)));

    // Mix functions for integers
    intReturnFunctions.add(new FunctionStruct("mix", new UnifiedTypeProxy(BasicType.INT),
        new UnifiedTypeProxy(BasicType.INT), new UnifiedTypeProxy(BasicType.INT),
        new UnifiedTypeProxy(BasicType.BOOL)));
    ivec2ReturnFunctions.add(new FunctionStruct("mix", new UnifiedTypeProxy(BasicType.IVEC2),
        new UnifiedTypeProxy(BasicType.IVEC2), new UnifiedTypeProxy(BasicType.IVEC2),
        new UnifiedTypeProxy(BasicType.BVEC2)));
    ivec3ReturnFunctions.add(new FunctionStruct("mix", new UnifiedTypeProxy(BasicType.IVEC3),
        new UnifiedTypeProxy(BasicType.IVEC3), new UnifiedTypeProxy(BasicType.IVEC3),
        new UnifiedTypeProxy(BasicType.BVEC3)));
    ivec4ReturnFunctions.add(new FunctionStruct("mix", new UnifiedTypeProxy(BasicType.IVEC4),
        new UnifiedTypeProxy(BasicType.IVEC4), new UnifiedTypeProxy(BasicType.IVEC4),
        new UnifiedTypeProxy(BasicType.BVEC4)));

    // Mix functions for unsigned integers
    uintReturnFunctions.add(new FunctionStruct("mix", new UnifiedTypeProxy(BasicType.UINT),
        new UnifiedTypeProxy(BasicType.UINT), new UnifiedTypeProxy(BasicType.UINT),
        new UnifiedTypeProxy(BasicType.BOOL)));
    uvec2ReturnFunctions.add(new FunctionStruct("mix", new UnifiedTypeProxy(BasicType.UVEC2),
        new UnifiedTypeProxy(BasicType.UVEC2), new UnifiedTypeProxy(BasicType.UVEC2),
        new UnifiedTypeProxy(BasicType.BVEC2)));
    uvec3ReturnFunctions.add(new FunctionStruct("mix", new UnifiedTypeProxy(BasicType.UVEC3),
        new UnifiedTypeProxy(BasicType.UVEC3), new UnifiedTypeProxy(BasicType.UVEC3),
        new UnifiedTypeProxy(BasicType.BVEC3)));
    uvec4ReturnFunctions.add(new FunctionStruct("mix", new UnifiedTypeProxy(BasicType.UVEC4),
        new UnifiedTypeProxy(BasicType.UVEC4), new UnifiedTypeProxy(BasicType.UVEC4),
        new UnifiedTypeProxy(BasicType.BVEC4)));

    // Mix functions for booleans
    boolReturnFunctions.add(new FunctionStruct("mix", new UnifiedTypeProxy(BasicType.BOOL),
        new UnifiedTypeProxy(BasicType.BOOL), new UnifiedTypeProxy(BasicType.BOOL),
        new UnifiedTypeProxy(BasicType.BOOL)));
    bvec2ReturnFunctions.add(new FunctionStruct("mix", new UnifiedTypeProxy(BasicType.BVEC2),
        new UnifiedTypeProxy(BasicType.BVEC2), new UnifiedTypeProxy(BasicType.BVEC2),
        new UnifiedTypeProxy(BasicType.BVEC2)));
    bvec3ReturnFunctions.add(new FunctionStruct("mix", new UnifiedTypeProxy(BasicType.BVEC3),
        new UnifiedTypeProxy(BasicType.BVEC3), new UnifiedTypeProxy(BasicType.BVEC3),
        new UnifiedTypeProxy(BasicType.BVEC3)));
    bvec4ReturnFunctions.add(new FunctionStruct("mix", new UnifiedTypeProxy(BasicType.BVEC4),
        new UnifiedTypeProxy(BasicType.BVEC4), new UnifiedTypeProxy(BasicType.BVEC4),
        new UnifiedTypeProxy(BasicType.BVEC4)));

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Vector relational functions
    ////////////////////////////////////////////////////////////////////////////////////////////////

    for (String functionText : Arrays.asList("lessThan", "lessThanEqual", "greaterThan",
        "greaterThanEqual", "equal", "notEqual")) {
      // Comparison functions for integers
      bvec2ReturnFunctions.add(new FunctionStruct(functionText,
          new UnifiedTypeProxy(BasicType.BVEC2),
          new UnifiedTypeProxy(BasicType.IVEC2), new UnifiedTypeProxy(BasicType.IVEC2)));
      bvec3ReturnFunctions.add(new FunctionStruct(functionText,
          new UnifiedTypeProxy(BasicType.BVEC3),
          new UnifiedTypeProxy(BasicType.IVEC3), new UnifiedTypeProxy(BasicType.IVEC3)));
      bvec4ReturnFunctions.add(new FunctionStruct(functionText,
          new UnifiedTypeProxy(BasicType.BVEC4),
          new UnifiedTypeProxy(BasicType.IVEC4), new UnifiedTypeProxy(BasicType.IVEC4)));

      // Comparison functions for unsigned integers
      bvec2ReturnFunctions.add(new FunctionStruct(functionText,
          new UnifiedTypeProxy(BasicType.BVEC2),
          new UnifiedTypeProxy(BasicType.UVEC2), new UnifiedTypeProxy(BasicType.UVEC2)));
      bvec3ReturnFunctions.add(new FunctionStruct(functionText,
          new UnifiedTypeProxy(BasicType.BVEC3),
          new UnifiedTypeProxy(BasicType.UVEC3), new UnifiedTypeProxy(BasicType.UVEC3)));
      bvec4ReturnFunctions.add(new FunctionStruct(functionText,
          new UnifiedTypeProxy(BasicType.BVEC4),
          new UnifiedTypeProxy(BasicType.UVEC4), new UnifiedTypeProxy(BasicType.UVEC4)));
    }

    // Comparison functions on booleans
    for (String functionText : Arrays.asList("equal", "notEqual")) {
      bvec2ReturnFunctions.add(new FunctionStruct(functionText,
          new UnifiedTypeProxy(BasicType.BVEC2),
          new UnifiedTypeProxy(BasicType.BVEC2), new UnifiedTypeProxy(BasicType.BVEC2)));
      bvec3ReturnFunctions.add(new FunctionStruct(functionText,
          new UnifiedTypeProxy(BasicType.BVEC3),
          new UnifiedTypeProxy(BasicType.BVEC3), new UnifiedTypeProxy(BasicType.BVEC3)));
      bvec4ReturnFunctions.add(new FunctionStruct(functionText,
          new UnifiedTypeProxy(BasicType.BVEC4),
          new UnifiedTypeProxy(BasicType.BVEC4), new UnifiedTypeProxy(BasicType.BVEC4)));
    }

    // Any functions for booleans
    boolReturnFunctions.add(new FunctionStruct("any", new UnifiedTypeProxy(BasicType.BOOL),
        new UnifiedTypeProxy(BasicType.BVEC2)));
    boolReturnFunctions.add(new FunctionStruct("any", new UnifiedTypeProxy(BasicType.BOOL),
        new UnifiedTypeProxy(BasicType.BVEC3)));
    boolReturnFunctions.add(new FunctionStruct("any", new UnifiedTypeProxy(BasicType.BOOL),
        new UnifiedTypeProxy(BasicType.BVEC4)));

    // All functions for booleans
    boolReturnFunctions.add(new FunctionStruct("all", new UnifiedTypeProxy(BasicType.BOOL),
        new UnifiedTypeProxy(BasicType.BVEC2)));
    boolReturnFunctions.add(new FunctionStruct("all", new UnifiedTypeProxy(BasicType.BOOL),
        new UnifiedTypeProxy(BasicType.BVEC3)));
    boolReturnFunctions.add(new FunctionStruct("all", new UnifiedTypeProxy(BasicType.BOOL),
        new UnifiedTypeProxy(BasicType.BVEC4)));

    //Not function
    bvec2ReturnFunctions.add(new FunctionStruct("not", new UnifiedTypeProxy(BasicType.BVEC2),
        new UnifiedTypeProxy(BasicType.BVEC2)));
    bvec3ReturnFunctions.add(new FunctionStruct("not", new UnifiedTypeProxy(BasicType.BVEC3),
        new UnifiedTypeProxy(BasicType.BVEC3)));
    bvec4ReturnFunctions.add(new FunctionStruct("not", new UnifiedTypeProxy(BasicType.BVEC4),
        new UnifiedTypeProxy(BasicType.BVEC4)));

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Integer functions
    ////////////////////////////////////////////////////////////////////////////////////////////////
    //TODo suport lowp and mediump values

    // UmulExtended
    voidReturnFunctions.add(new FunctionStruct("umulExtended",
        new UnifiedTypeProxy(VoidType.VOID),
        new UnifiedTypeProxy(BasicType.UINT), new UnifiedTypeProxy(BasicType.UINT),
        new UnifiedTypeProxy(BasicType.UINT), new UnifiedTypeProxy(BasicType.UINT)));
    voidReturnFunctions.add(new FunctionStruct("umulExtended",
        new UnifiedTypeProxy(VoidType.VOID),
        new UnifiedTypeProxy(BasicType.UVEC2), new UnifiedTypeProxy(BasicType.UVEC2),
        new UnifiedTypeProxy(BasicType.UVEC2), new UnifiedTypeProxy(BasicType.UVEC2)));
    voidReturnFunctions.add(new FunctionStruct("umulExtended",
        new UnifiedTypeProxy(VoidType.VOID),
        new UnifiedTypeProxy(BasicType.UVEC3), new UnifiedTypeProxy(BasicType.UVEC3),
        new UnifiedTypeProxy(BasicType.UVEC3), new UnifiedTypeProxy(BasicType.UVEC3)));
    voidReturnFunctions.add(new FunctionStruct("umulExtended",
        new UnifiedTypeProxy(VoidType.VOID),
        new UnifiedTypeProxy(BasicType.UVEC4), new UnifiedTypeProxy(BasicType.UVEC4),
        new UnifiedTypeProxy(BasicType.UVEC4), new UnifiedTypeProxy(BasicType.UVEC4)));

    // ImulExtended
    voidReturnFunctions.add(new FunctionStruct("imulExtended",
        new UnifiedTypeProxy(VoidType.VOID),
        new UnifiedTypeProxy(BasicType.INT), new UnifiedTypeProxy(BasicType.INT),
        new UnifiedTypeProxy(BasicType.INT), new UnifiedTypeProxy(BasicType.INT)));
    voidReturnFunctions.add(new FunctionStruct("imulExtended",
        new UnifiedTypeProxy(VoidType.VOID),
        new UnifiedTypeProxy(BasicType.IVEC2), new UnifiedTypeProxy(BasicType.IVEC2),
        new UnifiedTypeProxy(BasicType.IVEC2), new UnifiedTypeProxy(BasicType.IVEC2)));
    voidReturnFunctions.add(new FunctionStruct("imulExtended",
        new UnifiedTypeProxy(VoidType.VOID),
        new UnifiedTypeProxy(BasicType.IVEC3), new UnifiedTypeProxy(BasicType.IVEC3),
        new UnifiedTypeProxy(BasicType.IVEC3), new UnifiedTypeProxy(BasicType.IVEC3)));
    voidReturnFunctions.add(new FunctionStruct("imulExtended",
        new UnifiedTypeProxy(VoidType.VOID),
        new UnifiedTypeProxy(BasicType.IVEC4), new UnifiedTypeProxy(BasicType.IVEC4),
        new UnifiedTypeProxy(BasicType.IVEC4), new UnifiedTypeProxy(BasicType.IVEC4)));

    // Bitfield Extract functions for integers
    intReturnFunctions.add(new FunctionStruct("bitfieldExtract",
        new UnifiedTypeProxy(BasicType.INT),
        new UnifiedTypeProxy(BasicType.INT), new UnifiedTypeProxy(BasicType.INT),
        new UnifiedTypeProxy(BasicType.INT)));
    ivec2ReturnFunctions.add(new FunctionStruct("bitfieldExtract",
        new UnifiedTypeProxy(BasicType.IVEC2),
        new UnifiedTypeProxy(BasicType.IVEC2), new UnifiedTypeProxy(BasicType.INT),
        new UnifiedTypeProxy(BasicType.INT)));
    ivec3ReturnFunctions.add(new FunctionStruct("bitfieldExtract",
        new UnifiedTypeProxy(BasicType.IVEC3),
        new UnifiedTypeProxy(BasicType.IVEC3), new UnifiedTypeProxy(BasicType.INT),
        new UnifiedTypeProxy(BasicType.INT)));
    ivec4ReturnFunctions.add(new FunctionStruct("bitfieldExtract",
        new UnifiedTypeProxy(BasicType.IVEC4),
        new UnifiedTypeProxy(BasicType.IVEC4), new UnifiedTypeProxy(BasicType.INT),
        new UnifiedTypeProxy(BasicType.INT)));

    // Bitfield Extract functions for unsigned integers
    uintReturnFunctions.add(new FunctionStruct("bitfieldExtract",
        new UnifiedTypeProxy(BasicType.UINT),
        new UnifiedTypeProxy(BasicType.UINT), new UnifiedTypeProxy(BasicType.INT),
        new UnifiedTypeProxy(BasicType.INT)));
    uvec2ReturnFunctions.add(new FunctionStruct("bitfieldExtract",
        new UnifiedTypeProxy(BasicType.UVEC2),
        new UnifiedTypeProxy(BasicType.UVEC2), new UnifiedTypeProxy(BasicType.INT),
        new UnifiedTypeProxy(BasicType.INT)));
    uvec3ReturnFunctions.add(new FunctionStruct("bitfieldExtract",
        new UnifiedTypeProxy(BasicType.UVEC3),
        new UnifiedTypeProxy(BasicType.UVEC3), new UnifiedTypeProxy(BasicType.INT),
        new UnifiedTypeProxy(BasicType.INT)));
    uvec4ReturnFunctions.add(new FunctionStruct("bitfieldExtract",
        new UnifiedTypeProxy(BasicType.UVEC4),
        new UnifiedTypeProxy(BasicType.UVEC4), new UnifiedTypeProxy(BasicType.INT),
        new UnifiedTypeProxy(BasicType.INT)));


    // Bitfield Insert functions for integers
    intReturnFunctions.add(new FunctionStruct("bitfieldInsert", new UnifiedTypeProxy(BasicType.INT),
        new UnifiedTypeProxy(BasicType.INT), new UnifiedTypeProxy(BasicType.INT),
        new UnifiedTypeProxy(BasicType.INT), new UnifiedTypeProxy(BasicType.INT)));
    ivec2ReturnFunctions.add(new FunctionStruct("bitfieldInsert",
        new UnifiedTypeProxy(BasicType.IVEC2),
        new UnifiedTypeProxy(BasicType.IVEC2), new UnifiedTypeProxy(BasicType.IVEC2),
        new UnifiedTypeProxy(BasicType.INT), new UnifiedTypeProxy(BasicType.INT)));
    ivec3ReturnFunctions.add(new FunctionStruct("bitfieldInsert",
        new UnifiedTypeProxy(BasicType.IVEC3),
        new UnifiedTypeProxy(BasicType.IVEC3), new UnifiedTypeProxy(BasicType.IVEC3),
        new UnifiedTypeProxy(BasicType.INT), new UnifiedTypeProxy(BasicType.INT)));
    ivec4ReturnFunctions.add(new FunctionStruct("bitfieldInsert",
        new UnifiedTypeProxy(BasicType.IVEC4),
        new UnifiedTypeProxy(BasicType.IVEC4), new UnifiedTypeProxy(BasicType.IVEC4),
        new UnifiedTypeProxy(BasicType.INT), new UnifiedTypeProxy(BasicType.INT)));

    // Bitfield Insert functions for unsigned integers
    uintReturnFunctions.add(new FunctionStruct("bitfieldInsert",
        new UnifiedTypeProxy(BasicType.UINT),
        new UnifiedTypeProxy(BasicType.UINT), new UnifiedTypeProxy(BasicType.UINT),
        new UnifiedTypeProxy(BasicType.INT), new UnifiedTypeProxy(BasicType.INT)));
    uvec2ReturnFunctions.add(new FunctionStruct("bitfieldInsert",
        new UnifiedTypeProxy(BasicType.UVEC2),
        new UnifiedTypeProxy(BasicType.UVEC2), new UnifiedTypeProxy(BasicType.UVEC2),
        new UnifiedTypeProxy(BasicType.INT), new UnifiedTypeProxy(BasicType.INT)));
    uvec3ReturnFunctions.add(new FunctionStruct("bitfieldInsert",
        new UnifiedTypeProxy(BasicType.UVEC3),
        new UnifiedTypeProxy(BasicType.UVEC3), new UnifiedTypeProxy(BasicType.UVEC3),
        new UnifiedTypeProxy(BasicType.INT), new UnifiedTypeProxy(BasicType.INT)));
    uvec4ReturnFunctions.add(new FunctionStruct("bitfieldInsert",
        new UnifiedTypeProxy(BasicType.UVEC4),
        new UnifiedTypeProxy(BasicType.UVEC4), new UnifiedTypeProxy(BasicType.UVEC4),
        new UnifiedTypeProxy(BasicType.INT), new UnifiedTypeProxy(BasicType.INT)));


    // Bitfield reverse for integers
    intReturnFunctions.add(new FunctionStruct("bitfieldReverse",
        new UnifiedTypeProxy(BasicType.INT),
        new UnifiedTypeProxy(BasicType.INT)));
    ivec2ReturnFunctions.add(new FunctionStruct("bitfieldReverse",
        new UnifiedTypeProxy(BasicType.IVEC2),
        new UnifiedTypeProxy(BasicType.IVEC2)));
    ivec3ReturnFunctions.add(new FunctionStruct("bitfieldReverse",
        new UnifiedTypeProxy(BasicType.IVEC3),
        new UnifiedTypeProxy(BasicType.IVEC3)));
    ivec4ReturnFunctions.add(new FunctionStruct("bitfieldReverse",
        new UnifiedTypeProxy(BasicType.IVEC4),
        new UnifiedTypeProxy(BasicType.IVEC4)));

    // Bitfield reverse for unsigned integers
    uintReturnFunctions.add(new FunctionStruct("bitfieldReverse",
        new UnifiedTypeProxy(BasicType.UINT),
        new UnifiedTypeProxy(BasicType.UINT)));
    uvec2ReturnFunctions.add(new FunctionStruct("bitfieldReverse",
        new UnifiedTypeProxy(BasicType.UVEC2),
        new UnifiedTypeProxy(BasicType.UVEC2)));
    uvec3ReturnFunctions.add(new FunctionStruct("bitfieldReverse",
        new UnifiedTypeProxy(BasicType.UVEC3),
        new UnifiedTypeProxy(BasicType.UVEC3)));
    uvec4ReturnFunctions.add(new FunctionStruct("bitfieldReverse",
        new UnifiedTypeProxy(BasicType.UVEC4),
        new UnifiedTypeProxy(BasicType.UVEC4)));

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Shader invocation Control and Shader memory control
    ////////////////////////////////////////////////////////////////////////////////////////////////

    if (shaderKind == ShaderKind.COMPUTE) {
      voidReturnFunctions.add(new FunctionStruct("barrier", new UnifiedTypeProxy(VoidType.VOID)));
    }
    voidReturnFunctions.add(new FunctionStruct("memoryBarrier",
        new UnifiedTypeProxy(VoidType.VOID)));
    voidReturnFunctions.add(new FunctionStruct("memoryBarrierAtomicCounter",
        new UnifiedTypeProxy(VoidType.VOID)));
    voidReturnFunctions.add(new FunctionStruct("memoryBarrierBuffer",
        new UnifiedTypeProxy(VoidType.VOID)));
    voidReturnFunctions.add(new FunctionStruct("memoryBarrierShared",
        new UnifiedTypeProxy(VoidType.VOID)));
    voidReturnFunctions.add(new FunctionStruct("memoryBarrierImage",
        new UnifiedTypeProxy(VoidType.VOID)));
    voidReturnFunctions.add(new FunctionStruct("groupMemoryBarrier",
        new UnifiedTypeProxy(VoidType.VOID)));

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
    stdFunctions.put(new UnifiedTypeProxy(VoidType.VOID), voidReturnFunctions);
  }


  public FunctionStruct getRandomStdFunctionStruct(UnifiedTypeProxy returnType) {
    if (stdFunctions.containsKey(returnType)) {
      List<FunctionStruct> availableFunctions = stdFunctions.get(returnType);
      return availableFunctions.get(randGen.nextInt(availableFunctions.size()));
    } else {
      throw new UnsupportedOperationException("No function exists for the given return type");
    }
  }

  public FunctionStruct getRandomFunctionStruct(UnifiedTypeProxy returnType) {
    if (userDefinedFunctions.containsKey(returnType)
        && !userDefinedFunctions.get(returnType).isEmpty() && randGen.nextBoolean()) {
      List<FunctionStruct> availableFunctions = userDefinedFunctions.get(returnType);
      return availableFunctions.get(randGen.nextInt(availableFunctions.size()));
    } else {
      return getRandomStdFunctionStruct(returnType);
    }
  }

  public void addUserDefinedFunction(FunctionStruct newFunc) {
    if (!userDefinedFunctions.containsKey(newFunc.returnType)) {
      userDefinedFunctions.put(newFunc.returnType, new ArrayList<>());
    }
    userDefinedFunctions.get(newFunc.returnType).add(newFunc);
  }
}
