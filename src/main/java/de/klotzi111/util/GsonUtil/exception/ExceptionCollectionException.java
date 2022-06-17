package de.klotzi111.util.GsonUtil.exception;

import java.util.Collections;
import java.util.List;

public class ExceptionCollectionException extends Exception {
	private static final long serialVersionUID = 3408461165338124435L;

	private static final String MESSAGE_FORMAT = "%d Exceptions";

	private final List<Throwable> exceptions;

	public ExceptionCollectionException(List<Throwable> exceptions) {
		this.exceptions = exceptions;
		for (Throwable throwable : exceptions) {
			addSuppressed(throwable);
		}
	}

	@Override
	public String getMessage() {
		return String.format(MESSAGE_FORMAT, exceptions.size());
	}

	public List<Throwable> getExceptions() {
		return Collections.unmodifiableList(exceptions);
	}
}
