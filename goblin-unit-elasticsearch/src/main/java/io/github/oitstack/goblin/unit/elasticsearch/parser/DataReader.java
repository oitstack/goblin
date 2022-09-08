package io.github.oitstack.goblin.unit.elasticsearch.parser;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.GetIndexResponse;
import org.elasticsearch.common.xcontent.XContentType;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataReader {
	private static final ObjectMapper MAPPER = new ObjectMapper();

	public static final String DOCUMENTS_ELEMENT = "documents";
	public static final String DOCUMENT_ELEMENT = "document";
	public static final String DATA_ELEMENT = "data";
	public static final String INDEX_ELEMENT = "index";
	public static final String INDEX_NAME_ELEMENT = "indexName";
	public static final String INDEX_TYPE_ELEMENT = "indexType";
	public static final String INDEX_ID_ELEMENT = "indexId";

	private RestHighLevelClient client;

	public DataReader(RestHighLevelClient client) {
		this.client = client;
	}

	public void read(InputStream data) {
		try {
			final List<Map<String, Object>> documents = getDocuments(data);
			insertDocuments(documents);
			refreshNode();
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

	private void refreshNode() {
		try {
			GetIndexRequest request = new GetIndexRequest("*");
			GetIndexResponse response = client.indices().get(request, RequestOptions.DEFAULT);
			client.indices().refresh(new RefreshRequest(response.getIndices()), RequestOptions.DEFAULT);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void insertDocuments(List<Map<String, Object>> documents) {
		for (Map<String, Object> document : documents) {
			final Object object = document.get(DOCUMENT_ELEMENT);

			if (object instanceof List) {
				@SuppressWarnings("unchecked")
				final List<Map<String, Object>> properties = (List<Map<String, Object>>) object;
				insertDocument(properties);
			} else {
				throw new IllegalArgumentException("Array of Indexes and Data are required.");
			}
		}
	}

	private void insertDocument(List<Map<String, Object>> properties) {
		final List<Map<String, String>> indexes = new ArrayList<>();
		Map<String, Object> dataOfDocument = new HashMap<>();

		for (Map<String, Object> property : properties) {
			if (property.containsKey(INDEX_ELEMENT)) {
				indexes.add(dataOfIndex(property.get(INDEX_ELEMENT)));
			} else {
				if (property.containsKey(DATA_ELEMENT)) {
					dataOfDocument = dataOfDocument(property.get(DATA_ELEMENT));
				}
			}
		}

		insertIndexes(indexes, dataOfDocument);
	}

	private void insertIndexes(List<Map<String, String>> indexes, Map<String, Object> dataOfDocument) {

		for (Map<String, String> index : indexes) {
			IndexRequest indexRequest = new IndexRequest();
			indexRequest.source(dataOfDocument, XContentType.JSON);
			if (index.containsKey(INDEX_ID_ELEMENT)) {
				indexRequest.id(index.get(INDEX_ID_ELEMENT));
			}
			if (index.containsKey(INDEX_NAME_ELEMENT)) {
				indexRequest.index(index.get(INDEX_NAME_ELEMENT));
			}

			try {
				client.index(indexRequest, RequestOptions.DEFAULT);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> dataOfDocument(Object object) {
		return (Map<String, Object>) object;
	}

	private Map<String, String> dataOfIndex(Object object) {
		return (Map<String, String>) object;
	}

	private CreateIndexRequest createIndex(Object object) {
		@SuppressWarnings("unchecked")
		final Map<String, String> indexInformation = (Map<String, String>) object;
		final CreateIndexRequest prepareIndex = new CreateIndexRequest(indexInformation.get(INDEX_NAME_ELEMENT));

//		if (indexInformation.containsKey(INDEX_TYPE_ELEMENT)) {
//			prepareIndex.setType(indexInformation.get(INDEX_TYPE_ELEMENT));
//		}
//
//		if (indexInformation.containsKey(INDEX_ID_ELEMENT)) {
//			prepareIndex.setId(indexInformation.get(INDEX_ID_ELEMENT));
//		}

		return prepareIndex;
	}

	@SuppressWarnings("unchecked")
	public static List<Map<String, Object>> getDocuments(InputStream data) throws IOException {
		final Map<String, Object> rootNode = MAPPER.readValue(data, new TypeReference<Map<String, Object>>() {
		});
		final Object dataElements = rootNode.get(DOCUMENTS_ELEMENT);

		if (dataElements instanceof List) {
			return (List<Map<String, Object>>) dataElements;
		} else {
			throw new IllegalArgumentException("Array of documents are required.");
		}
	}
}
