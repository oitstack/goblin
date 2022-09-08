package io.github.oitstack.goblin.unit.elasticsearch;

import io.github.oitstack.goblin.unit.db.XUnitAssertionError;
import io.github.oitstack.goblin.unit.elasticsearch.parser.DataReader;
import org.elasticsearch.client.RestHighLevelClient;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class DefaultElasticsearchComparisonStrategy implements ElasticsearchComparisonStrategy {
	@Override
	public boolean compare(ElasticsearchConnectionCallback connection, InputStream dataset) throws XUnitAssertionError,
			Throwable {
		final RestHighLevelClient nodeClient = connection.client();
		final List<Map<String, Object>> documents = DataReader.getDocuments(dataset);
		ElasticsearchAssertion.strictAssertEquals(documents, nodeClient);
		return true;
	}

	@Override
	public void setIgnoreProperties(String[] ignoreProperties) {
	}
}
