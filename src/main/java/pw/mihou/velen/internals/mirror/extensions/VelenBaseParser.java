package pw.mihou.velen.internals.mirror.extensions;

import pw.mihou.velen.utils.Pair;

import java.io.File;
import java.util.Arrays;

public class VelenBaseParser {

    private final String type;

    public VelenBaseParser(String type) {
        this.type = type;
    }

    /**
     * Creates a new illegal state argument exception
     * that follows the readable format of Velen.
     *
     * @param file The file that caused the issue.
     * @param name The name of what is being parsed.
     * @param error The error.
     * @return A new illegal state exception.
     */
    public IllegalStateException error(File file, String name, String error) {
        throw new IllegalStateException("Failed to create "+type+" from the following file: ["+file.getPath()+"] with name " +
                "["+name+"]: \n" + error + ", please read the wiki for more information.");
    }

    public String[] array(String value) {
        String[] arr;
        if (value.startsWith("[") && value.endsWith("]"))
            arr = value.substring(1, value.length() - 1).split(",");
        else
            arr = value.split(",");

        arr = Arrays.stream(arr).map(String::trim).toArray(String[]::new);
        return arr;
    }

    public Pair<String, String> arrayOfTwo(String value) {
        String[] array = array(value);

        return Pair.of(array[0], array.length > 1 ? array[1] : null);
    }

}
