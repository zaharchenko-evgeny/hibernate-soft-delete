package ru.zaharchenko.hibernate.softdelete.core.api;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation to mark softy deleted column
 *
 * @author Evgeny Zakharchenko
 */
@Target({METHOD, FIELD})
@Retention(RUNTIME)
public @interface SoftDeleteColumn {
}
