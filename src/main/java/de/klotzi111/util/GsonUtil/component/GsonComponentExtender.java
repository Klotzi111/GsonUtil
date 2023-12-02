package de.klotzi111.util.GsonUtil.component;

import java.util.Map;
import java.util.Stack;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import de.klotzi111.util.GsonUtil.exception.JsonObjectExtensionConflictException;

public class GsonComponentExtender {

	private GsonComponentExtender() {
	}

	private static class ExtendJsonObjectStackObject {
		public final JsonObject leftObj;
		public final JsonObject rightObj;

		public ExtendJsonObjectStackObject(JsonObject leftObj, JsonObject rightObj) {
			this.leftObj = leftObj;
			this.rightObj = rightObj;
		}

	}

	/**
	 * This method extends the {@code destinationObject} with the values in the given {@code objects} using the provided {@link MergeStrategy} and {@link ConflictStrategy}.
	 * <p>
	 * Note: This method breaks when the same instance of a {@link JsonObject} or {@link JsonArray} (a == b) is contained multiple times in any of the given json objects and across.
	 *
	 * @param destinationObject
	 * @param mergeStrategy
	 * @param conflictStrategy
	 * @param objects
	 * @throws JsonObjectExtensionConflictException
	 */
	public static void extendJsonObject(JsonObject destinationObject, MergeStrategy mergeStrategy, ConflictStrategy conflictStrategy, JsonObject... objects)
		throws JsonObjectExtensionConflictException {
		for (JsonObject obj : objects) {
			extendJsonObject(destinationObject, obj, mergeStrategy, conflictStrategy);
		}
	}

	private static void extendJsonObjectInner(JsonObject leftObj, JsonObject rightObj, MergeStrategy mergeStrategy, ConflictStrategy conflictStrategy, Stack<ExtendJsonObjectStackObject> stack)
		throws JsonObjectExtensionConflictException {
		for (Map.Entry<String, JsonElement> rightEntry : rightObj.entrySet()) {
			String rightKey = rightEntry.getKey();
			JsonElement rightVal = rightEntry.getValue();
			if (leftObj.has(rightKey)) {
				// conflict
				JsonElement leftVal = leftObj.get(rightKey);
				if (mergeStrategy.isRecursive()) {
					if (leftVal.isJsonArray() && rightVal.isJsonArray() && mergeStrategy.mergeArrays) {
						JsonArray leftArr = leftVal.getAsJsonArray();
						JsonArray rightArr = rightVal.getAsJsonArray();
						// concat the arrays -- there cannot be a conflict in an array, it's just a collection of stuff with duplicates allowed
						leftArr.addAll(rightArr);
						continue;
					} else if (leftVal.isJsonObject() && rightVal.isJsonObject() && mergeStrategy.mergeObjects) {
						// recursive merging
						stack.push(new ExtendJsonObjectStackObject(leftVal.getAsJsonObject(), rightVal.getAsJsonObject()));
						continue;
					}
				}
				// not both are arrays or objects, normal merge with conflict resolution
				conflictStrategy.handleMergeConflict(rightKey, leftObj, leftVal, rightVal);
			} else {
				// no conflict, add to the object
				leftObj.add(rightKey, rightVal);
			}
		}
	}

	private static void extendJsonObject(JsonObject leftObj, JsonObject rightObj, MergeStrategy mergeStrategy, ConflictStrategy conflictStrategy)
		throws JsonObjectExtensionConflictException {

		if (!mergeStrategy.isRecursive()) {
			extendJsonObjectInner(leftObj, rightObj, mergeStrategy, conflictStrategy, null);
			return;
		}

		Stack<ExtendJsonObjectStackObject> stack = new Stack<ExtendJsonObjectStackObject>();
		stack.push(new ExtendJsonObjectStackObject(leftObj, rightObj));

		while (!stack.isEmpty()) {
			ExtendJsonObjectStackObject working = stack.pop();
			extendJsonObjectInner(working.leftObj, working.rightObj, mergeStrategy, conflictStrategy, stack);
		}
	}

}
