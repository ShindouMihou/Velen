package pw.mihou.velen.builders;

import pw.mihou.velen.impl.VelenCategoryImpl;
import pw.mihou.velen.interfaces.Velen;
import pw.mihou.velen.interfaces.VelenCategory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VelenCategoryBuilder {

    private String name;
    private String description;
    private List<String> middlewares = new ArrayList<>();
    private List<String> afterwares = new ArrayList<>();

    public VelenCategoryBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public VelenCategoryBuilder setDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * Adds one or more middlewares to the list of middlewares to be used, ensure that
     * all the middlewares here are available in the {@link pw.mihou.velen.interfaces.Velen} instance.
     *
     * @param middlewares The middlewares to attach.
     * @return {@link VelenCategoryBuilder} for chain-calling methods.
     */
    public VelenCategoryBuilder addMiddleware(String... middlewares) {
        this.middlewares.addAll(Arrays.asList(middlewares));
        return this;
    }

    /**
     * Adds one or more afterwares to the list of afterwares to be used, ensure that
     * all the middlewares here are available in the {@link pw.mihou.velen.interfaces.Velen} instance.
     *
     * @param afterwares The afterwares to attach.
     * @return {@link VelenCategoryBuilder} for chain-calling methods.
     */
    public VelenCategoryBuilder addAfterware(String... afterwares) {
        this.afterwares.addAll(Arrays.asList(afterwares));
        return this;
    }

    /**
     * Creates a new category instance using the specified details written
     * in the following {@link VelenCategoryBuilder} instance.
     *
     * @param velen The {@link Velen} instance to store.
     * @return A new {@link VelenCategory} instance.
     */
    public VelenCategory create(Velen velen) {
        if (name == null || name.isEmpty())
            throw new IllegalArgumentException("You cannot create a category without a specified name.");

        if (velen == null)
            throw new IllegalArgumentException("You cannot create a category without a Velen instance.");

        return new VelenCategoryImpl(name, description, middlewares, afterwares, velen);
    }

}
