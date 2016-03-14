package com.klausboeing.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerBuilder;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.klausboeing.jackson.annotation.JsonPropertyGroup;
import com.klausboeing.jackson.annotation.JsonUsePropertyGroup;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

final class BeanSerializerModifierGroupSupport extends BeanSerializerModifier {

    @Override
    public BeanSerializerBuilder updateBuilder(SerializationConfig config, BeanDescription beanDesc, BeanSerializerBuilder builder) {
        List<BeanPropertyWriter> properties = builder.getProperties().stream().map(BeanPropertyWriterGroupSupport::new).collect(Collectors.toList());
        builder.setProperties(properties);
        return builder;
    }

    private static final class BeanPropertyWriterGroupSupport extends BeanPropertyWriter {

        private final BeanPropertyWriter beanPropertyWriter;

        public BeanPropertyWriterGroupSupport(BeanPropertyWriter beanPropertyWriter) {
            super(beanPropertyWriter);
            this.beanPropertyWriter = beanPropertyWriter;
        }

        @Override
        public void serializeAsField(Object bean, JsonGenerator gen, SerializerProvider prov) throws Exception {
            if (!gen.getOutputContext().getParent().inRoot()) {
                JsonStreamContext parent = getParentObject(gen.getOutputContext());

                if (parent == null) {
                    super.serializeAsField(bean, gen, prov);
                    return;
                }

                String parentName = parent.getCurrentName();
                Object parentValue = parent.getCurrentValue();

                JsonPropertyGroup propertyGroup = beanPropertyWriter.getAnnotation(JsonPropertyGroup.class);
                JsonPropertyGroup propertyGroupParent = parentValue.getClass().getDeclaredField(parentName).getAnnotation(JsonPropertyGroup.class);
                JsonUsePropertyGroup usePropertyGroup = getResolvedUsePropertyGroup(parent, propertyGroupParent, parentValue, parentName);

                if (usePropertyGroup != null) {
                    if (!isPropertyGroupInUsePropertyGroup(usePropertyGroup, propertyGroup)) {
                        return;
                    }
                }
            }
            super.serializeAsField(bean, gen, prov);
        }

        private JsonUsePropertyGroup getResolvedUsePropertyGroup(JsonStreamContext parent, JsonPropertyGroup propertyGroupParent, Object parentValue, String currentName) throws NoSuchFieldException, SecurityException {
            JsonUsePropertyGroup jsonUsePropertyGroup;
            if (hasUsePropertyGroupIntoParentPropertyGroup(parent, propertyGroupParent)
                    && hasValidGroupToParentUsePropertyGroup(parent, propertyGroupParent)) {
                jsonUsePropertyGroup = propertyGroupParent.usePropertyGroup();
            } else {
                jsonUsePropertyGroup = parentValue.getClass().getDeclaredField(currentName).getAnnotation(JsonUsePropertyGroup.class);
            }
            return jsonUsePropertyGroup;
        }

        private boolean hasValidGroupToParentUsePropertyGroup(JsonStreamContext parent, JsonPropertyGroup propertyGroupParent) throws SecurityException, NoSuchFieldException {
            JsonStreamContext parentParent = getParentObject(parent);
            JsonUsePropertyGroup usePropertyGroupParentParent = parentParent.getCurrentValue().getClass().getDeclaredField(parentParent.getCurrentName()).getAnnotation(JsonUsePropertyGroup.class);

            if (usePropertyGroupParentParent == null) {
                return false;
            }

            return isPropertyGroupInUsePropertyGroup(usePropertyGroupParentParent, propertyGroupParent);
        }

        private boolean hasUsePropertyGroupIntoParentPropertyGroup(JsonStreamContext parent, JsonPropertyGroup propertyGroupParent) throws NoSuchFieldException {
            return propertyGroupParent != null && !propertyGroupParent.usePropertyGroup().value()[0].equals("EMPTY_GROUP") && !parent.getParent().inRoot();
        }

        private boolean isPropertyGroupInUsePropertyGroup(JsonUsePropertyGroup jsonUsePropertyGroup, JsonPropertyGroup propertyGroup) {
            if (propertyGroup == null) {
                return false;
            }

            List<String> usePropertyGroups = Arrays.asList(jsonUsePropertyGroup.value());
            List<String> propertyGroups = Arrays.asList(propertyGroup.value());

            return propertyGroups.stream().anyMatch(usePropertyGroups::contains);
        }

        private JsonStreamContext getParentObject(JsonStreamContext context) {
            JsonStreamContext parent = context.getParent();
            while (parent != null &&!parent.inObject()) {
                parent = parent.getParent();
            }

            return parent;
        }

    }

}
