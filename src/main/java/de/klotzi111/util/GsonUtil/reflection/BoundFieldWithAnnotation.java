package de.klotzi111.util.GsonUtil.reflection;

import java.lang.annotation.Annotation;

public class BoundFieldWithAnnotation<A extends Annotation> {
	public final BoundField boundField;
	public final A annotation;

	public BoundFieldWithAnnotation(BoundField boundField, A annotation) {
		this.boundField = boundField;
		this.annotation = annotation;
	}
}
