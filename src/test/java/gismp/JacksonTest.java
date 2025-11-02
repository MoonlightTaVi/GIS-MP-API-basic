package gismp;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import tools.jackson.databind.ObjectMapper;

/**
 * This tests make sure Jackson serialization/deserialization
 * work properly.
 */
public class JacksonTest {
	static ObjectMapper objectMapper = new ObjectMapper();
	
	/**
	 * Single-level serialization
	 */
	@Test
	public void serializedSuccess() {
		SampleObject sample = new SampleObject(1, "foo");
		
		// Convert to JSON string
		String jsonString = objectMapper.writeValueAsString(sample);
		
		// Convert back
		SampleObject deserializedSample = objectMapper.readValue(
				jsonString, SampleObject.class
				);
		
		Assertions.assertEquals(
				sample, deserializedSample, 
				"No exception thrown, but deserialization failed."
				);
	}
	
	/**
	 * Two-level serialization
	 */
	@Test
	public void serializedNestedSuccess() {
		SampleComposition master = new SampleComposition(1, "foo");
		master.setNestedObject(new SampleObject(2, "bar"));
		
		// Convert to JSON string
		String jsonString = objectMapper.writeValueAsString(master);
		
		// Convert back
		SampleComposition deserializedMaster = objectMapper.readValue(
				jsonString, SampleComposition.class
				);
		
		Assertions.assertEquals(
				master, deserializedMaster, 
				"No exception thrown, but deserialization failed."
				);
	}
	
	
	
	// SAMPLE OBJECTS
	
	/**
	 * The object contains one int field
	 * and one String field.
	 */
	public static class SampleObject {
		int intField = 0;
		String textField = "defaultValue";
		
		public SampleObject() {
			
		}
		public SampleObject(int intField, String textField) {
			this.intField = intField;
			this.textField = textField;
		}
		
		// Setters / Getters
		public int getIntField() {
			return intField;
		}
		public void setIntField(int intField) {
			this.intField = intField;
		}
		public String getTextField() {
			return textField;
		}
		public void setTextField(String textField) {
			this.textField = textField;
		}
		
		// To check equality
		@Override
		public boolean equals(Object o) {
			if (o instanceof SampleObject) {
				// Of the same class
				SampleObject comparedWith = (SampleObject) o;
				
				// Both fields must be equal
				if (comparedWith.intField != intField) {
					return false;
				}
				// Text fields may be null
				if (comparedWith.textField == null && 
						textField == null) {
					return true;
				}
				if (!comparedWith.textField.equals(textField)) {
					return false;
				}
				
				return true;
			}
			
			// Not of the same class
			return false;
		}
		
		// For debugging
		@Override
		public String toString() {
			return String.format(
					"SampleObject(%s;%s)", 
					String.valueOf(intField), 
					String.valueOf(textField)
					);
		}
	}
	
	/**
	 * The object contains one int field,
	 * one String field and one object field. <br>
	 * Extends SampleObject.
	 */
	public static class SampleComposition extends SampleObject {
		/** This is a nested serializable object */
		SampleObject nestedObject;
		
		public SampleComposition() {
			super();
		}
		public SampleComposition(int intField, String textField) {
			super(intField, textField);
		}
		
		// Setters / Getters
		public SampleObject getNestedObject() {
			return nestedObject;
		}
		public void setNestedObject(SampleObject nestedObject) {
			this.nestedObject = nestedObject;
		}

		@Override
		public boolean equals(Object o) {
			if (o instanceof SampleComposition) {
				// Of the same type
				SampleComposition comparedWith = (SampleComposition) o;
				
				// int and String fields must be equal
				if (!super.equals(comparedWith)) {
					return false;
				}
				
				// Nested objects must be equal (or both may be null)
				if (nestedObject == null && 
						comparedWith.nestedObject == null) {
					return true;
				}
				if (nestedObject.equals(comparedWith.nestedObject)) {
					return true;
				}
			}
			
			// Not of the same type OR nested objects aren't equal
			return false;
		}
		
		// For debugging
		@Override
		public String toString() {
			return String.format(
					"SampleComposition(%s;%s); contains: %s", 
					String.valueOf(intField), 
					String.valueOf(textField),
					String.valueOf(nestedObject)
					);
		}
	}
	
}
