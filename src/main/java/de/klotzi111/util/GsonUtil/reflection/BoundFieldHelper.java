package de.klotzi111.util.GsonUtil.reflection;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class BoundFieldHelper {

	public static Map<String, BoundField> getBoundFields(Gson gson, TypeToken<?> type) {
		GsonReflectiveTypeAccessor gra = GsonReflectiveTypeAccessor.fromGson(gson);
		return gra.getBoundFields(type, type.getRawType());
	}

	public static boolean isBoundFieldApplicable(BoundField field, boolean realFieldsOnly, boolean isSerialization) {
		if (realFieldsOnly && field.isAlternativeNameField()) {
			return false;
		}
		if (isSerialization) {
			if (!field.serialized) {
				return false;
			}
		} else {
			if (!field.deserialized) {
				return false;
			}
		}
		return true;
	}

	public static boolean checkOnlyValidFields(Map<String, BoundField> boundFields, boolean isSerialization, BoundFieldWithAnnotation<?>... fieldsWithAnnotation) {
		BoundField[] fields = new BoundField[fieldsWithAnnotation.length];
		for (int i = 0; i < fieldsWithAnnotation.length; i++) {
			BoundFieldWithAnnotation<?> fieldWithAnnotation = fieldsWithAnnotation[i];
			fields[i] = fieldWithAnnotation == null ? null : fieldWithAnnotation.boundField;
		}
		return checkOnlyValidFields(boundFields, isSerialization, fields);
	}

	public static boolean checkOnlyValidFields(Map<String, BoundField> boundFields, boolean isSerialization, BoundField... fields) {
		List<BoundField> checkFields = Arrays.asList(fields);
		if (checkFields.contains(null)) {
			return false;
		}
		for (BoundField field : boundFields.values()) {
			if (!isBoundFieldApplicable(field, true, isSerialization)) {
				continue;
			}
			if (checkFields.contains(field)) {
				continue;
			}
			return false;
		}
		return true;
	}

	public static <A extends Annotation> BoundFieldWithAnnotation<A> getSingleFieldWithAnnotation(Map<String, BoundField> boundFields, boolean isSerialization, Class<A> annotationClass) {
		List<BoundFieldWithAnnotation<A>> fields = getFieldsWithAnnotation(boundFields, isSerialization, annotationClass, 2);
		if (fields.size() != 1) {
			return null;
		}
		return fields.get(0);
	}

	public static <A extends Annotation> List<BoundFieldWithAnnotation<A>> getFieldsWithAnnotation(Map<String, BoundField> boundFields, boolean isSerialization, Class<A> annotationClass, int maxFields) {
		List<BoundFieldWithAnnotation<A>> resultFields = new ArrayList<>();
		if (maxFields <= 0) {
			return resultFields;
		}
		for (BoundField field : boundFields.values()) {
			if (!isBoundFieldApplicable(field, true, isSerialization)) {
				continue;
			}

			A annotation = field.field.getAnnotation(annotationClass);
			boolean isSearchedAnnotation = annotation != null;
			if (isSearchedAnnotation) {
				resultFields.add(new BoundFieldWithAnnotation<A>(field, annotation));
				if (resultFields.size() == maxFields) {
					break;
				}
			}
		}
		return resultFields;
	}

	public static <A extends Annotation> List<BoundFieldWithAnnotation<A>> getFieldsWithAnnotation(Map<String, BoundField> boundFields, boolean isSerialization, Class<A> annotationClass) {
		return getFieldsWithAnnotation(boundFields, isSerialization, annotationClass, Integer.MAX_VALUE);
	}

}
