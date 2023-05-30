package io.github.oitstack.goblin.unit.redis.parser;

import io.github.oitstack.goblin.unit.redis.RedisClient;
import io.github.oitstack.goblin.unit.redis.serializer.Serializer;
import io.github.oitstack.goblin.unit.redis.serializer.SerializerFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static io.github.oitstack.goblin.unit.redis.parser.JsonToJedisConverter.toByteArray;
import static io.github.oitstack.goblin.unit.redis.parser.JsonToJedisConverter.toDouble;


public class JsonParser {

    public static final String SCORE_TOKEN = "score";
    public static final String SORTSET_TOKEN = "sortset";
    public static final String VALUES_TOKEN = "values";
    public static final String LIST_TOKEN = "list";
    public static final String VALUE_TOKEN = "value";
    public static final String KEY_TOKEN = "key";
    public static final String SIMPLE_TOKEN = "simple";
    public static final String DATA_TOKEN = "data";
    public static final String HASH_TOKEN = "hash";
    public static final String FIELD_TOKEN = "field";
    public static final String EXPIRE_SEC_TOKEN = "expireSeconds";
    public static final String EXPIRE_AT_SEC_TOKEN = "expireAtSeconds";
    public static final String SET_TOKEN = "set";
    public static final String FIELD_SERIALIZER_TOKEN = "field_serializer";
    public static final String VALUE_SERIALIZER_TOKEN = "value_serializer";
    public static final String KEY_SERIALIZER_TOKEN = "key_serializer";
    public static final String KEY_SCORE_TOKEN = "score_serializer";
    private RedisClient redisClient;

    public JsonParser(RedisClient redisClient) {
        this.redisClient = redisClient;
    }

    public void insert(InputStream data) {

        Object parse = JSONValue.parse(new InputStreamReader(data));
        JSONObject rootObject = (JSONObject) parse;

        JSONArray dataObject = (JSONArray) rootObject.get(DATA_TOKEN);

        for (Object object : dataObject) {
            JSONObject elementObject = (JSONObject) object;

            if (elementObject.containsKey(SIMPLE_TOKEN)) {
                addSimpleValues(elementObject);
            } else {
                if (elementObject.containsKey(LIST_TOKEN)) {
                    addListsElement(elementObject);
                } else {
                    if (elementObject.containsKey(SORTSET_TOKEN)) {
                        addSortSetsElement(elementObject);
                    } else {
                        if (elementObject.containsKey(HASH_TOKEN)) {
                            addHashesElement(elementObject);
                        } else {
                            if (elementObject.containsKey(SET_TOKEN)) {
                                addSetsElement(elementObject);
                            }
                        }
                    }
                }
            }
        }
    }

    public void delete(InputStream data) {
        Object parse = JSONValue.parse(new InputStreamReader(data));
        JSONObject rootObject = (JSONObject) parse;

        JSONArray dataObject = (JSONArray) rootObject.get(DATA_TOKEN);

        for (Object object : dataObject) {
            JSONObject elementObject = (JSONObject) object;

            for (JSONArray jsonArray : (Collection<JSONArray>) elementObject.values()) {
                for (Object obj : jsonArray) {
                    Object key = ((JSONObject) obj).get(KEY_TOKEN);
                    this.redisClient.del(new String(toByteArray(key)));
                }
            }
        }
    }

    private void addHashesElement(JSONObject hashesObject) {
        JSONArray hashsetObjs = (JSONArray) hashesObject.get(HASH_TOKEN);


        for (Object object : hashsetObjs) {
            JSONObject hashObject = (JSONObject) object;
            addHashElements(hashObject);
        }

    }

    private void addHashElements(JSONObject hashesObject) {

        Object key = hashesObject.get(KEY_TOKEN);
        JSONArray valuesArray = (JSONArray) hashesObject.get(VALUES_TOKEN);

        Map<byte[], byte[]> value = new HashMap<>();

        String keySerializerName = (String) hashesObject.get(KEY_SERIALIZER_TOKEN);
        String fieldSerializerName = (String) hashesObject.get(FIELD_SERIALIZER_TOKEN);
        String valueSerializerName = (String) hashesObject.get(VALUE_SERIALIZER_TOKEN);

        Serializer fieldSerializer = SerializerFactory.getInstance().getSerializer(fieldSerializerName);
        Serializer valueSerializer = SerializerFactory.getInstance().getSerializer(valueSerializerName);
        Serializer keySerializer = SerializerFactory.getInstance().getSerializer(keySerializerName);


        for (Object object : valuesArray) {
            JSONObject fieldObject = (JSONObject) object;

            value.put(fieldSerializer.serialize((String) fieldObject.get(FIELD_TOKEN)), valueSerializer.serialize((String) fieldObject.get(VALUE_TOKEN)));
        }
        this.redisClient.hmset(keySerializer.serialize((String) key), value);
        setTTL(hashesObject, key);
    }


    private void addSortSetsElement(JSONObject elementObject) {

        JSONArray sortsetsObject = (JSONArray) elementObject.get(SORTSET_TOKEN);

        for (Object object : sortsetsObject) {
            JSONObject sortsetObject = (JSONObject) object;
            addSortSetElements(sortsetObject);
        }

    }

