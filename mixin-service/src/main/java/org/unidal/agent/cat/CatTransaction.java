package org.unidal.agent.cat;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Documented
@Retention(RUNTIME)
@Target({ TYPE, METHOD, CONSTRUCTOR })
public @interface CatTransaction {
   String type() default "";

   String name() default "";

   String status() default "";

   String[] keys() default {};

   String[] values() default {};
}
