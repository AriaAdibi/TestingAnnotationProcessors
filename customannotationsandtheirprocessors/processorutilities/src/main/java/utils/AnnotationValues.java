package utils;

import com.google.common.base.Equivalence;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleAnnotationValueVisitor9;
import java.util.List;
import java.util.function.Function;

/**
 * A utility class for working with {@link AnnotationValue} instances.
 */
public final class AnnotationValues {

  private AnnotationValues() {
  }

  /**
   * Returns a string representation of the given annotation value, suitable for inclusion in a Java
   * source file as part of an annotation. For example, if {@code annotationValue} represents the
   * string {@code unchecked} in the annotation {@code @SuppressWarnings("unchecked")}, this method
   * will return the string {@code "unchecked"}, which you can then use as part of an annotation
   * being generated.
   *
   * <p>For all annotation values other than nested annotations, the returned string can also be
   * used to initialize a variable of the appropriate type.
   *
   * <p>Fully qualified names are used for types in annotations, class literals, and enum constants,
   * ensuring that the source form will compile without requiring additional imports.
   *
   * @param annotationValue the {@linkplain AnnotationValue} to be represented
   * @return a string representation of the {@code annotationValue}
   */
  public static String toString(AnnotationValue annotationValue) {
    return AnnotationOutput.toString(annotationValue);
  }

  /* ********************************************************************* */
  /* Equivalency ********************************************************* */
  /* ********************************************************************* */

  /**
   * Returns an {@link Equivalence} for {@link AnnotationValue} as annotation values may
   * contain {@link AnnotationMirror} instances some of whose implementations delegate
   * equality tests to {@link Object#equals} whereas the documentation explicitly states
   * that instance/reference equality is not the proper test.
   *
   * @return an {@link Equivalence} that can be used to compare {@linkplain AnnotationValue}s.
   * @see AnnotationMirrors#equivalence()
   */
  public static Equivalence<AnnotationValue> equivalence() {
    return AnnotationValueEquivalence.INSTANCE;
  }

  @SuppressWarnings("Convert2Diamond")
  private static final class AnnotationValueEquivalence extends Equivalence<AnnotationValue> {
    private static final AnnotationValueEquivalence INSTANCE = new AnnotationValueEquivalence();

    @Override
    @SuppressWarnings("NullableProblems")
    protected boolean doEquivalent(AnnotationValue left, AnnotationValue right) {
      return left.accept(
          new SimpleAnnotationValueVisitor9<Boolean, AnnotationValue>() {
            // LHS is not an annotation or array of annotation values, so just test equality.
            @Override
            protected Boolean defaultAction(Object left, AnnotationValue right) {
              return left.equals(
                  right.accept(
                      new SimpleAnnotationValueVisitor9<Object, Void>() {
                        @Override
                        protected Object defaultAction(Object object, Void unused) {
                          return object;
                        }
                      },
                      null
                  )
              );
            }

            // LHS is an annotation mirror so test equivalence for RHS annotation mirror
            // and false for other types.
            @Override
            public Boolean visitAnnotation(AnnotationMirror left, AnnotationValue right) {
              return right.accept(
                  new SimpleAnnotationValueVisitor9<Boolean, AnnotationMirror>() {
                    @Override
                    protected Boolean defaultAction(Object right, AnnotationMirror left) {
                      return false; // Not an annotation mirror, so can't be equal to such.
                    }

                    @Override
                    public Boolean visitAnnotation(AnnotationMirror right, AnnotationMirror left) {
                      return AnnotationMirrors.equivalence().equivalent(left, right);
                    }
                  },
                  left);
            }

            // LHS is a list of annotation values have to collect-test equivalences, or false
            // for any other types.
            @Override
            public Boolean visitArray(List<? extends AnnotationValue> left, AnnotationValue right) {
              return right.accept(
                  new SimpleAnnotationValueVisitor9<Boolean, List<? extends AnnotationValue>>() {
                    @Override
                    protected Boolean defaultAction(Object right, List<? extends AnnotationValue> left) {
                      return false; // Not an array, so can't be equal to such.
                    }

                    @SuppressWarnings("unchecked") // safe covariant cast
                    @Override
                    public Boolean visitArray(
                        List<? extends AnnotationValue> right,
                        List<? extends AnnotationValue> left) {
                      return AnnotationValues.equivalence()
                          .pairwise()
                          .equivalent((List<AnnotationValue>) left, (List<AnnotationValue>) right);
                    }
                  },
                  left);
            }

            @Override
            public Boolean visitType(TypeMirror left, AnnotationValue right) {
              return right.accept(
                  new SimpleAnnotationValueVisitor9<Boolean, TypeMirror>() {
                    @Override
                    protected Boolean defaultAction(Object right, TypeMirror left) {
                      return false; // Not an annotation mirror, so can't be equal to such.
                    }

                    @Override
                    public Boolean visitType(TypeMirror right, TypeMirror left) {
                      return MoreTypes.equivalence().equivalent(left, right);
                    }
                  },
                  left);
            }
          },
          right
      );
    }

