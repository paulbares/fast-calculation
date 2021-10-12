package me.paulbares.medium;

interface Consumer<T extends AggregateResult> {
    void accept(char[] a, int length);

    void eol();

    T getResult();
}
