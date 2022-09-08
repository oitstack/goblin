package io.github.oitstack.goblin.unit.redis;

import io.github.oitstack.goblin.unit.db.FailureHandler;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static io.github.oitstack.goblin.unit.redis.parser.JsonParser.DATA_TOKEN;
import static io.github.oitstack.goblin.unit.redis.parser.JsonParser.FIELD_TOKEN;
import static io.github.oitstack.goblin.unit.redis.parser.JsonParser.HASH_TOKEN;
import static io.github.oitstack.goblin.unit.redis.parser.JsonParser.KEY_TOKEN;
import static io.github.oitstack.goblin.unit.redis.parser.JsonParser.LIST_TOKEN;
import static io.github.oitstack.goblin.unit.redis.parser.JsonParser.SCORE_TOKEN;
import static io.github.oitstack.goblin.unit.redis.parser.JsonParser.SET_TOKEN;
import static io.github.oitstack.goblin.unit.redis.parser.JsonParser.SIMPLE_TOKEN;
import static io.github.oitstack.goblin.unit.redis.parser.JsonParser.SORTSET_TOKEN;
import static io.github.oitstack.goblin.unit.redis.parser.JsonParser.VALUES_TOKEN;
import static io.github.oitstack.goblin.unit.redis.parser.JsonParser.VALUE_TOKEN;
import static io.github.oitstack.goblin.unit.redis.parser.JsonToJedisConverter.toByteArray;
//import static org.hamcrest.Matchers.equalTo;

public class RedisAssertion {

	public static void strictAssertEquals(RedisConnectionCallback redisConnectionCallback, InputStream expectedData) {

		Object parse = JSONValue.parse(new InputStreamReader(expectedData));
		JSONObject rootObject = (JSONObject) parse;

		JSONArray dataObject = (JSONArray) rootObject.get(DATA_TOKEN);

		long expectedTotalNumberOfKeys = 0;

		for (Object object : dataObject) {
			JSONObject elementObject = (JSONObject) object;

			if (elementObject.containsKey(SIMPLE_TOKEN)) {

				expectedTotalNumberOfKeys += checkSimpleValues(elementObject, redisConnectionCallback);

			} else {

				if (elementObject.containsKey(LIST_TOKEN)) {
					expectedTotalNumberOfKeys += checkListsValue(elementObject, redisConnectionCallback);
				} else {

					if (elementObject.containsKey(SORTSET_TOKEN)) {
						expectedTotalNumberOfKeys += checkSortSetsValue(elementObject, redisConnectionCallback);
					} else {

						if (elementObject.containsKey(HASH_TOKEN)) {
							expectedTotalNumberOfKeys += checkHashesValue(elementObject, redisConnectionCallback);
						} else {

							if (elementObject.containsKey(SET_TOKEN)) {
								expectedTotalNumberOfKeys += checkSetsValue(elementObject, redisConnectionCallback);
							}

						}

					}
				}
			}
		}

		checkNumberOfKeys(redisConnectionCallback, expectedTotalNumberOfKeys);

	}

	private static void checkNumberOfKeys(RedisConnectionCallback redisConnectionCallback,
			long expectedTotalNumberOfKeys) throws Error {

		long insertedElements = countNumberOfAllElements(redisConnectionCallback);

		if (expectedTotalNumberOfKeys != insertedElements) {
			throw FailureHandler.createFailure("Number of expected keys are %s but was found %s.",
					expectedTotalNumberOfKeys, insertedElements);
		}
	}

	private static long countNumberOfAllElements(RedisConnectionCallback redisConnectionCallback) {

		return redisConnectionCallback.redisClient().dbsize();
	}

	private static long checkHashesValue(JSONObject expectedElementObject,
			RedisConnectionCallback redisConnectionCallback) {
		long numberOfKeys = 0;

		JSONArray expectedSortsetsObject = (JSONArray) expectedElementObject.get(HASH_TOKEN);

		for (Object object : expectedSortsetsObject) {

			JSONObject expectedHashObject = (JSONObject) object;
			checkHashValues(redisConnectionCallback, expectedHashObject);

			numberOfKeys++;

		}

		return numberOfKeys;
	}

