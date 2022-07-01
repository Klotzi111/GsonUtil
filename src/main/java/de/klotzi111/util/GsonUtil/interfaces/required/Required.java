package de.klotzi111.util.GsonUtil.interfaces.required;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.google.gson.Gson;
import com.google.gson.TypeAdapterFactory;

import de.klotzi111.util.GsonUtil.interfaces.inlinefield.InlineFieldJsonObject;
import de.klotzi111.util.GsonUtil.interfaces.map.MapJsonObject;
import de.klotzi111.util.GsonUtil.typeadapters.RequiredTypeAdapterFactory;

/**
 * This annotation on a field makes the property non-optional in the json object when deserializing with {@link Gson}. The corresponding {@link TypeAdapterFactory} {@link RequiredTypeAdapterFactory} must be registered with gson.
 *
 * This annotation is ignored when the owning class of the field implements {@link MapJsonObject} or {@link InlineFieldJsonObject}.
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface Required {

}
