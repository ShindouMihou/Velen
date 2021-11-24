package pw.mihou.velen.internals.routing;

import pw.mihou.velen.internals.routing.routers.OfTypeRouter;
import pw.mihou.velen.internals.routing.routers.RemoveSpecialSyntaxRouter;
import pw.mihou.velen.utils.Pair;
import pw.mihou.velen.utils.VelenUtils;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class VelenUnderscoreParser {

    private static final List<VelenUnderscoreRoute> routers = new ArrayList<>();

    static {
        // All the :of(type) options.
        routers.add(new OfTypeRouter());
        routers.add(new RemoveSpecialSyntaxRouter());
    }

    public static Map<Integer, Pair<String, String>> route(String command, List<String> formats) {
        String[] commandIndexes = Stream.concat(Arrays.stream(new String[]{command.split(" ")[0]}), Arrays.stream(VelenUtils.splitContent(command))
                        .filter(s -> !s.equals(command.split(" ")[0])))
                .toArray(String[]::new);
        if (commandIndexes.length == 1)
            return Collections.singletonMap(0, generatePairValue(command, null));

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

        Map<Integer, Pair<String, String>> finalMap = new HashMap<>();
        // This is used to allow us to send the name of options that passed.
        Map<String, Map<Integer, Pair<String, String>>> formatMaps = new HashMap<>();

        for (String format : foo) {
            Map<Integer, Pair<String, String>> componentMap = new HashMap<>();

            if ((format.contains(":[") && format.contains("]"))) {
                String[] indexes = format.split("\\s+");

                String name;
                boolean isThisMrRight = true;
                for (int i1 = 0; i1 < indexes.length; i1++) {
                    String s = indexes[i1];
                    // This is used to store the value of the option.
                    String value = null;

                    if (isThisMrRight) {
                        if (s.startsWith(":[")) {
                            // This is used to indicate if it passed all the regex and requirements.
                            boolean passes = true;

                            name = s.substring(s.lastIndexOf(":[") + 2, s.lastIndexOf("]"));

                            while ((name.contains("::(") || name.contains(":(")) && name.contains(")")) {
                                try {
                                    boolean ignoreCasing = name.contains("::(");

                                    String token = (ignoreCasing ? "::(" : ":(");
                                    String pattern = name.substring(name.indexOf(token) + token.length(), findClosure(name.indexOf(token), name, ')'));
                                    String[] values = pattern.split(",", 2);

                                    name = name.replace(collect(token, ')', name), "");
                                    value = values[1];

                                    // Check if it matches with the required options.
                                    int finalI = i1;
                                    passes = Arrays.stream(values).anyMatch(s1 -> ignoreCasing ? commandIndexes[finalI].equalsIgnoreCase(s1) :
                                            commandIndexes[finalI].equals(s1));
                                } catch (IndexOutOfBoundsException e) {
                                    throw new IllegalStateException("An exception occurred while trying to parse command [" + command + "] with format [" + format + "]," +
                                            " please do not put spaces between the options: e.g. :[option::(no,spaces)]!");
                                }
                            }

                            // If it has the regex pattern.
                            while (name.contains(":{") && name.contains("}")) {
                                Pattern pattern = Pattern.compile(name.substring(name.indexOf(":{") + 2, findClosure(name.indexOf(":{"), name, '}')));

                                // If the regex check is present then we can make name end with this.
                                name = name.replace(collect(":{", '}', name), "");
                                if (!pattern.matcher(commandIndexes[i1]).matches()) {
                                    // Since the pattern doesn't match then we'll ignore this format.
                                    passes = false;
                                }
                            }

                            // Now it is easier for us to handle stuff.
                            for (VelenUnderscoreRoute router : routers) {
                                Pair<Boolean, String> v = router.accept(s, name, i1, commandIndexes,commandIndexes[i1], command, format);

                                if (!v.getLeft()) {
                                    passes = false;
                                } else {
                                    name = v.getRight();
                                }
                            }

                            if (passes) {
                                if (i1 == indexes.length - 1) {
                                    // We want to integrate the extra parts onto the command.
                                    // if the last index of the format has the symbol [+]
                                    if (commandIndexes.length > indexes.length) {
                                        if (indexes[indexes.length - 1].contains(":hasMany()")) {
                                            name = name.replace(":hasMany()", "");
                                            for (int i = i1; i < commandIndexes.length; i++) {
                                                componentMap.put(i, generatePairValue(name, value));
                                            }
                                        }
                                    } else {
                                        // We still have to remove it here again...
                                        if (indexes[indexes.length - 1].contains(":hasMany()")) {
                                            name = name.replace(":hasMany()", "");
                                        }
                                    }
                                }
                                componentMap.put(i1, generatePairValue(name, value));
                            } else {
                                componentMap.put(i1, null);
                                isThisMrRight = false;
                            }
                        } else {
                            // Check if this part is also the same, for example, if the format is
                            // quiz me :[difficulty:{easy|hard|medium}] :[topic]
                            // Ignore the command as well.
                            if (!s.equalsIgnoreCase(commandIndexes[i1]) && i1 != 0) {
                                isThisMrRight = false;
                            } else {
                                componentMap.put(i1, generatePairValue("_commandName", null));
                            }
                        }


                        if (i1 == indexes.length - 1) {
                            if (isThisMrRight)
                                finalMap = componentMap;
                            else
                                formatMaps.put(format, componentMap);
                        }
                    }
                }
            }
        }

        if (finalMap.isEmpty()) {
            // We want to return the format that actually got the largest amount
            // of arguments identified while leaving on those that failed to pass with null.
            Optional<Map<Integer, Pair<String, String>>> largestMap = formatMaps.values()
                    .stream().max(Comparator.comparingInt(Map::size));

            if (largestMap.isPresent()) {
                finalMap = largestMap.get();
            } else {
                for (int i = 0; i < commandIndexes.length; i++) {
                    finalMap.put(i, null);
                }
            }
        }

        return finalMap;
    }

    private static Pair<String, String> generatePairValue(String name, String value) {
        return Pair.of(name, value);
    }

    public static boolean hasParameterType(String source, String type) {
        return source.contains(":of("+type+")");
    }

    public static String cleanseParameterType(String source, String type) {
        return cleanse(source, ":of\\("+type+"\\)");
    }

    public static String cleanse(String source, String outline) {
        return source.replaceFirst(outline, "");
    }

    public static Pair<Integer, Integer> find(String opening, char closure, String source) {
        return Pair.of(source.indexOf(opening), findClosure(source.indexOf(opening), source, closure));
    }

    public static String collect(String opening, char closure, String source) {
        return source.substring(source.indexOf(opening), findClosure(source.indexOf(opening), source, closure) + 1);
    }

    public static int findClosure(int startingIndex, String source, char closure) {
        int pos = startingIndex+1;

        char[] ch = source.toCharArray();

        while (pos < ch.length - 1) {
            pos++;
            if (ch[pos] == closure) {
                return pos;
            }
        }

        // Let's throw an exception if the format is not correct, missing closure.
        throw new IllegalStateException("The format ["+source+"] is unaccepted, missing closure for character position: " + startingIndex);
    }

}
