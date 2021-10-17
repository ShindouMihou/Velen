package pw.mihou.velen.internals.routing;

import pw.mihou.velen.utils.Pair;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class VelenUnderscoreParser {

    public static Map<Integer, String> route(String command, List<String> formats) {
        String[] commandIndexes = command.split("\\s+");
        if (commandIndexes.length == 1)
            return Collections.singletonMap(0, command);

        List<String> foo;

        // We want to see if there are any formats
        // that actually matches the length of arguments the user input has.
        List<String> x = formats.stream()
                .filter(s -> s.split("\\s+").length == commandIndexes.length)
                .collect(Collectors.toList());

        if (x.size() == 0) {
            foo = formats;
        } else {
            foo = x;
        }

        Map<Integer, String> finalMap = new HashMap<>();
        for (String format : foo) {
            Map<Integer, String> componentMap = new HashMap<>();

            if ((format.contains(":[") && format.contains("]"))) {
                String[] indexes = format.split("\\s+");

                String name;
                boolean isThisMrRight = true;
                for (int i1 = 0; i1 < indexes.length; i1++) {
                    String s = indexes[i1];

                    if (isThisMrRight) {
                        if (s.startsWith(":[")) {
                            // This is used to indicate if it passed all the regex and requirements.
                            boolean passes = true;

                            name = s.substring(s.lastIndexOf(":[") + 2, s.lastIndexOf("]"));

                            if ((s.contains("::(") || s.contains(":(")) && s.contains(")")) {
                                try {
                                    boolean ignoreCasing = name.contains("::(");

                                    String token = (ignoreCasing ? "::(" : ":(");
                                    String pattern = name.substring(name.lastIndexOf(token) + token.length(), name.lastIndexOf(")"));
                                    String[] values = pattern.split(",");

                                    String rawToken = name.substring(name.lastIndexOf(token), name.lastIndexOf(")") + 1);
                                    name = name.substring(0, name.lastIndexOf(rawToken));

                                    // Check if it matches with the required options.
                                    int finalI = i1;
                                    passes = Arrays.stream(values).anyMatch(s1 -> ignoreCasing ? commandIndexes[finalI].equalsIgnoreCase(s1) :
                                            commandIndexes[finalI].equals(s1));
                                } catch (IndexOutOfBoundsException e) {
                                    throw new IllegalStateException("An exception occurred while trying to parse command [" + command + "] with format [" + format + "]," +
                                            " please do not put spaces between the options: e.g. :[option::(no,spaces,please)]!");
                                }
                            }

                            // If it has the regex pattern.
                            if (s.contains(":{") && s.contains("}")) {
                                String regexPattern = name.substring(name.lastIndexOf(":{"), name.lastIndexOf("}") + 1);
                                Pattern pattern = Pattern.compile(name.substring(name.lastIndexOf(":{") + 2, name.lastIndexOf("}")));

                                // If the regex check is present then we can make name end with this.
                                name = name.substring(0, name.lastIndexOf(regexPattern));
                                if (!pattern.matcher(commandIndexes[i1]).matches()) {
                                    // Since the pattern doesn't match then we'll ignore this format.
                                    passes = false;
                                }
                            }

                            if (passes) {
                                componentMap.put(i1, name);
                            } else {
                                isThisMrRight = false;
                            }
                        } else {
                            // Check if this part is also the same, for example, if the format is
                            // quiz me :[difficulty:{easy|hard|medium}] :[topic]
                            // Ignore the command as well.
                            if (!s.equalsIgnoreCase(commandIndexes[i1]) && i1 != 0) {
                                isThisMrRight = false;
                            } else {
                                componentMap.put(i1, s);
                            }
                        }

                        if (i1 == indexes.length - 1 && isThisMrRight) {
                            finalMap = componentMap;
                        }
                    }
                }
            }
        }

        if (finalMap.isEmpty()) {
            for (int i = 0; i < commandIndexes.length; i++) {
                finalMap.put(i, null);
            }
        }
        return finalMap;
    }

}
