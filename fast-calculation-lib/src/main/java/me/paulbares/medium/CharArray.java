package me.paulbares.medium;

public class CharArray implements CharSequence {

    /**
     * Holds the character array.
     */
    private char[] array;

    /**
     * Holds the length of char sequence.
     */
    private int length;

    @Override
    public int length() {
        return this.length;
    }

    @Override
    public char charAt(int index) {
        // no boundary check to maximize the performance !
        return this.array[index];
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        throw new RuntimeException("Not implemented"); // we do not need this
    }

    public CharArray withArray(char[] array) {
        this.array = array;
        return this;
    }

    public CharArray withLength(int length) {
        this.length = length;
        return this;
    }

    @Override
    public String toString() {
        // we have to implement this method because FastDoubleParser#parseRestOfDecimalFloatLiteralTheHardWay
        return new String(this.array, 0, this.length);
    }
}
