package io.github.oitstack.goblin.unit.elasticsearch;


import io.github.oitstack.goblin.unit.elasticsearch.parser.DataReader;

import java.io.InputStream;

public class DefaultElasticsearchInsertionStrategy implements ElasticsearchInsertionStrategy {
	@Override
	public void insert(ElasticsearchConnectionCallback connection, InputStream dataset) throws Throwable {
		DataReader dataReader = new DataReader(connection.client());
		dataReader.read(dataset);
	}
}
