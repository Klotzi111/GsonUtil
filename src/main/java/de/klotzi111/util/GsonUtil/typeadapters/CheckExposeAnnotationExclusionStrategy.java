package de.klotzi111.util.GsonUtil.typeadapters;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

/**
 * This behaves similar to the excludeFieldsWithoutExposeAnnotation() method from the {@link GsonBuilder} but this does only exclude the field for serialization/deserialization if the corresponding property of the {@link Expose} annotation is false
 */
public class CheckExposeAnnotationExclusionStrategy implements ExclusionStrategy {

	private final boolean deserialize;

	public CheckExposeAnnotationExclusionStrategy(boolean deserialize) {
		this.deserialize = deserialize;
	}

	@Override
	public boolean shouldSkipClass(Class<?> clazz) {
		return false;
	}

	@Override
	public boolean shouldSkipField(FieldAttributes field) {
		return !(field.getAnnotation(Expose.class) == null || (deserialize ? field.getAnnotation(Expose.class).deserialize() : field.getAnnotation(Expose.class).serialize()));
	}

}
