package com.graphicsfuzz.random;

import com.graphicsfuzz.FuzzerConstants;
import com.graphicsfuzz.common.util.RandomWrapper;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class MultipleRangeRandomWrapper extends RandomWrapper {

  public enum GeneratorType {
    SMALL,
    HIGH,
    FULL,
    SPECIAL
  }

  private final List<GeneratorType> generatorList;
  private final List<Integer> specialPositiveIntValues = Arrays.asList(0, 1, 2,
      FuzzerConstants.MAX_INT_VALUE);
  private final List<Integer> specialIntValues = Arrays.asList(0, -1, 1, 2, -2,
      FuzzerConstants.MAX_INT_VALUE, FuzzerConstants.MIN_INT_VALUE);
  private final List<Long> specialUIntValues = Arrays.asList(0L, 1L,
      2L, FuzzerConstants.MAX_UINT_VALUE);

  public MultipleRangeRandomWrapper(long seed, GeneratorType... types) {
    super(seed);
    if (types.length == 0) {
      generatorList = Collections.singletonList(GeneratorType.FULL);
    } else {
      generatorList = Arrays.asList(types);
    }
  }

  public MultipleRangeRandomWrapper(long seed) {
    this(seed, GeneratorType.SMALL, GeneratorType.HIGH, GeneratorType.FULL,
        GeneratorType.SPECIAL);
  }

  private GeneratorType pickRandomGenerator() {
    return generatorList.get(super.nextInt(generatorList.size()));
  }

  @Override
  public int nextInt(int bound) {
    if (bound == Integer.MAX_VALUE) {
      switch (pickRandomGenerator()) {
        case SMALL:
          return super.nextInt(FuzzerConstants.MAX_SMALL);
        case HIGH:
          return super.nextInt(FuzzerConstants.MIN_HIGH, FuzzerConstants.MAX_INT_VALUE);
        case FULL:
          return super.nextInt(bound);
        default:
          return specialPositiveIntValues.get(super.nextInt(specialPositiveIntValues.size()));
      }
    } else {
      return super.nextInt(bound);
    }
  }

  @Override
  public int nextInt(int origin, int bound) {
    if (origin == Integer.MIN_VALUE && bound == Integer.MAX_VALUE) {
      switch (pickRandomGenerator()) {
        case SMALL:
          return super.nextInt(FuzzerConstants.MIN_SMALL, FuzzerConstants.MAX_SMALL);
        case HIGH:
          return super.nextBoolean() ? super.nextInt(FuzzerConstants.MIN_INT_VALUE,
              FuzzerConstants.MAX_HIGH) : super.nextInt(FuzzerConstants.MIN_HIGH,
              FuzzerConstants.MAX_INT_VALUE);
        case FULL:
          return super.nextInt(origin, bound);
        default:
          return specialIntValues.get(super.nextInt(specialIntValues.size()));
      }
    } else {
      return super.nextInt(origin, bound);
    }
  }

  @Override
  public long nextLong(long bound) {
    if (bound == FuzzerConstants.MAX_UINT_VALUE) {
      switch (pickRandomGenerator()) {
        case SMALL:
          return super.nextLong(FuzzerConstants.MAX_SMALL);
        case HIGH:
          return ((long) super.nextInt(FuzzerConstants.MIN_HIGH, FuzzerConstants.MAX_INT_VALUE))
              << 1;
        case FULL:
          return super.nextLong(bound);
        default:
          return specialUIntValues.get(super.nextInt(specialUIntValues.size()));
      }
    } else {
      return super.nextLong(bound);
    }
  }
}