	private static void checkHashValues(RedisConnectionCallback redisConnectionCallback, JSONObject expectedHashObject) {

		Object expectedKey = expectedHashObject.get(KEY_TOKEN);

		byte[] key = toByteArray(expectedKey);
		RedisClient redisClient = redisConnectionCallback.redisClient();

		checkType(redisClient, expectedKey, key, "hash");

		/** field:.., value:... */
		JSONArray expectedValuesArray = (JSONArray) expectedHashObject.get(VALUES_TOKEN);

		Map<byte[], byte[]> currentFields = redisClient.hgetAllAsMapb(key);

		checkNumberOfFields(key, expectedValuesArray, currentFields);
		checkFields(key, expectedValuesArray, currentFields);
	}

	private static void checkFields(byte[] key, JSONArray expectedValuesArray, Map<byte[], byte[]> currentFields) {
		for (Object object : expectedValuesArray) {

			JSONObject expectedField = (JSONObject) object;

			byte[] expectedFieldName = toByteArray(expectedField.get(FIELD_TOKEN));
			byte[] expectedFieldValue = toByteArray(expectedField.get(VALUE_TOKEN));

			Set<Entry<byte[], byte[]>> currentFieldsSet = currentFields.entrySet();

			boolean found = false;
			for (Entry<byte[], byte[]> entry : currentFieldsSet) {
				if (Arrays.equals(entry.getKey(), expectedFieldName)) {
					found = true;
					if ( ! Arrays.equals(entry.getValue(), expectedFieldValue)) {
						throw FailureHandler.createFailure("Key %s and field %s does not contain element %s but %s.",
								new String(key), new String(expectedFieldName), new String(expectedFieldValue), new String(
										entry.getValue()));
					}
				}
			}

			if ( ! found) {
				throw FailureHandler.createFailure("Key %s and field %s not exists.",
						new String(key), new String(expectedFieldName));
			}

		}
	}

	private static void checkNumberOfFields(byte[] key, JSONArray expectedValuesArray, Map<byte[], byte[]> hgetAll)
			throws Error {
		int numberExpectedFields = expectedValuesArray.size();
		int numberOfFields = hgetAll.size();

		if (numberExpectedFields != numberOfFields) {
			throw FailureHandler.createFailure("Expected fields for key %s are %s but %s was found.", new String(key),
					numberExpectedFields, numberOfFields);
		}
	}

	private static long checkSortSetsValue(JSONObject expectedElementObject, RedisConnectionCallback redisConnectionCallback) {

		long numberOfKeys = 0;

		JSONArray expectedSortsetsObject = (JSONArray) expectedElementObject.get(SORTSET_TOKEN);

		for (Object object : expectedSortsetsObject) {
			JSONObject expectedSortsetObject = (JSONObject) object;
			checkSortSetValues(redisConnectionCallback, expectedSortsetObject);

			numberOfKeys++;
		}

		return numberOfKeys;
	}

	private static void checkSortSetValues(RedisConnectionCallback redisConnectionCallback, JSONObject expectedSortsetObject) throws Error {
		Object expectedKey = expectedSortsetObject.get(KEY_TOKEN);

		byte[] key = toByteArray(expectedKey);
		RedisClient redisClient = redisConnectionCallback.redisClient();

		checkType(redisClient, expectedKey, key, "zset");

		/** value:.., score:... */
		JSONArray expectedValuesArray = (JSONArray) expectedSortsetObject.get(VALUES_TOKEN);

		Set<byte[]> currentElements = redisClient.zrangeb(key, 0, -1);
		List<byte[]> expectedOrderedValues = extractSortValues(expectedValuesArray);

		checkListSize(expectedOrderedValues.size(), expectedKey, currentElements.size());
		checkSetElementsAndPosition(expectedKey, currentElements, expectedOrderedValues);
	}

	private static void checkSetElementsAndPosition(Object expectedKey, Set<byte[]> zrange,
			List<byte[]> expectedOrderedValues) throws Error {
		int position = 0;
		for (byte[] value : zrange) {

			if (!Arrays.equals(value, expectedOrderedValues.get(position))) {
				throw FailureHandler.createFailure("Element %s is not found in set with same order of key %s.",
						new String(expectedOrderedValues.get(position)), expectedKey);
			}

			position++;
		}
	}

