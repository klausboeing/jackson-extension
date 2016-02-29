package com.klausboeing.jackson;

import com.klausboeing.jackson.annotation.JsonPropertyGroup;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MapEntityIdBuilder {

    public static Object build(final Object bean, final List<String> properties) {
        if (Collection.class.isAssignableFrom(bean.getClass())) {
            return Collection.class.cast(bean).
                    stream()
                    .map(b -> toMap(b, properties))
                    .collect(Collectors.toList());
        } else {
            return toMap(bean, properties);
        }
    }

    public static Map toMap(final Object bean, List<String> properties) {
        Map map = new HashMap();

        Arrays.stream(bean.getClass().getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(JsonPropertyGroup.class))
                .filter(f -> Arrays.stream(f.getAnnotation(JsonPropertyGroup.class).value()).anyMatch(properties::contains))
                .forEach(f -> {
                    try {
                        f.setAccessible(true);
                        map.put(f.getName(), f.get(bean));
                    } catch (IllegalArgumentException | IllegalAccessException ex) {
                        throw new RuntimeException(ex);
                    }
                }
                );

        return map;

    }

}
