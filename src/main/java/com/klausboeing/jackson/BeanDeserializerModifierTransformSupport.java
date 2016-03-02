package com.klausboeing.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.BeanDeserializerBuilder;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.klausboeing.jackson.annotation.JsonDeserializePropertyTransformer;
import com.klausboeing.jackson.annotation.PropertyTransformerProvider;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Iterator;

final class BeanDeserializerModifierTransformSupport extends BeanDeserializerModifier {

    @Override
    public BeanDeserializerBuilder updateBuilder(DeserializationConfig config, BeanDescription beanDesc, BeanDeserializerBuilder builder) {
        Class type;
        if (builder.getBuildMethod() != null) {
            type = builder.getBuildMethod().getRawReturnType();
        } else {
            type = beanDesc.getBeanClass();
        }
        Iterator<SettableBeanProperty> beanPropertyIterator = builder.getProperties();
        while (beanPropertyIterator.hasNext()) {
            final SettableBeanProperty settableBeanProperty = beanPropertyIterator.next();
            try {
                final Field declaredField = type.getDeclaredField(settableBeanProperty.getName());
                if (declaredField.isAnnotationPresent(JsonDeserializePropertyTransformer.class)) {
                    final JsonDeserializePropertyTransformer jsonPropertyTransformer = declaredField.getAnnotation(JsonDeserializePropertyTransformer.class);
                    final PropertyTransformerProvider propertyTransformerProvider;
                    try {
                        propertyTransformerProvider = jsonPropertyTransformer.value().newInstance();
                    } catch (InstantiationException | IllegalAccessException ex) {
                        throw new RuntimeException(ex);
                    }
                    builder.addOrReplaceProperty(settableBeanProperty.withValueDeserializer(new StdDeserializer<Object>(settableBeanProperty.getType()) {
                        @Override
                        public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
                            return propertyTransformerProvider.getTransformer().transform(p, ctxt, declaredField.getType());
                        }
                    }), true);
                }
            } catch (NoSuchFieldException | SecurityException ex) {
                throw new RuntimeException(ex);
            }
        }
        return builder;
    }

}
