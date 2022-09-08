package io.github.oitstack.goblin.unit.elasticsearch;


import org.elasticsearch.client.RestHighLevelClient;

public interface ElasticsearchConnectionCallback {
	RestHighLevelClient client();
}