	private static List<byte[]> extractSortValues(JSONArray expectedValuesArray) {

		Set<SortElement> elementsOrderedByScore = new TreeSet<SortElement>(
				(o1, o2) -> Float.compare(o1.getScore().floatValue(), o2.getScore().floatValue())
		);

		for (Object expectedObject : expectedValuesArray) {
			JSONObject expectedValue = (JSONObject) expectedObject;

			elementsOrderedByScore.add(new SortElement((Number) expectedValue.get(SCORE_TOKEN),
					toByteArray(expectedValue.get(VALUE_TOKEN))));
		}

		return elementsOrderedByScore.stream().map(e -> e.getValue()).collect(Collectors.toList());
	}

	private static long checkSetsValue(JSONObject expectedElementObject, RedisConnectionCallback redisConnectionCallback) {
		long numberOfKeys = 0;

		JSONArray expectedListsObject = (JSONArray) expectedElementObject.get(SET_TOKEN);

		for (Object object : expectedListsObject) {
			JSONObject expectedListObject = (JSONObject) object;
			checkSetValues(redisConnectionCallback, expectedListObject);

			numberOfKeys++;
		}

		return numberOfKeys;
	}

	private static void checkSetValues(RedisConnectionCallback redisConnectionCallback, JSONObject expectedSetObject) {

		JSONArray expectedValuesArray = (JSONArray) expectedSetObject.get(VALUES_TOKEN);
		Set<byte[]> expectedSetValues = extractSetOfValues(expectedValuesArray);

		Object expectedKey = expectedSetObject.get(KEY_TOKEN);

		byte[] key = toByteArray(expectedKey);
		RedisClient redisClient = redisConnectionCallback.redisClient();

		checkType(redisClient, expectedKey, key, "set");

		Set<byte[]> elements = redisClient.smembersb(key);

		checkListSize(expectedSetValues.size(), expectedKey, elements.size());
		checkValueInSet(expectedSetValues, expectedKey, elements);
	}

	private static Set<byte[]> extractSetOfValues(JSONArray valuesArray) {

		Set<byte[]> setValues = new TreeSet<byte[]>(new Comparator<byte[]>() {
			public int compare(byte[] left, byte[] right) {
				for (int i = 0, j = 0; i < left.length && j < right.length; i++, j++) {
					int a = (left[i] & 0xff);
					int b = (right[j] & 0xff);
					if (a != b) {
						return a - b;
					}
				}
				return left.length - right.length;
			}
		});

		for (Object valueObject : valuesArray) {
			JSONObject jsonValueObject = (JSONObject) valueObject;
			byte[] value = toByteArray(jsonValueObject.get(VALUE_TOKEN));

			if (!setValues.contains(value)) {
				setValues.add(value);
			}
		}
		return setValues;
	}

	private static void checkValueInSet(Set<byte[]> expectedListValues, Object expectedKey, Set<byte[]> elements)
			throws Error {
		for (byte[] bs : elements) {
			if (!isValuePresent(expectedListValues, bs)) {
				throw FailureHandler.createFailure("Element %s is not found in set of key %s.", new String(bs),
						expectedKey);
			}
		}
	}

	private static long checkListsValue(JSONObject expectedElementObject, RedisConnectionCallback redisConnectionCallback) {

		long numberOfKeys = 0;

		JSONArray expectedListsObject = (JSONArray) expectedElementObject.get(LIST_TOKEN);

		for (Object object : expectedListsObject) {
			JSONObject expectedListObject = (JSONObject) object;
			checkListValues(redisConnectionCallback, expectedListObject);

			numberOfKeys++;
		}

		return numberOfKeys;
	}