    @Override
    protected int doHash(AnnotationValue value) {
      return value.accept(
          new SimpleAnnotationValueVisitor9<Integer, Void>() {
            @Override
            protected Integer defaultAction(Object value, Void ignore) {
              return value.hashCode();
            }

            @Override
            public Integer visitAnnotation(AnnotationMirror value, Void ignore) {
              return AnnotationMirrors.equivalence().hash(value);
            }

            @SuppressWarnings("unchecked") // safe covariant cast
            @Override
            public Integer visitArray(List<? extends AnnotationValue> values, Void ignore) {
              return AnnotationValues.equivalence()
                  .pairwise()
                  .hash((List<AnnotationValue>) values);
            }

            @Override
            public Integer visitType(TypeMirror value, Void ignore) {
              return MoreTypes.equivalence().hash(value);
            }
          },
          null
      );
    }

    @Override
    public String toString() {
      return "AnnotationValues.equivalence()";
    }
  }

  /* ********************************************************************* */
  /* Getters ************************************************************* */
  /* ********************************************************************* */

  /**
   * Returns the value as a {@link DeclaredType}.
   *
   * @param value the {@linkplain AnnotationValue} whose value is being extracted
   * @return the value as a {@linkplain DeclaredType}
   * @throws IllegalArgumentException if the value is not a {@linkplain DeclaredType}
   */
  public static DeclaredType getDeclaredType(AnnotationValue value) {
    return DeclaredTypeVisitor.INSTANCE.visit(value);
  }

  /**
   * Returns the value as an {@link AnnotationMirror}.
   *
   * @param value the {@linkplain AnnotationValue} whose value is being extracted
   * @return the value as a {@linkplain AnnotationMirror}
   * @throws IllegalArgumentException if the value is not an {@linkplain AnnotationMirror}
   */
  public static AnnotationMirror getAnnotationMirror(AnnotationValue value) {
    return AnnotationMirrorVisitor.INSTANCE.visit(value);
  }

  /**
   * Returns the value as a {@link VariableElement}.
   *
   * @param value the {@linkplain AnnotationValue} whose value is being extracted
   * @return the value as a {@linkplain VariableElement}
   * @throws IllegalArgumentException if the value is not an {@linkplain Enum}
   */
  public static VariableElement getEnum(AnnotationValue value) {
    return EnumVisitor.INSTANCE.visit(value);
  }

  private static class DefaultValueExtractorVisitor<T> extends SimpleAnnotationValueVisitor9<T, Void> {
    final Class<T> clazz;

    DefaultValueExtractorVisitor(Class<T> clazz) {
      this.clazz = Preconditions.checkNotNull(clazz);
    }

    @Override
    public T defaultAction(Object o, Void ignore) {
      throw new IllegalArgumentException(
          "Expected a " + clazz.getSimpleName() + ", got instead: " + o);
    }
  }


  private static final class DeclaredTypeVisitor extends DefaultValueExtractorVisitor<DeclaredType> {
    static final DeclaredTypeVisitor INSTANCE = new DeclaredTypeVisitor();

    DeclaredTypeVisitor() {
      super(DeclaredType.class);
    }

