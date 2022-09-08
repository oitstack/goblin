package io.github.oitstack.goblin.unit.elasticsearch;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.oitstack.goblin.unit.db.FailureHandler;
import io.github.oitstack.goblin.unit.elasticsearch.parser.DataReader;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.GetIndexResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ElasticsearchAssertion {
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	public final static Logger logger = LoggerFactory.getLogger(ElasticsearchAssertion.class);

	private ElasticsearchAssertion() {
		super();
	}

	public static void strictAssertEquals(List<Map<String, Object>> expectedDocuments, RestHighLevelClient client) {

		checkNumberOfDocuments(expectedDocuments, client);

		for (Map<String, Object> document : expectedDocuments) {
			final Object object = document.get(DataReader.DOCUMENT_ELEMENT);

			if (object instanceof List) {
				@SuppressWarnings("unchecked")
				final List<Map<String, Object>> properties = (List<Map<String, Object>>) object;
				final List<GetRequest> indexes = new ArrayList<>();
				Map<String, Object> expectedDataOfDocument = new HashMap<>();

				for (Map<String, Object> property : properties) {
					if (property.containsKey(DataReader.INDEX_ELEMENT)) {
						indexes.add(prepareGetIndex(property.get(DataReader.INDEX_ELEMENT)));
					} else {
						if (property.containsKey(DataReader.DATA_ELEMENT)) {
							expectedDataOfDocument = dataOfDocument(property.get(DataReader.DATA_ELEMENT));
						}
					}
				}

				checkIndicesWithDocument(indexes, expectedDataOfDocument, client);
			} else {
				throw new IllegalArgumentException("Array of Indexes and Data are required.");
			}
		}
	}

	private static void checkIndicesWithDocument(List<GetRequest> indexes,
												 Map<String, Object> expectedDataOfDocument, RestHighLevelClient client) {

		for (GetRequest getRequest : indexes) {
			GetResponse dataOfDocumentResponse = null;
			try {
				dataOfDocumentResponse = client.get(getRequest, RequestOptions.DEFAULT);
				checkExistenceOfDocument(getRequest, dataOfDocumentResponse);
				checkDocumentEquality(expectedDataOfDocument, getRequest, dataOfDocumentResponse);
			} catch (IOException e) {
				logger.error("", e);
			}
		}
	}

	private static void checkDocumentEquality(Map<String, Object> expectedDataOfDocument,
											  GetRequest getRequest, GetResponse dataOfDocumentResponse) {
		final Map<String, Object> dataOfDocument = new LinkedHashMap<>(dataOfDocumentResponse.getSource());

		if (!DeepEquals.deepEquals(dataOfDocument, expectedDataOfDocument)) {
			try {
				throw FailureHandler.createFailure("Expected document for index: %s - id: %s is %s, but %s was found.", getRequest.index(),
						getRequest.id(), OBJECT_MAPPER.writeValueAsString(expectedDataOfDocument), OBJECT_MAPPER.writeValueAsString(dataOfDocument));
			} catch (IOException e) {
				throw new IllegalArgumentException(e);
			}
		}
	}

	private static void checkExistenceOfDocument(GetRequest getRequest, GetResponse dataOfDocumentResponse) {
		if (!dataOfDocumentResponse.isExists()) {
			throw FailureHandler.createFailure(
					"Document with index: %s - id: %s has not returned any document.",
					getRequest.index(), getRequest.id());
		}
	}

	private static void checkNumberOfDocuments(List<Map<String, Object>> expectedDocuments, RestHighLevelClient client) {
		int expectedNumberOfElements = expectedDocuments.size();
		long numberOfInsertedDocuments = numberOfInsertedDocuments(client);

		if (expectedNumberOfElements != numberOfInsertedDocuments) {
			throw FailureHandler.createFailure("Expected number of documents are %s but %s has been found.",
					expectedNumberOfElements, numberOfInsertedDocuments);
		}
	}

	private static GetRequest prepareGetIndex(Object object) {
		@SuppressWarnings("unchecked")
		Map<String, String> indexInformation = (Map<String, String>) object;
		GetRequest prepareGet = new GetRequest();

		if (indexInformation.containsKey(DataReader.INDEX_NAME_ELEMENT)) {
			prepareGet.index(indexInformation.get(DataReader.INDEX_NAME_ELEMENT));
		}

		if (indexInformation.containsKey(DataReader.INDEX_ID_ELEMENT)) {
			prepareGet.id(indexInformation.get(DataReader.INDEX_ID_ELEMENT));
		}

		return prepareGet;
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> dataOfDocument(Object object) {
		return (Map<String, Object>) object;
	}

	private static Map<String, String> dataOfIndex(Object object) {
		return (Map<String, String>) object;
	}

	private static long numberOfInsertedDocuments(RestHighLevelClient client) {
		long numberOfPresentedElements = 0L;
		try {
			GetIndexRequest request = new GetIndexRequest("*");
			GetIndexResponse response = client.indices().get(request, RequestOptions.DEFAULT);

			if (response.getIndices().length > 0) {
				CountRequest countRequest = new CountRequest(response.getIndices());
				countRequest.query(QueryBuilders.matchAllQuery());
				CountResponse countResponse = client.count(countRequest, RequestOptions.DEFAULT);

				numberOfPresentedElements = countResponse.getCount();
			}
		} catch (Throwable t) {
			if (t instanceof ElasticsearchStatusException) {
				if (((ElasticsearchStatusException) t).status() != RestStatus.NOT_FOUND) {
					logger.info("clear documents failed.", t);
				}
			} else {
				logger.error("", t);
			}
		}

		return numberOfPresentedElements;
	}
}
