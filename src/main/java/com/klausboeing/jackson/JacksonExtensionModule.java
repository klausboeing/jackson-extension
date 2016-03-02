package com.klausboeing.jackson;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class JacksonExtensionModule extends SimpleModule {

    @Override
    public void setupModule(Module.SetupContext context) {
        context.addBeanSerializerModifier(new BeanSerializerModifierGroupSupport());
        context.addBeanDeserializerModifier(new BeanDeserializerModifierTransformSupport());
    }

}
