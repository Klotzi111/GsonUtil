package de.klotzi111.util.GsonUtil.reflection;

import java.io.IOException;
import java.lang.reflect.Field;

import com.google.gson.JsonElement;
import com.google.gson.TypeAdapter;
import com.google.gson.internal.Primitives;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class BoundField {

	public final Field field;
	public final TypeToken<?> fieldType;
	public final boolean isPrimitive;

	public final boolean jsonAdapterPresent;
	public final TypeAdapter<?> typeAdapter;
	public final TypeAdapter<?> serializeTypeAdapter;

	public final String serializationName;
	public final BoundField realField;
	public final boolean serialized;
	public final boolean deserialized;

	protected BoundField(Field field, TypeToken<?> fieldType, boolean jsonAdapterPresent, TypeAdapter<?> typeAdapter, TypeAdapter<?> serializeTypeAdapter, String serializationName, BoundField realField,
		boolean serialized, boolean deserialized) {
		this.field = field;
		this.fieldType = fieldType;
		isPrimitive = Primitives.isPrimitive(fieldType.getRawType());
		this.jsonAdapterPresent = jsonAdapterPresent;
		this.typeAdapter = typeAdapter;
		this.serializeTypeAdapter = serializeTypeAdapter;
		this.serializationName = serializationName;
		this.realField = realField;
		this.serialized = serialized;
		this.deserialized = deserialized;
	}

	public boolean isAlternativeNameField() {
		return realField != null;
	}

	public boolean writeField(Object value) throws IOException, IllegalAccessException {
		if (!serialized) {
			return false;
		}
		Object fieldValue = field.get(value);
		return fieldValue != value; // avoid recursion for example for Throwable.cause
	}

	public Object getValue(Object object) throws IllegalArgumentException, IllegalAccessException {
		return field.get(object);
	}

	@SuppressWarnings({"rawtypes", "unchecked"}) // somehow generics do not work here
	public void write(JsonWriter writer, Object object) throws IOException, IllegalAccessException {
		Object fieldValue = getValue(object);
		TypeAdapter t = serializeTypeAdapter;
		t.write(writer, fieldValue);
	}

	@SuppressWarnings({"rawtypes", "unchecked"}) // somehow generics do not work here
	public JsonElement toJsonTree(Object object) throws IllegalArgumentException, IllegalAccessException {
		Object fieldValue = getValue(object);
		TypeAdapter t = serializeTypeAdapter;
		return t.toJsonTree(fieldValue);
	}

	public void setValue(Object object, Object value) throws IllegalArgumentException, IllegalAccessException {
		if (value != null || !isPrimitive) {
			field.set(object, value);
		}
	}

	public void read(JsonReader reader, Object object) throws IOException, IllegalAccessException {
		Object fieldValue = typeAdapter.read(reader);
		setValue(object, fieldValue);
	}

	public void fromJsonTree(JsonElement jsonElement, Object object) throws IllegalArgumentException, IllegalAccessException {
		Object fieldValue = typeAdapter.fromJsonTree(jsonElement);
		setValue(object, fieldValue);
	}

}
