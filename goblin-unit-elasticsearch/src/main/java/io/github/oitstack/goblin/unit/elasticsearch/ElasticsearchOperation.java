package io.github.oitstack.goblin.unit.elasticsearch;

import io.github.oitstack.goblin.unit.db.AbstractCustomizableDatabaseOperation;
import io.github.oitstack.goblin.unit.db.XUnitAssertionError;
import org.apache.http.HttpHost;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.GetIndexResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.rest.RestStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

public class ElasticsearchOperation extends
		AbstractCustomizableDatabaseOperation<ElasticsearchConnectionCallback, RestHighLevelClient> {

	private RestHighLevelClient client;

	public final static Logger logger = LoggerFactory.getLogger(ElasticsearchOperation.class);

	public ElasticsearchOperation(ElasticsearchConfiguration configuration) {

		RestHighLevelClient client = new RestHighLevelClient(
				RestClient.builder(new HttpHost(configuration.getHost(), configuration.getPort(), "http")));
		this.client = client;
		setInsertStrategy(new DefaultElasticsearchInsertionStrategy());
		setCompareStrategy(new DefaultElasticsearchComparisonStrategy());
	}

	@Override
	public void insert(InputStream dataScript) {
		insertData(dataScript);
	}

	private void insertData(InputStream dataScript) {
		try {
			executeInsertion(() -> client, dataScript);
		} catch (Throwable e) {
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	public void deleteAll(InputStream dataScript) {
		try {
			clearDocuments();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void clearDocuments() throws IOException {
		try {
			GetIndexRequest request = new GetIndexRequest("*");
			GetIndexResponse response = client.indices().get(request, RequestOptions.DEFAULT);

			if (response.getIndices().length > 0) {
				DeleteByQueryRequest deleteByQueryRequest = new DeleteByQueryRequest(response.getIndices());
				deleteByQueryRequest.setQuery(QueryBuilders.matchAllQuery());
				client.deleteByQuery(deleteByQueryRequest, RequestOptions.DEFAULT);

				client.indices().refresh(new RefreshRequest(response.getIndices()), RequestOptions.DEFAULT);
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
	}

	@Override
	public boolean databaseIs(InputStream expectedData) {
		try {
			executeComparison(() -> client, expectedData);
			return true;
		} catch (XUnitAssertionError e) {
			throw e;
		} catch (Throwable e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public RestHighLevelClient connectionManager() {
		return client;
	}
}
