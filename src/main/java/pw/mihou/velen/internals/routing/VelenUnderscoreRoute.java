package pw.mihou.velen.internals.routing;

import pw.mihou.velen.utils.Pair;

public interface VelenUnderscoreRoute {

    /**
     * Choose whether to accept this command, or not.
     *
     * @param s The unchanged index value.
     * @param name The index name.
     * @param index The index position.
     * @param commandIndexes All the indexes of the message content.
     * @param messageArgument The index value message content of the current index.
     * @return Whether to accept this or not, also the new name of the index.
     */
    Pair<Boolean, String> accept(String s, String name, int index, String[] commandIndexes, String messageArgument, String command,
                                 String format);

}