    @Override
    public DeclaredType visitType(TypeMirror value, Void ignore) {
      return MoreTypes.asDeclared(value);
    }
  }


  private static final class AnnotationMirrorVisitor extends DefaultValueExtractorVisitor<AnnotationMirror> {
    static final AnnotationMirrorVisitor INSTANCE = new AnnotationMirrorVisitor();

    AnnotationMirrorVisitor() {
      super(AnnotationMirror.class);
    }

    @Override
    public AnnotationMirror visitAnnotation(AnnotationMirror value, Void ignore) {
      return value;
    }
  }


  private static final class EnumVisitor extends DefaultValueExtractorVisitor<VariableElement> {
    static final EnumVisitor INSTANCE = new EnumVisitor();

    EnumVisitor() {
      super(VariableElement.class);
    }

    @Override
    public VariableElement visitEnumConstant(VariableElement value, Void ignore) {
      return value;
    }
  }

  /* Primitives and String */

  /**
   * Returns the value as a {@link String}.
   *
   * @param value the {@linkplain AnnotationValue} whose value is being extracted
   * @return the value as a {@linkplain String}
   * @throws IllegalArgumentException if the value is not an {@linkplain String}
   */
  public static String getString(AnnotationValue value) {
    return valueOfType(value, String.class);
  }

  /**
   * Returns the value as an int.
   *
   * @param value the {@linkplain AnnotationValue} whose value is being extracted
   * @return the value as an int
   * @throws IllegalArgumentException if the value is not an int
   */
  public static int getInt(AnnotationValue value) {
    return valueOfType(value, Integer.class);
  }

  /**
   * Returns the value as a long.
   *
   * @param value the {@linkplain AnnotationValue} whose value is being extracted
   * @return the value as a long
   * @throws IllegalArgumentException if the value is not a long
   */
  public static long getLong(AnnotationValue value) {
    return valueOfType(value, Long.class);
  }

  /**
   * Returns the value as a byte.
   *
   * @param value the {@linkplain AnnotationValue} whose value is being extracted
   * @return the value as a byte
   * @throws IllegalArgumentException if the value is not a byte
   */
  public static byte getByte(AnnotationValue value) {
    return valueOfType(value, Byte.class);
  }

  /**
   * Returns the value as a short.
   *
   * @param value the {@linkplain AnnotationValue} whose value is being extracted
   * @return the value as a short
   * @throws IllegalArgumentException if the value is not a short
   */
  public static short getShort(AnnotationValue value) {
    return valueOfType(value, Short.class);
  }

  /**
   * Returns the value as a float.
   *
   * @param value the {@linkplain AnnotationValue} whose value is being extracted
   * @return the value as a float
   * @throws IllegalArgumentException if the value is not a float
   */
  public static float getFloat(AnnotationValue value) {
    return valueOfType(value, Float.class);
  }

  /**
   * Returns the value as a double.
   *
   * @param value the {@linkplain AnnotationValue} whose value is being extracted
   * @return the value as a double
   * @throws IllegalArgumentException if the value is not a double
   */
  public static double getDouble(AnnotationValue value) {
    return valueOfType(value, Double.class);
  }

  /**
   * Returns the value as a boolean.
   *
   * @param value the {@linkplain AnnotationValue} whose value is being extracted
   * @return the value as a boolean
   * @throws IllegalArgumentException if the value is not a boolean
   */
  public static boolean getBoolean(AnnotationValue value) {
    return valueOfType(value, Boolean.class);
  }

  /**
   * Returns the value as a char.
   *
   * @param value the {@linkplain AnnotationValue} whose value is being extracted
   * @return the value as a char
   * @throws IllegalArgumentException if the value is not a char
   */
  public static char getChar(AnnotationValue value) {
    return valueOfType(value, Character.class);
  }

  private static <T> T valueOfType(AnnotationValue annotationValue, Class<T> type) {
    Object value = annotationValue.getValue();
    if (!type.isInstance(value)) {
      throw new IllegalArgumentException(
          "Expected " + type.getSimpleName() + ", got instead: " + value);
    }
    return type.cast(value);
  }

  /* Arrays */

