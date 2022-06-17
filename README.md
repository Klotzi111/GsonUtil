# GsonUtil
Helper library for working with Gson and Java objects.

## Usage
You can get it via jitpack or compile it yourself.

### Jitpack
Add the following in your build.gradle:

```groovy
repositories {
	maven {
		url "https://jitpack.io"
	}
}

dependencies {
	implementation 'com.github.Klotzi111:GsonUtil:main-SNAPSHOT'
}
```

### Compile self
Do the following to use this library:
 - Download, build and publish this library to your local maven repository (use the gradle task `publishToMavenLocal` for that)
 - Add the following in your build.gradle:
 
```groovy
repositories {
	mavenLocal()
}

dependencies {
	implementation 'de.klotzi111.util:GsonUtil:1+'
}
```

## TypeAdapters
This library comes with TypeAdapters for useful/common situations when de-/serializing custom java objects from/to json.

### SerializationUpdaterTypeAdapterFactory
This type adapter allows you to do custom actions and validation before (pre) object serialization and after (post) de-/serialization.

There are these two interfaces:
- `PreSerializationUpdater`
- `PostDeSerializationUpdater`

Example for `PostDeSerializationUpdater`:

```java
// This example class validates its fields after deserialization and throws an exception if the name is null (explicitly or because of default init)
public class MyObject implements PostDeSerializationUpdater {

	public final String name;
	public final int age;

	// constructor...

	@Override
	public void postSerializationUpdate(boolean deserialize) throws Exception {
		if (deserialize) {
			if (name == null) {
				throw new IllegalArgumentException("name");
			}
		}
	}

}
```

### InlineFieldJsonObjectTypeAdapterFactory
This type adapter inlines field values annotated with `@InlineField` within class that implement `InlineFieldJsonObject`.
The field that should be inlined can have any type. It can also have the `@JsonAdapter`, `@SerializedName` or any other Gson annotation.

Example:
```java
public class MyObject implements InlineFieldJsonObject {
	@InlineField
	public final String value;

	// constructor...

}
```
Serializing an instance of this class like `new MyObject("Test")` will result in `"Test"` and **NOT** `{"value":"Test"}` as it normally would.

### MapJsonObjectTypeAdapterFactory
This type adapter serializes a collection of classes that implement `MapJsonObject` as a map (like normal Gson would serialize a map: to an json array if `enableComplexMapKeySerialization` is enabled and the map contains complex keys or to an json object otherwise).
The class implementing `MapJsonObject` must have two fields. One annotated with `@MapKey` and one annotated with `@MapValue`.
The fields can have any type. They can also have the `@JsonAdapter`, `@SerializedName` or any other Gson annotation.

Example:
```java
public class MyObject implements MapJsonObject {
	@MapKey
	public String key;
	@MapValue
	public String value;

	// constructor...

}
```
Serializing an collection with instances of this class like `Arrays.asList(new MyObject("Test", "Data"))` will result in `{"Test":"Data"}` and **NOT** `[{"key":"Test","value":"Data"}]` as it normally would.

## ExclusionStrategy

### CheckExposeAnnotationExclusionStrategy
This exclusion strategy can be used to disable serialization and/or deserialization for any annotated field.

Add it to your `GsonBuilder` like so:
```java
gsonBuilder.addSerializationExclusionStrategy(new CheckExposeAnnotationExclusionStrategy(false))
	.addDeserializationExclusionStrategy(new CheckExposeAnnotationExclusionStrategy(true));
```

Example:
```java
public class MyObject {
	public String always;
	@Expose(serialize = false)
	public String noSerialize;
	@Expose(deserialize = false)
	public String noDeserialize;

	// constructor...

}
```
Serializing an instance of this class like `new MyObject("A", "S", "D")` will result in `{"always":"A","noDeserialize":"D"}`.
Deserializing a json string like `{"always":"A","noSerialize":"S","noDeserialize":"D"}` will result in an instance like `new MyObject("A", "S", null)`.

## Additional examples
Take a look at the test classes to find out more about how to use this library's classes.


