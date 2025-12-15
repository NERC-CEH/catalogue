package uk.ac.ceh.gateway.catalogue.util;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class CollectionFilter {

    /**
     * Filters a list of items based on a property value
     *
     * @param <T> The type of items in the collection
     * @param <V> The type of the property to filter on
     * @param items The collection to filter (can be null)
     * @param propertyExtractor Function to extract the property value from an item
     * @param expectedValue The value to match against
     * @param exclude If true, excludes items that match the expected value
     * @return A new list containing the filtered items, or empty list if input is null
     */
    public static <T, V> List<T> filterByProperty(
        List<T> items,
        Function<T, V> propertyExtractor,
        V expectedValue,
        boolean exclude) {

        return filterByPredicate(items,
            item -> exclude ^ Objects.equals(propertyExtractor.apply(item), expectedValue));
    }

    /**
     * Generic filter using a predicate
     */
    public static <T> List<T> filterByPredicate(
        List<T> items,
        Predicate<T> predicate) {

        if (items == null) {
            return List.of();
        }
        return items.stream()
            .filter(predicate)
            .collect(Collectors.toList());
    }

    /**
     * Filters a list of items where the specified property matches the given regex pattern
     *
     * @param <T> The type of items in the collection
     * @param <V> The type of the property to filter on
     * @param items The collection to filter (can be null)
     * @param propertyExtractor Function to extract the property value from an item
     * @param regex The regex pattern to match against the property value
     * @param exclude If true, excludes items that match the regex pattern
     * @return A new list containing the filtered items, or empty list if input is null
     */
    public static <T, V> List<T> filterByPropertyRegex(
        List<T> items,
        Function<T, V> propertyExtractor,
        String regex,
        boolean exclude) {

        return filterByPredicate(items,
            item -> {
                V value = propertyExtractor.apply(item);
                boolean matches = value != null && value.toString().matches(regex);
                return exclude ^ matches;
            });
    }
}
