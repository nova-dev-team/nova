package nova.common.util;

public class Pair<FirstType, SecondType> {

    public FirstType first;

    public SecondType second;

    public Pair() {
        this.first = null;
        this.second = null;
    }

    public Pair(FirstType first, SecondType second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Pair<?, ?>) {
            @SuppressWarnings("unchecked")
            Pair<FirstType, SecondType> another = (Pair<FirstType, SecondType>) o;
            return another.first.equals(first) && another.second.equals(second);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return "(" + first.toString() + ", " + second.toString() + ")";
    }

    @Override
    public int hashCode() {
        int h1 = first.hashCode();
        int h2 = second.hashCode();
        return h1 ^ h2;
    }

    public FirstType getFirst() {
        return first;
    }

    public void setFirst(FirstType first) {
        this.first = first;
    }

    public SecondType getSecond() {
        return second;
    }

    public void setSecond(SecondType second) {
        this.second = second;
    }

}
