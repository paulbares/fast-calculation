package org.example;

/**
 * From https://kholdstare.github.io/technical/2020/05/26/faster-integer-parsing.html
 */
public class FastParser {

    public static void main(String[] args) {
        // works for number of 8 digits!!
        System.out.println(parse("12345678".toCharArray()));
        System.out.println(parse("123".toCharArray()));
        System.out.println(parse("1234567".toCharArray()));
        System.out.println(parse("1".toCharArray()));
    }

    public static long parse(char[] string) { // should be of size 8
        long chunk = 0;
        for (int i = 0; i < string.length; i++) {
            int shift = 8 * i;
            long s = (long) string[i] << shift;
            chunk |= s;
//            System.out.println(Long.toBinaryString(string[i]));
//            System.out.println(Long.toBinaryString(s));
//            System.out.println("=========");
        }


        // 1-byte mask trick (works on 4 pairs of single digits)
        long lower_digits = (chunk & 0x0f000f000f000f00l) >> 8;
        long upper_digits = (chunk & 0x000f000f000f000fl) * 10;
        chunk = lower_digits + upper_digits;

        // 2-byte mask trick (works on 2 pairs of two digits)
        lower_digits = (chunk & 0x00ff000000ff0000l) >> 16;
        upper_digits = (chunk & 0x000000ff000000ffl) * 100;
        chunk = lower_digits + upper_digits;

        // 4-byte mask trick (works on pair of four digits)
        lower_digits = (chunk & 0x0000ffff00000000l) >> 32;
        upper_digits = (chunk & 0x000000000000ffffl) * 10000;
        chunk = lower_digits + upper_digits;

        return chunk;
    }
}
