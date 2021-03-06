package pw.mihou.velen.impl;

import pw.mihou.velen.interfaces.Velen;
import pw.mihou.velen.interfaces.VelenCategory;
import pw.mihou.velen.interfaces.VelenCommand;
import pw.mihou.velen.interfaces.afterware.VelenAfterware;
import pw.mihou.velen.interfaces.middleware.VelenMiddleware;

import java.util.List;
import java.util.stream.Collectors;

public class VelenCategoryImpl implements VelenCategory {

    private final String name;
    private final String description;
    private final List<VelenMiddleware> middlewares;
    private final List<VelenAfterware> afterwares;
    private final Velen velen;

    /**
     * Initiates a new Velen Category instance that holds all the required
     * information for a category. Please use Builder to create this instance.
     *
     * @param name The name of the category.
     * @param description The description of the category.
     * @param velen The velen instance to attach to.
     * @param middlewares The list of middlewares to use.
     * @param afterwares The list of afterwares to use.
     */
    public VelenCategoryImpl(String name, String description, List<String> middlewares, List<String> afterwares, Velen velen) {
        this.name = name;
        this.description = description;
        this.middlewares = middlewares.stream()
                .map(s -> velen.getMiddleware(s)
                        .orElseThrow(() -> new IllegalStateException("The middleware " + s + " is not found in the Velen instance.")))
                .collect(Collectors.toList());
        this.afterwares = afterwares.stream()
                .map(s -> velen.getAfterware(s)
                        .orElseThrow(() -> new IllegalStateException("The afterware " + s + " is not found in the Velen instance.")))
                .collect(Collectors.toList());
        this.velen = velen;
    }


    @Override
    public List<VelenMiddleware> getMiddlewares() {
        return middlewares;
    }

    @Override
    public List<VelenAfterware> getAfterwares() {
        return afterwares;
    }

    @Override
    public List<VelenCommand> getCommands() {
        return velen.getCategoryIgnoreCasing(getName());
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "VelenCategoryImpl{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", middleware_count=" + middlewares.size() +
                '}';
    }

}
