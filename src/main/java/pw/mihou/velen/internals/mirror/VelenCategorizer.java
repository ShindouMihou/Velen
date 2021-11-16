package pw.mihou.velen.internals.mirror;

import pw.mihou.velen.builders.VelenCategoryBuilder;
import pw.mihou.velen.impl.VelenImpl;
import pw.mihou.velen.interfaces.Velen;
import pw.mihou.velen.internals.mirror.extensions.VelenBaseParser;
import pw.mihou.velen.internals.routing.VelenUnderscoreParser;
import pw.mihou.velen.utils.Pair;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicReference;

public class VelenCategorizer extends VelenBaseParser {


    private final VelenImpl velen;

    /**
     * Creates a new Velen Categorizer to read categories out of
     * files.
     *
     * @param velen The velen instance.
     */
    public VelenCategorizer(Velen velen) {
        super("category");
        this.velen = (VelenImpl) velen;
    }

    public void comprehend(File file) {
        try (BufferedReader reader = Files.newBufferedReader(file.toPath())) {
            AtomicReference<VelenCategoryBuilder> builder = new AtomicReference<>(new VelenCategoryBuilder());

            reader.lines().forEach(s -> {
                s = s.trim();

                if (s.startsWith("&[") && s.contains("]: ")) {
                    Pair<Integer, Integer> positions = VelenUnderscoreParser.find("&[", ']', s);
                    String innerName = s.substring(positions.getLeft() + 2, positions.getRight()).trim();
                    String type = s.substring(positions.getRight()+2).trim();

                    if (type.endsWith("{"))
                        type = type.substring(0, type.length() - 1).trim();

                    if (type.equalsIgnoreCase("category")) {
                        builder.get().setName(innerName);
                    }
                }

                if (!s.startsWith("&[") && s.split("\\s+")[0].endsWith(":")) {
                    String innerName = s.split("\\s+")[0].replaceFirst(":", "").replaceFirst("\\{", "").trim();
                    String value = s.split("\\s+", 2)[1];
                    switch (innerName.toLowerCase()) {
                        case "middleware":
                            builder.get().addMiddleware(value);
                            break;
                        case "middlewares":
                            builder.get().addMiddleware(array(value));
                            break;
                        case "desc":
                            builder.get().setDescription(value);
                    }
                }

                if (s.trim().equalsIgnoreCase("}")) {
                    builder.get().create(velen);
                    builder.set(new VelenCategoryBuilder());
                }
            });
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read the following file: ["+file.getPath()+"].");
        }
    }

}
