package com.graphicsfuzz.config;

public class FuzzerConstants {
  //Max and Min integer values (happily they are 32 bytes on Java as well as in Glsl)
  public static final int MAX_INT_VALUE = Integer.MAX_VALUE;
  public static final int MIN_INT_VALUE = Integer.MIN_VALUE;
  //Max uint value stored as a long value
  public static final long MAX_UINT_VALUE = 4294967295L;

  //Range bounds for the special random generator
  public static final int MAX_SMALL = 128;
  public static final int MIN_SMALL = -128;
  public static final int MIN_HIGH = MAX_INT_VALUE - (MAX_INT_VALUE >> 3);
  public static final int MAX_HIGH = MIN_INT_VALUE - (MIN_INT_VALUE >> 3);
  public static final int MAX_PERMITTED_FLOAT = (1 << 24) - 1;
}