    private void addSortSetElements(JSONObject sortsetObject) {
        Object key = sortsetObject.get(KEY_TOKEN);
        JSONArray valuesArray = (JSONArray) sortsetObject.get(VALUES_TOKEN);

        Map<byte[], Double> scoreMembers = new HashMap<>();
        String keySerializerName = (String) sortsetObject.get(KEY_SERIALIZER_TOKEN);
        String valueSerializerName = (String) sortsetObject.get(VALUE_SERIALIZER_TOKEN);

        Serializer valueSerializer = SerializerFactory.getInstance().getSerializer(valueSerializerName);
        Serializer keySerializer = SerializerFactory.getInstance().getSerializer(keySerializerName);

        for (Object valueObject : valuesArray) {
            JSONObject valueScopeObject = (JSONObject) valueObject;
            scoreMembers.put(valueSerializer.serialize((String)valueScopeObject.get(VALUE_TOKEN)),toDouble(valueScopeObject.get(SCORE_TOKEN)));
//            scoreMembers.put(toByteArray(valueScopeObject.get(VALUE_TOKEN)),
//                    toDouble(valueScopeObject.get(SCORE_TOKEN)));
        }

        scoreMembers.entrySet().forEach(e -> {
            this.redisClient.zadd(keySerializer.serialize((String) key), e.getValue().toString().getBytes(), e.getKey());
        });
        setTTL(sortsetObject, key);
    }

    private void addSetsElement(JSONObject elementObject) {
        JSONArray setObjects = (JSONArray) elementObject.get(SET_TOKEN);

        for (Object object : setObjects) {
            JSONObject setObject = (JSONObject) object;
            addSetElements(setObject);
        }
    }

    private void addSetElements(JSONObject setObject) {
        Object key = setObject.get(KEY_TOKEN);
        JSONArray valuesArray = (JSONArray) setObject.get(VALUES_TOKEN);
        List<byte[]> listValues = extractListOfValues(valuesArray,setObject);

        String keySerializerName = (String) setObject.get(KEY_SERIALIZER_TOKEN);
        Serializer keySerializer = SerializerFactory.getInstance().getSerializer(keySerializerName);

        listValues.forEach(e -> {
            this.redisClient.sadd(keySerializer.serialize((String)key), e);
        });
        setTTL(setObject, key);
    }

    private void addListsElement(JSONObject elementObject) {
        JSONArray listObjects = (JSONArray) elementObject.get(LIST_TOKEN);

        for (Object object : listObjects) {
            JSONObject listObject = (JSONObject) object;
            addListElements(listObject);
        }
    }

    private void addListElements(JSONObject listObject) {
        JSONArray valuesArray = (JSONArray) listObject.get(VALUES_TOKEN);
        List<byte[]> listValues = extractListOfValues(valuesArray,listObject);
        Object key = listObject.get(KEY_TOKEN);

        String keySerializerName = (String) listObject.get(KEY_SERIALIZER_TOKEN);
        Serializer keySerializer = SerializerFactory.getInstance().getSerializer(keySerializerName);

        listValues.forEach(e -> {
            this.redisClient.rpush(keySerializer.serialize((String)listObject.get(KEY_TOKEN)), e);
        });

        setTTL(listObject, key);
    }

    private List<byte[]> extractListOfValues(JSONArray valuesArray,JSONObject setObject) {
        List<byte[]> listValues = new LinkedList<>();

        String valueSerializerName = (String) setObject.get(VALUE_SERIALIZER_TOKEN);
        Serializer valueSerializer = SerializerFactory.getInstance().getSerializer(valueSerializerName);

        for(Object valueObject:valuesArray){
            JSONObject valueScopeObject = (JSONObject) valueObject;
            listValues.add(valueSerializer.serialize((String)valueScopeObject.get(VALUE_TOKEN)));
        }
        return listValues;
    }

    private void addSimpleValues(JSONObject elementObject) {
        JSONArray simpleElements = (JSONArray) elementObject.get(SIMPLE_TOKEN);
        for (Object simpleElement : simpleElements) {
            JSONObject simpleElementObject = (JSONObject) simpleElement;
            Object key = simpleElementObject.get(KEY_TOKEN);

            String keySerializerName = (String)simpleElementObject.get(KEY_SERIALIZER_TOKEN);
            Serializer keySerializer = SerializerFactory.getInstance().getSerializer(keySerializerName);
            String valueSerializerName = (String) simpleElementObject.get(VALUE_SERIALIZER_TOKEN);
            Serializer valueSerializer = SerializerFactory.getInstance().getSerializer(valueSerializerName);

            this.redisClient.set(keySerializer.serialize((String) key),
                    valueSerializer.serialize((String) simpleElementObject.get(VALUE_TOKEN)));
            setTTL(simpleElementObject, key);
        }
    }

    private void setTTL(JSONObject object, Object key) {

        if (object.containsKey(EXPIRE_AT_SEC_TOKEN)) {
            Object expirationDate = object.get(EXPIRE_AT_SEC_TOKEN);

            if (expirationDate instanceof Long) {
                this.redisClient.expireat(new String(toByteArray(key)), (Long) expirationDate);
            } else {
                throw new IllegalArgumentException("TTL expiration date should be a long value.");
            }
        }

        if (object.containsKey(EXPIRE_SEC_TOKEN)) {
            Object expiration = object.get(EXPIRE_SEC_TOKEN);

            if (expiration instanceof Long) {
                this.redisClient.expire(new String(toByteArray(key)), ((Long) expiration).intValue());
            } else {
                throw new IllegalArgumentException("TTL expiration date should be an integer value.");
            }

        }

    }

}

