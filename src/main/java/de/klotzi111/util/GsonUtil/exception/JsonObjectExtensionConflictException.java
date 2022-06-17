package de.klotzi111.util.GsonUtil.exception;

import de.klotzi111.util.GsonUtil.component.ConflictStrategy;

public class JsonObjectExtensionConflictException extends Exception {
	private static final long serialVersionUID = -3016577404714535893L;

	private static final String MESSAGE_FORMAT = "Key \"%s\" exists in both objects and the conflict resolution strategy is \"%s\"";

	public final String key;
	public final ConflictStrategy conflictStrategy;

	public JsonObjectExtensionConflictException(String key, ConflictStrategy conflictStrategy) {
		super(String.format(MESSAGE_FORMAT, key, conflictStrategy));
		this.key = key;
		this.conflictStrategy = conflictStrategy;
	}
}
