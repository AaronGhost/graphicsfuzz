package com.graphicsfuzz;

public class FuzzerConstants {
  //Compute shader Local size parameters
  public static final int MAX_LOCAL_SIZE_X = 2;
  public static final int MAX_LOCAL_SIZE_Y = 2;
  public static final int MAX_LOCAL_SIZE_Z = 2;

  //buffer generations parameters
  public static final int MAX_INPUT_BUFFERS = 5;
  public static final int MAX_OUTPUT_BUFFERS = 3;
  public static final int MAX_BUFFER_ELEMENTS = 4;

  //Arrays parameters
  public static final int MAX_ARRAY_LENGTH = 10;

  //General program parameters
  public static final int MAX_MAIN_LENGTH = 15;
  public static final int MAX_SCOPE_DEPTH = 3;

  public static final int MAX_VARDECL_ELEMENTS = 2;


  public static final int MAX_EXPR_DEPTH = 3;
  public static final int MAX_SWIZZLE_DEPTH = 2;

  //Max and Min integer values (happily they are 32 bytes on Java as well as in Glsl)
  public static final int MAX_INT_VALUE = Integer.MAX_VALUE;
  public static final int MIN_INT_VALUE = Integer.MIN_VALUE;
  public static final long MAX_UINT_VALUE = 4294967295L;

  //Range bounds for the special random generator
  public static final int MAX_SMALL = 128;
  public static final int MIN_SMALL = -128;
  public static final int MIN_HIGH = MAX_INT_VALUE - (MAX_INT_VALUE >> 3);
  public static final int MAX_HIGH = MIN_INT_VALUE - (MIN_INT_VALUE >> 3);

}
