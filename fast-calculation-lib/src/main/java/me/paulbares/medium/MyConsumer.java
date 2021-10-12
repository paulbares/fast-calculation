package me.paulbares.medium;

import ch.randelshofer.fastdoubleparser.FastDoubleParser;

public abstract class MyConsumer implements Consumer<AggregateResult> {

    private final CharArray charArray = new CharArray();
    protected final AggregateResult result = createResult();

    private int count;
    private int year;
    private int mileage;
    private double price;

    public abstract AggregateResult createResult();

    @Override
    public void accept(char[] a, int length) {
        CharArray charSeq = charArray.withArray(a).withLength(length);
        if (count == 0) {
            year = Integer.parseInt(charSeq, 0, length, 10);
        } else if (count == 1) {
            mileage = Integer.parseInt(charSeq, 0, length, 10);
        } else if (count == 2) {
            price = FastDoubleParser.parseDouble(charSeq);
            result.aggregate(year, mileage, price);
        }
        count++;
    }

    @Override
    public void eol() {
        count = 0; // reset
    }

    @Override
    public AggregateResult getResult() {
        return this.result;
    }
}
