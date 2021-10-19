package pw.mihou.velen.internals.routing;

public class VelenRoutedArgument {

    private final int index;
    private final String name;
    private final String value;

    /**
     * Creates a new routed argument that can be used to
     * identify arguments of a message command.
     *
     * @param index The index to map with the name.
     * @param name The name to map with the index.
     * @param value The value to map with the two.
     */
    public VelenRoutedArgument(int index, String name, String value) {
        this.index = index;
        this.name = name;
        this.value = value;
    }

    /**
     * Gets the index of this argument.
     *
     * @return The index.
     */
    public int getIndex() {
        return index;
    }

    /**
     * Gets the name of this argument.
     *
     * @return The name of the argument.
     */
    public String getName() {
        return name;
    }

    /**
     * Retrieves the value of this argument.
     *
     * @return The value of this argument.
     */
    public String getValue() {
        return value;
    }
}