  private static final ArrayVisitor<DeclaredType> TYPE_MIRRORS_VISITOR =
      new ArrayVisitor<>(AnnotationValues::getDeclaredType);

  /**
   * Returns the value as a list of {@link DeclaredType}s.
   *
   * @param value the {@linkplain AnnotationValue} whose value is being extracted
   * @return the value as a list of {@linkplain DeclaredType}s
   * @throws IllegalArgumentException if the value is not an array of {@linkplain DeclaredType}.
   */
  public static ImmutableList<DeclaredType> getDeclaredTypes(AnnotationValue value) {
    return TYPE_MIRRORS_VISITOR.visit(value);
  }

  private static final ArrayVisitor<AnnotationMirror> ANNOTATION_MIRRORS_VISITOR =
      new ArrayVisitor<>(AnnotationValues::getAnnotationMirror);

  /**
   * Returns the value as a list of {@link AnnotationMirror}s.
   *
   * @param value the {@linkplain AnnotationValue} whose value is being extracted
   * @return the value as a list of {@linkplain AnnotationMirror}s
   * @throws IllegalArgumentException if the value is not an array of {@linkplain AnnotationMirror}.
   */
  public static ImmutableList<AnnotationMirror> getAnnotationMirrors(AnnotationValue value) {
    return ANNOTATION_MIRRORS_VISITOR.visit(value);
  }

  private static final ArrayVisitor<VariableElement> ENUMS_VISITOR =
      new ArrayVisitor<>(AnnotationValues::getEnum);

  /**
   * Returns the value as a list of {@link VariableElement}s.
   *
   * @param value the {@linkplain AnnotationValue} whose value is being extracted
   * @return the value as a list of {@linkplain VariableElement}s
   * @throws IllegalArgumentException if the value is not an array of {@linkplain Enum}.
   */
  public static ImmutableList<VariableElement> getEnums(AnnotationValue value) {
    return ENUMS_VISITOR.visit(value);
  }

  private static final ArrayVisitor<String> STRINGS_VISITOR =
      new ArrayVisitor<>(AnnotationValues::getString);

  /**
   * Returns the value as a list of {@link String}s.
   *
   * @param value the {@linkplain AnnotationValue} whose value is being extracted
   * @return the value as a list of {@linkplain String}s
   * @throws IllegalArgumentException if the value is not an array of {@linkplain String}.
   */
  public static ImmutableList<String> getStrings(AnnotationValue value) {
    return STRINGS_VISITOR.visit(value);
  }

  private static final ArrayVisitor<Integer> INTS_VISITOR =
      new ArrayVisitor<>(AnnotationValues::getInt);

  /**
   * Returns the value as a list of {@link Integer}s.
   *
   * @param value the {@linkplain AnnotationValue} whose value is being extracted
   * @return the value as a list of {@linkplain Integer}s
   * @throws IllegalArgumentException if the value is not an array of {@linkplain Integer}.
   */
  public static ImmutableList<Integer> getInts(AnnotationValue value) {
    return INTS_VISITOR.visit(value);
  }

  private static final ArrayVisitor<Long> LONGS_VISITOR =
      new ArrayVisitor<>(AnnotationValues::getLong);

  /**
   * Returns the value as a list of {@link Long}s.
   *
   * @param value the {@linkplain AnnotationValue} whose value is being extracted
   * @return the value as a list of {@linkplain Long}s
   * @throws IllegalArgumentException if the value is not an array of {@linkplain Long}.
   */
  public static ImmutableList<Long> getLongs(AnnotationValue value) {
    return LONGS_VISITOR.visit(value);
  }

  private static final ArrayVisitor<Byte> BYTES_VISITOR =
      new ArrayVisitor<>(AnnotationValues::getByte);

  /**
   * Returns the value as a list of {@link Byte}s.
   *
   * @param value the {@linkplain AnnotationValue} whose value is being extracted
   * @return the value as a list of {@linkplain Byte}s
   * @throws IllegalArgumentException if the value is not an array of {@linkplain Byte}.
   */
  public static ImmutableList<Byte> getBytes(AnnotationValue value) {
    return BYTES_VISITOR.visit(value);
  }

