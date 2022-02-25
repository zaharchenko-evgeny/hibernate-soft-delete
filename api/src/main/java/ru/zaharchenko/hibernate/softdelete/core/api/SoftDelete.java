package ru.zaharchenko.hibernate.softdelete.core.api;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation to mark entity as soft deleted
 *
 * @author Evgeny Zakharchenko
 */
@Target({TYPE})
@Retention(RUNTIME)
public @interface SoftDelete {

    String column() default "";

    String property() default "";

    Class type() default Boolean.class;
}
