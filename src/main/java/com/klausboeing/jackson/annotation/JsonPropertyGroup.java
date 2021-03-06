package com.klausboeing.jackson.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = {ElementType.FIELD})
public @interface JsonPropertyGroup {

    String[] value() default {"default"};

    JsonUsePropertyGroup usePropertyGroup() default @JsonUsePropertyGroup("EMPTY_GROUP");
}
