package me.paulbares;

public class JacksonTestParser {

  public static void main(String[] args) throws Exception {
    int a = 129;
    int sum = 2;

    int t = (a - 128) >> 31;
    sum += ~t & a;
//    System.out.println(Integer.toBinaryString(t));
//    System.out.println(Integer.toBinaryString(t >>> 1));
//    System.out.println(Integer.toBinaryString(Integer.MAX_VALUE));
//    System.out.println((t >>> 1) == Integer.MAX_VALUE);
//    System.out.println((t >>> 1) | a);
//    double v = Double.longBitsToDouble(((long) ~t) & (Double.doubleToRawLongBits(0.5)));
    double v = Double.longBitsToDouble(((long) ~t) & (long) 0.5);
    System.out.println(v);
//    System.out.println("result:" + sign1(0));
//    System.out.println("result:" + sign2(0));

    // if a < 128, then ~t is 0.
    // if a >= 128 then ~t is -1, ~t is Integer.MAX_VALUE

    // We want min = min(min, b) if  a >= 128
    // We want min = min(min, Integer.MAX_VALUE) if a < 128
    // we want arg = if a >= 128 { b } else { Integer.MAX_VALUE }
  }

  /**
   * 1 if positive, 0 if negative or zero !! different than {@link #sign2(int)}
   *
   * @param i
   * @return
   */
  static int sign1(int i) {
    return (-i) >>> 31;
  }

  /**
   * 1 if positive or zero, 0 if negative
   *
   * @param i
   * @return
   */
  static int sign2(int i) {
    return (~(i >> 31)) >>> 31;
  }
}
