package com.klausboeing.jackson.annotation;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;

public interface PropertyTransformer<T> {

    T transform(JsonParser p, DeserializationContext ctxt, Class<T> type);

}
