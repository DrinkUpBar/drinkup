package cool.drinkup.drinkup.wine.internal.rag.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VectorStoreConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    // @Bean
    // public JedisPooled jedisPooled() {
    //     return new JedisPooled(redisHost, redisPort);
    // }

    // @Bean
    // VectorStore vectorStore(JedisPooled jedisPooled, @Qualifier("openAiEmbeddingModel")
    // EmbeddingModel embeddingModel) {
    //     return RedisVectorStore.builder(jedisPooled, embeddingModel)
    //             .initializeSchema(true)
    //             .indexName("wine-index") // Optional: defaults to "spring-ai-index"
    //             .metadataFields( // Optional: define metadata fields for filtering
    //                     MetadataField.tag("wineId")
    //             )
    //             .batchingStrategy(new TokenCountBatchingStrategy()) // Optional: defaults to
    // TokenCountBatchingStrategy
    //             .build();
    // }

    // @Bean
    // public VectorStore vectorStore(MilvusServiceClient milvusClient,
    // @Qualifier("openAiEmbeddingModel") EmbeddingModel embeddingModel) {
    // 	return MilvusVectorStore.builder(milvusClient, embeddingModel)
    // 			.collectionName(milvusCollectionName)
    // 			.databaseName(milvusDatabaseName)
    // 			.indexType(IndexType.IVF_FLAT)
    // 			.metricType(MetricType.COSINE)
    // 			.batchingStrategy(new TokenCountBatchingStrategy())
    // 			.initializeSchema(true)
    // 			.build();
    // }

    // @Bean
    // public MilvusServiceClient milvusClient() {
    // 	return new MilvusServiceClient(ConnectParam.newBuilder()
    // 		.withAuthorization(milvusUsername, milvusPassword)
    // 		.withUri(milvusHost + ":" + milvusPort)
    // 		.build());
    // }
}
