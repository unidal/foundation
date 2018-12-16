package org.unidal.agent.mixin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MixinMeta {
   public Class<?> targetClass() default Object.class;

   public String targetClassName() default "";
}