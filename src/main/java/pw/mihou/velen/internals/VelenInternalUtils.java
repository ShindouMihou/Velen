package pw.mihou.velen.internals;

import pw.mihou.velen.utils.Pair;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class VelenInternalUtils {

    private final Locale locale;

    public VelenInternalUtils(Locale locale) {
        this.locale = locale;
    }

    /**
     * Fuzzy scoring algorithm.
     *
     * @param s1 The first string (a.k.a word).
     * @param s2 The second string (a.k.a query).
     * @return the score of s2.
     */
    public int score(String s1, String s2) {
        if (s1 == null || s2 == null)
            throw new IllegalArgumentException("Any of the arguments must not be null.");

        s1 = s1.toLowerCase(locale);
        s2 = s2.toLowerCase(locale);

        int score = 0;
        int lastMatch = Integer.MIN_VALUE;

        for (int i = 0; i < s2.length(); i++) {
            char q1 = s2.charAt(i);

            boolean matching = false;
            for (int index = 0; index < s1.length() && !matching; index++) {
                char q2 = s1.charAt(index);

                if (q1 == q2) {
                    score++;

                    if (lastMatch + 1 == index)
                        score += 2;

                    lastMatch = index;
                    matching = true;
                }
            }
        }
        return score;
    }

    /**
     * Finds the closest string among the list of string.
     * This uses Fuzzy scoring to score each one and returns back
     * the highest score.
     *
     * @param query        The query.
     * @param participants All the potential matches.
     * @return the highest scored participant.
     */
    public Pair<String, Integer> closest(String query, List<String> participants) {
        int iN = 0;
        int iS = 0;

        for (int i = 0; i < participants.size(); i++) {
            int s = score(participants.get(i), query);

            if (iS < s) {
                iN = i;
                iS = s;
            }

            if (iS == s) {
                if (participants.get(i).length() < participants.get(iN).length()) {
                    iN = i;
                    iS = s;
                }
            }

        }

        return Pair.of(participants.get(iN), iS);
    }

    /**
     * Finds the closest string among the list of string.
     * This uses Fuzzy scoring to score each one and returns back
     * the highest score.
     *
     * @param query        The query.
     * @param participants All the potential matches.
     * @return the highest scored participant.
     */
    public Pair<String, Integer> closest(String query, String... participants) {
        return closest(query, Arrays.asList(participants));
    }

}
