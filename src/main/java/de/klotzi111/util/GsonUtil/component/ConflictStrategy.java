package de.klotzi111.util.GsonUtil.component;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import de.klotzi111.util.GsonUtil.exception.JsonObjectExtensionConflictException;

public enum ConflictStrategy {
	THROW_EXCEPTION,
	PREFER_FIRST_OBJ,
	PREFER_SECOND_OBJ,
	NON_NULL_PREFER_FIRST_OBJ,
	NON_NULL_PREFER_SECOND_OBJ;

	public void handleMergeConflict(String key, JsonObject leftObj, JsonElement leftVal, JsonElement rightVal)
		throws JsonObjectExtensionConflictException {
		switch (this) {
			case PREFER_FIRST_OBJ:
				break;// do nothing, the right val gets thrown out
			case PREFER_SECOND_OBJ:
				leftObj.add(key, rightVal);// right side auto-wins, replace left val with its val
				break;
			case NON_NULL_PREFER_FIRST_OBJ:
				// only use left val if it is not null
				if (leftVal.isJsonNull()) {
					leftObj.add(key, rightVal);
				} // else: left val not null use left side
				break;
			case NON_NULL_PREFER_SECOND_OBJ:
				// only use right val if it is not null
				if (!rightVal.isJsonNull()) {
					leftObj.add(key, rightVal);
				}
				break;
			case THROW_EXCEPTION:
				throw new JsonObjectExtensionConflictException(key, this);
			default:
				throw new UnsupportedOperationException("The conflict strategy " + this + " is unknown and cannot be processed");
		}
	}

}
