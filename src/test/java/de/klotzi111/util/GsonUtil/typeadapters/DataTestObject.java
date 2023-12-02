package de.klotzi111.util.GsonUtil.typeadapters;

import java.util.Objects;

public class DataTestObject {

	public String name;
	public int data;

	public DataTestObject(String name, int data) {
		this.name = name;
		this.data = data;
	}

	@Override
	public int hashCode() {
		return Objects.hash(data, name);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof DataTestObject)) {
			return false;
		}
		DataTestObject other = (DataTestObject) obj;
		return data == other.data && Objects.equals(name, other.name);
	}

	@Override
	public String toString() {
		return name + "=" + data;
	}

}