	private static void checkListValues(RedisConnectionCallback redisConnectionCallback, JSONObject expectedListObject) throws Error {
		JSONArray expectedValuesArray = (JSONArray) expectedListObject.get(VALUES_TOKEN);
		List<byte[]> expectedListValues = extractListOfValues(expectedValuesArray);

		Object expectedKey = expectedListObject.get(KEY_TOKEN);
		byte[] key = toByteArray(expectedKey);

		RedisClient redisClient = redisConnectionCallback.redisClient();
		
		checkType(redisClient, expectedKey, key, "list");

		List<byte[]> elements = redisClient.lrangeb(key, 0, -1);

		checkListSize(expectedListValues.size(), expectedKey, elements.size());
		checkValueInList(expectedListValues, expectedKey, elements);
	}

	private static void checkValueInList(List<byte[]> expectedListValues, Object expectedKey, List<byte[]> elements)
			throws Error {
		for (byte[] bs : elements) {
			if (!isValuePresent(expectedListValues, bs)) {
				throw FailureHandler.createFailure("Element %s is not found in list of key %s.", new String(bs),
						expectedKey);
			}
		}
	}

	private static void checkListSize(int expectedListValues, Object expectedKey, int lrange) throws Error {
		if (lrange != expectedListValues) {
			throw FailureHandler.createFailure("Expected number of elements for key %s is %s but was counted %s.",
					expectedKey, expectedListValues, lrange);
		}
	}

	private static void checkType(RedisClient redisClient, Object expectedKey, byte[] key, String expectedType) throws Error {
		String type = redisClient.type(new String(key));

		if ("none".equals(type)) {
			throw FailureHandler.createFailure("Key %s is not found.", expectedKey);
		}

		if (!expectedType.equals(type)) {
			throw FailureHandler.createFailure("Element with key %s is not a %s.", expectedKey, expectedType);
		}
	}

	private static boolean isValuePresent(Iterable<byte[]> expectedValues, byte[] value) {

		for (byte[] expectedBytes : expectedValues) {
			if (Arrays.equals(expectedBytes, value)) {
				return true;
			}
		}

		return false;
	}

	private static List<byte[]> extractListOfValues(JSONArray valuesArray) {
		List<byte[]> listValues = new ArrayList<byte[]>();

		for (Object valueObject : valuesArray) {
			JSONObject jsonValueObject = (JSONObject) valueObject;
			listValues.add(toByteArray(jsonValueObject.get(VALUE_TOKEN)));
		}
		return listValues;
	}

	private static long checkSimpleValues(JSONObject expectedElementObject, RedisConnectionCallback redisConnectionCallback) {

		long numberOfKeys = 0;

		JSONArray expectedSimpleElements = (JSONArray) expectedElementObject.get(SIMPLE_TOKEN);

		for (Object expectedSimpleElement : expectedSimpleElements) {
			JSONObject expectedSimpleElementObject = (JSONObject) expectedSimpleElement;

			Object expectedKey = expectedSimpleElementObject.get(KEY_TOKEN);
			byte[] key = toByteArray(expectedKey);

			RedisClient redisClient = redisConnectionCallback.redisClient();
			
			checkType(redisClient, expectedKey, key, "string");

			byte[] value = redisClient.getb(key);
			byte[] expectedValue = toByteArray(expectedSimpleElementObject.get(VALUE_TOKEN));

			boolean isExpected = Arrays.equals(value, expectedValue);

			if (!isExpected) {
				throw FailureHandler.createFailure("Key %s does not contain element %s but %s.", expectedKey,
						new String(expectedValue), new String(value));
			}

			numberOfKeys++;
		}

		return numberOfKeys;
	}

	private static class SortElement implements Comparable<SortElement> {

		private Number score;
		private byte[] value;

		public SortElement(Number score, byte[] value) {
			super();
			this.score = score;
			this.value = value;
		}

		public Number getScore() {
			return score;
		}

		public byte[] getValue() {
			return value;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((score == null) ? 0 : score.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			SortElement other = (SortElement) obj;
			if (score == null) {
				if (other.score != null)
					return false;
			} else if (!score.equals(other.score))
				return false;
			return true;
		}

		@Override
		public int compareTo(SortElement o) {
			double o1 = this.score.doubleValue();
			double o2 = o.score.doubleValue();

			if (o1 > o2) {
				return 1;
			} else {
				if (o1 < o2) {
					return -1;
				} else {
					return 0;
				}
			}
		}

	}
}
