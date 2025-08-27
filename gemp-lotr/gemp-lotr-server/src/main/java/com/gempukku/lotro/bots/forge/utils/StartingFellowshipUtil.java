package com.gempukku.lotro.bots.forge.utils;

import com.gempukku.lotro.logic.vo.LotroDeck;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StartingFellowshipUtil {
    public static List<String> getStartingFellowship(LotroDeck deck) {
        // Gandalf Starter
        if (isGandalfFotrStarter(deck)) {
            return List.of("1_364");
        }
        // Aragorn Starter
        if (isAragornFotrStarter(deck)) {
            return List.of("1_365");
        }

        // TODO expand

        throw new UnsupportedOperationException("Cannot decide on starting fellowship for deck: " + deck.getDeckName());
    }

    private static boolean isAragornFotrStarter(LotroDeck deck) {
        List<String> drawDeck = new ArrayList<>(deck.getDrawDeckCards());
        List<String> needed = List.of(
                "1_365", "1_365", "1_92", "1_94", "1_94", "1_97", "1_97", "1_101", "1_104", "1_104",
                "1_106", "1_107", "1_107", "1_296", "1_296", "1_299", "1_299", "1_51", "1_108", "1_108",
                "1_110", "1_110", "1_309", "1_311", "1_116", "1_116", "1_116", "1_117", "1_117", "1_117"
        );
        return containsAllWithCounts(drawDeck, needed);
    }

    private static boolean isGandalfFotrStarter(LotroDeck deck) {
        List<String> drawDeck = new ArrayList<>(deck.getDrawDeckCards());
        List<String> needed = List.of(
                "1_70", "1_97", "1_286", "1_286", "1_286", "1_37", "1_37", "1_364", "1_364", "1_86",
                "1_12", "1_12", "1_296", "1_296", "1_299", "1_76", "1_76", "1_76", "1_51", "1_51",
                "1_78", "1_78", "1_78", "1_78", "1_304", "1_304", "1_84", "1_312", "1_26", "1_26");
        return containsAllWithCounts(drawDeck, needed);
    }

    private static boolean containsAllWithCounts(List<String> listA, List<String> listB) {
        // Count occurrences in both lists
        Map<String, Long> countsA = listA.stream()
                .collect(Collectors.groupingBy(s -> s, Collectors.counting()));

        Map<String, Long> countsB = listB.stream()
                .collect(Collectors.groupingBy(s -> s, Collectors.counting()));

        // Check if A has at least as many of each element as B
        for (Map.Entry<String, Long> entry : countsB.entrySet()) {
            long countInA = countsA.getOrDefault(entry.getKey(), 0L);
            if (countInA < entry.getValue()) {
                return false;
            }
        }
        return true;
    }
}
