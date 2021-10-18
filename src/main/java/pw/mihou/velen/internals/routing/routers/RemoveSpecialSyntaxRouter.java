package pw.mihou.velen.internals.routing.routers;

import pw.mihou.velen.internals.routing.VelenUnderscoreRoute;
import pw.mihou.velen.utils.Pair;

import static pw.mihou.velen.internals.routing.VelenUnderscoreParser.collect;
import static pw.mihou.velen.internals.routing.VelenUnderscoreParser.findClosure;

/**
 * This is used to remove all special syntax routes
 * that are mainly used in slash commands generation.
 */
public class RemoveSpecialSyntaxRouter implements VelenUnderscoreRoute {
    @Override
    public Pair<Boolean, String> accept(String s, String name, int index, String[] commandIndexes,
                                        String messageArgument, String command, String format) {
        if (name.contains(":description(") && name.contains(")")) {
            name = name.replace(collect(":description(", ')', name), "");
        }

        if (name.contains(":optional()")) {
            name = name.replace(":optional\\(\\)", "");
        }

        if (name.contains(":required()")) {
            name = name.replace(":required\\(\\)", "");
        }

        if (name.contains("::withOptions(") && name.contains(")")) {
            name = name.substring(name.indexOf("::withOptions(") + "::withOptions(".length(),
                    findClosure(name.indexOf("::withOptions("), name, ')'));
        }

        return Pair.of(true, name);
    }
}
