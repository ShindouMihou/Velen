package pw.mihou.velen.utils;

import java.util.Objects;

public class Pair<L, R> {

    private final L left;
    private final R right;

    /**
     * Creates a new Pair of Key-Value or otherwise called, Left-Right object.
     * This is used to hold two objects at the same time.
     *
     * @param left  The left value.
     * @param right The right value.
     */
    public Pair(L left, R right) {
        this.left = left;
        this.right = right;
    }

    /**
     * Creates a new Pair of Key-Value or otherwise called, Left-Right object.
     * This is used to hold two objects at the same time.
     *
     * @param left  The value of the key (or the left item).
     * @param right The value of the value (or the right item).
     * @param <L>   The type of the key value.
     * @param <R>   The type of the left value.
     * @return A key-value or left-right object containing all the values.
     */
    public static <L, R> Pair<L, R> of(L left, R right) {
        return new Pair<>(left, right);
    }

    /**
     * Returns the key or the left value.
     *
     * @return The key or the left value.
     */
    public L getLeft() {
        return left;
    }

    /**
     * Returns the value or the right value.
     *
     * @return The value or the right value.
     */
    public R getRight() {
        return right;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return getLeft().equals(pair.getLeft()) &&
                getRight().equals(pair.getRight());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getLeft(), getRight());
    }

    @Override
    public String toString() {
        return "Pair (" + left + ", " + right + ")";
    }
}