  private static final ArrayVisitor<Short> SHORTS_VISITOR =
      new ArrayVisitor<>(AnnotationValues::getShort);

  /**
   * Returns the value as a list of {@link Short}s.
   *
   * @param value the {@linkplain AnnotationValue} whose value is being extracted
   * @return the value as a list of {@linkplain Short}s
   * @throws IllegalArgumentException if the value is not an array of {@linkplain Short}.
   */
  public static ImmutableList<Short> getShorts(AnnotationValue value) {
    return SHORTS_VISITOR.visit(value);
  }

  private static final ArrayVisitor<Float> FLOATS_VISITOR =
      new ArrayVisitor<>(AnnotationValues::getFloat);

  /**
   * Returns the value as a list of {@link Float}s.
   *
   * @param value the {@linkplain AnnotationValue} whose value is being extracted
   * @return the value as a list of {@linkplain Float}s
   * @throws IllegalArgumentException if the value is not an array of {@linkplain Float}.
   */
  public static ImmutableList<Float> getFloats(AnnotationValue value) {
    return FLOATS_VISITOR.visit(value);
  }

  private static final ArrayVisitor<Double> DOUBLES_VISITOR =
      new ArrayVisitor<>(AnnotationValues::getDouble);

  /**
   * Returns the value as a list of {@link Double}s.
   *
   * @param value the {@linkplain AnnotationValue} whose value is being extracted
   * @return the value as a list of {@linkplain Double}s
   * @throws IllegalArgumentException if the value is not an array of {@linkplain Double}.
   */
  public static ImmutableList<Double> getDoubles(AnnotationValue value) {
    return DOUBLES_VISITOR.visit(value);
  }

  private static final ArrayVisitor<Boolean> BOOLEANS_VISITOR =
      new ArrayVisitor<>(AnnotationValues::getBoolean);

  /**
   * Returns the value as a list of {@link Boolean}s.
   *
   * @param value the {@linkplain AnnotationValue} whose value is being extracted
   * @return the value as a list of {@linkplain Boolean}s
   * @throws IllegalArgumentException if the value is not an array of {@linkplain Boolean}.
   */
  public static ImmutableList<Boolean> getBooleans(AnnotationValue value) {
    return BOOLEANS_VISITOR.visit(value);
  }

  private static final ArrayVisitor<Character> CHARS_VISITOR =
      new ArrayVisitor<>(AnnotationValues::getChar);

  /**
   * Returns the value as a list of {@link Character}s.
   *
   * @param value the {@linkplain AnnotationValue} whose value is being extracted
   * @return the value as a list of {@linkplain Character}s
   * @throws IllegalArgumentException if the value is not an array of {@linkplain Character}.
   */
  public static ImmutableList<Character> getChars(AnnotationValue value) {
    return CHARS_VISITOR.visit(value);
  }

  private static final ArrayVisitor<AnnotationValue> ANNOTATION_VALUES_VISITOR =
      new ArrayVisitor<>(x -> x);

  /**
   * Returns the value as a list of {@link AnnotationValue}s.
   *
   * @param value the {@linkplain AnnotationValue} whose value is being extracted
   * @return the value as a list of {@linkplain AnnotationValue}s
   * @throws IllegalArgumentException if the value is not an array of {@linkplain AnnotationValue}.
   */
  public static ImmutableList<AnnotationValue> getAnnotationValues(AnnotationValue value) {
    return ANNOTATION_VALUES_VISITOR.visit(value);
  }

  private static final class ArrayVisitor<T> extends SimpleAnnotationValueVisitor9<ImmutableList<T>, Void> {
    final Function<AnnotationValue, T> visitT;

    ArrayVisitor(Function<AnnotationValue, T> visitT) {
      this.visitT = Preconditions.checkNotNull(visitT);
    }

    @Override
    public ImmutableList<T> defaultAction(Object o, Void unused) {
      throw new IllegalArgumentException("Expected an array, got instead: " + o); //TODO changed to IllegalArgument
    }

    @Override
    public ImmutableList<T> visitArray(List<? extends AnnotationValue> values, Void unused) {
      return values.stream().map(visitT).collect(ImmutableList.toImmutableList());
    }
  }

}
