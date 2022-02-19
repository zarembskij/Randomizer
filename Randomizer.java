package com.zarembski.Randomizer;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Randomizer {

    private static final String REGEXP = "\\d{4}";
    private static final int FIRST_HOUR_IN_DAY = 0;
    private static final int LAST_HOUR_IN_DAY = 23;
    private static final int FIRST_MINUTES = 0;
    private static final int LAST_MINUTES = 59;

    public String randomize(String start, String end, Map<String, Integer> valuesMap) {
        if (!start.matches(REGEXP) || !end.matches(REGEXP)) {
            throw new RuntimeException(); //TODO RandomizaerException
        }
        Map<String, Integer> initialMap = prepareInitialMap(start, end);
        valuesMap.keySet().stream().filter(initialMap::containsKey).forEach(k -> initialMap.put(k, valuesMap.get(k)));

        String s = initialMap.entrySet().stream().min(Map.Entry.comparingByValue()).get().getKey();
        return s;
    }

    private Map<String, Integer> prepareInitialMap(String start, String end) {
        int startHour = Integer.parseInt(start.substring(0, 2));
        int startMinutes = Integer.parseInt(start.substring(2,4));

        int endHour = Integer.parseInt(end.substring(0, 2));
        int endMinutes = Integer.parseInt(end.substring(2, 4));

        Stream<String> firstBeforeMidnight = IntStream.of(startHour).boxed().flatMap(h -> decorateWithMinutesFirstHourBeforeMidnight(h, startMinutes));
        Stream<String> otherBeforeMidnight = IntStream.range(startHour + 1, LAST_HOUR_IN_DAY).boxed().flatMap(this::decorateWithMinutesOtherHours);
        Stream<String> otherAfterMidnight = IntStream.range(FIRST_HOUR_IN_DAY, endHour - 1).boxed().flatMap(this::decorateWithMinutesOtherHours);
        Stream<String> lastAfterMidnight = IntStream.of(endHour).boxed().flatMap(h -> decorateWithMinutesLastHourAfterMidnight(h, endMinutes));

        if (endHour < startHour) {
            return Stream.of(firstBeforeMidnight, otherBeforeMidnight, otherAfterMidnight, lastAfterMidnight)
                    .flatMap(s -> s).distinct().collect(Collectors.toMap(k -> k, k -> 0));
        } else if (endHour == startHour && endMinutes > startMinutes) {
            return IntStream.of(startHour).boxed().flatMap(h -> decorateWithMinutesWithEndHourEqualsStartHour(h, startMinutes, endMinutes))
                    .distinct().collect(Collectors.toMap(k -> k, k -> 0));
        } else {
            return Stream.of(firstBeforeMidnight, lastAfterMidnight)
                    .flatMap(s -> s).distinct().collect(Collectors.toMap(k -> k, k -> 0));
        }
    }

    private Stream<String> decorateWithMinutesFirstHourBeforeMidnight(int hour, int startMinutes) {
        return IntStream.rangeClosed(startMinutes, LAST_MINUTES).mapToObj(m -> String.format("%02d%02d", hour, m));
    }
    private Stream<String> decorateWithMinutesWithEndHourEqualsStartHour(int hour, int startMinutes, int endMinutes) {
        return IntStream.rangeClosed(startMinutes, endMinutes).mapToObj(m -> String.format("%02d%02d", hour, m));
    }
    private Stream<String> decorateWithMinutesOtherHours(int hour) {
        return IntStream.rangeClosed(FIRST_MINUTES, LAST_MINUTES).mapToObj(m -> String.format("%02d%02d", hour, m));
    }
    private Stream<String> decorateWithMinutesLastHourAfterMidnight(int hour, int lastMinutes) {
        return IntStream.rangeClosed(FIRST_MINUTES, lastMinutes).mapToObj(m -> String.format("%02d%02d", hour, m));
    }
}
