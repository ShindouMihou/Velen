package pw.mihou.velen.pagination.entities;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class Paginator<T> {

    private final List<T> items;
    private final AtomicInteger arrow = new AtomicInteger(0);

    public Paginator(List<T> items) {
        this.items = items;
    }

    /**
     * Creates a new Paginator of type.
     *
     * @param items The items inside the paginator.
     * @param <T>   The type of the items.
     * @return A paginator.
     */
    public static <T> Paginator<T> of(List<T> items) {
        return new Paginator<T>(items);
    }

    /**
     * Gets the current item in the current index.
     * <br><br>
     * <b>Important:</b> This can return null if you move the arrow by yourself, if you want to move
     * to another position, please use {@link Paginator#reverse()} to move backwards or {@link Paginator#next()} to
     * move forward instead since they won't cause {@link Paginator#current()} to become null.
     *
     * @return The item inside the current position.
     */
    public T current() {
        return items.get(arrow.get());
    }

    /**
     * Moves the arrow a notch backwards and returns the value (if possible),
     * otherwise will return an empty Optional.
     *
     * @return An optional containing the next item or empty (to indicate nothing behind).
     */
    public Optional<T> reverse() {
        if (arrow.get() > 0) {
            return Optional.of(items.get(arrow.decrementAndGet()));
        }

        return Optional.empty();
    }

    /**
     * Moves the arrow a notch and returns the value (if possible),
     * otherwise will return an empty Optional.
     *
     * @return An optional containing the next item or empty (to indicate nothing next).
     */
    public Optional<T> next() {
        if (arrow.get() < items.size() - 1) {
            return Optional.of(items.get(arrow.incrementAndGet()));
        }

        return Optional.empty();
    }

    /**
     * Gets the current position of the arrow.
     *
     * @return The current position of the arrow.
     */
    public int getArrow() {
        return arrow.get();
    }

    /**
     * Moves the arrow to a certain new index, this is not
     * recommended to use unless you are certain that you are moving
     * above length zero and below item lengths.
     *
     * @param index The index to move the arrow to.
     */
    public void moveArrow(int index) {
        arrow.set(index);
    }

    /**
     * Gets the item inside the index specified.
     *
     * @param index The index to retrieve.
     * @return The item inside the index, could become null.
     */
    public T get(int index) {
        return items.get(index);
    }

    /**
     * This is used to check if there are no items.
     *
     * @return Is the Paginator empty?
     */
    public boolean isEmpty() {
        return items.isEmpty();
    }

    /**
     * This is used to get the size of the paginator, you can use
     * this in tandem with {@link Paginator#getArrow()} to create something like:
     * (1/2 pages).
     *
     * @return The size of the paginator.
     */
    public int size() {
        return items.size();
    }

}
