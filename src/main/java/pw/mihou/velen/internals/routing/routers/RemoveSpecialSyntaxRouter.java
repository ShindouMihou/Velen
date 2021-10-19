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
        if (name.contains(":required()")) {
            name = name.replace(":required()", "");
        }

        return Pair.of(true, name);
    }
}
