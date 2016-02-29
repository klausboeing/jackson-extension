package com.klausboeing.jackson.annotation;

public interface PropertyTransformerProvider<T> {

    PropertyTransformer<T> getTransformer();

}
