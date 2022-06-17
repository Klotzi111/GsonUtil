package de.klotzi111.util.GsonUtil.component;

public class MergeStrategy {

	public final boolean mergeArrays;
	public final boolean mergeObjects;

	public MergeStrategy(boolean mergeArrays, boolean mergeObjects) {
		this.mergeArrays = mergeArrays;
		this.mergeObjects = mergeObjects;
	}

	public boolean isRecursive() {
		return mergeArrays || mergeObjects;
	}

}
