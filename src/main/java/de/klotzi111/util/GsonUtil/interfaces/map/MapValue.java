package de.klotzi111.util.GsonUtil.interfaces.map;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * @see MapJsonObject
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface MapValue {

}
