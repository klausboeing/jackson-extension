package com.klausboeing.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.BeanDeserializerBuilder;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.klausboeing.jackson.annotation.JsonPropertyTransformer;
import com.klausboeing.jackson.annotation.JsonUsePropertyGroup;
import com.klausboeing.jackson.annotation.PropertyTransformerProvider;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class JacksonExtensionModule extends SimpleModule {

    @Override
    public void setupModule(Module.SetupContext context) {

        context.addBeanSerializerModifier(new BeanSerializerModifier() {

            @Override
            public List<BeanPropertyWriter> changeProperties(SerializationConfig config, BeanDescription beanDesc, final List<BeanPropertyWriter> beanProperties) {
                List<BeanPropertyWriter> list = beanProperties
                        .stream()
                        .filter(b -> b.getAnnotation(JsonUsePropertyGroup.class) != null)
                        .collect(Collectors.toList());

                beanProperties.removeAll(list);

                list.forEach(b -> beanProperties.add(new BeanProperty(b)));

                return beanProperties;
            }

        });

        context.addBeanDeserializerModifier(new MyBeanDeserializerModifier());

    }

    public static class MyBeanDeserializerModifier extends BeanDeserializerModifier {

        Class type;

        @Override
        public BeanDeserializerBuilder updateBuilder(DeserializationConfig config, BeanDescription beanDesc, BeanDeserializerBuilder builder) {
            Iterator<SettableBeanProperty> beanPropertyIterator = builder.getProperties();

            if (builder.getBuildMethod() != null) {
                type = builder.getBuildMethod().getRawReturnType();
            } else {
                type = beanDesc.getBeanClass();
            }

            while (beanPropertyIterator.hasNext()) {
                final SettableBeanProperty settableBeanProperty = beanPropertyIterator.next();

                try {
                    final Field declaredField = type.getDeclaredField(settableBeanProperty.getName());

                    if (declaredField.isAnnotationPresent(JsonPropertyTransformer.class)) {
                        final JsonPropertyTransformer jsonPropertyTransformer = declaredField.getAnnotation(JsonPropertyTransformer.class);
                        final PropertyTransformerProvider propertyTransformerProvider;
                        try {
                            propertyTransformerProvider = jsonPropertyTransformer.provider().newInstance();
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

    public static final class BeanProperty extends BeanPropertyWriter {

        private final BeanPropertyWriter writer;

        public BeanProperty(BeanPropertyWriter w) {
            super(w);
            writer = w;
        }

        @Override
        public void serializeAsField(Object bean, JsonGenerator gen, SerializerProvider prov) throws Exception {

            if (writer.get(bean) == null) {
                super.serializeAsField(bean, gen, prov);
                return;
            }

            gen.writeObjectField(writer.getName(), MapEntityIdBuilder.build(writer.get(bean), Arrays.asList(writer.getAnnotation(JsonUsePropertyGroup.class).value())));

        }

    }
}
