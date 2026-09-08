package uk.ac.ceh.gateway.catalogue.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.model.bedrock.titan.autoconfigure.BedrockTitanEmbeddingAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Guards which property actually switches Bedrock embeddings on.
 * <p>
 * Spring AI gates {@link BedrockTitanEmbeddingAutoConfiguration} on
 * {@code spring.ai.model.embedding=bedrock-titan} with {@code matchIfMissing = true}; unlike its
 * Cohere and Converse siblings, Titan has no {@code spring.ai.bedrock.titan.embedding.enabled}
 * property. The application previously set that non-existent property, so it neither switched
 * embeddings on under the "vector-search" profile nor kept them off by default — and because
 * {@code matchIfMissing} defaults the condition to matching, the {@link EmbeddingModel} bean was
 * offered in every profile. Nothing failed loudly, which is why it went unnoticed.
 * <p>
 * {@link ConfigDataApplicationContextInitializer} loads the real {@code application.properties}, so
 * the "off by default" case asserts against the value the application actually ships rather than one
 * the test supplies.
 */
@DisplayName("Bedrock embedding model switch")
class EmbeddingModelSwitchTest {

    // Spring AI 2.0's Bedrock API is built with a Jackson 3 JsonMapper, which the application gets
    // from Boot's own auto-configuration, so the runner needs it too.
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(
            JacksonAutoConfiguration.class,
            BedrockTitanEmbeddingAutoConfiguration.class
        ));

    @Test
    @DisplayName("Shipped configuration keeps the embedding model out of the context")
    void offByDefault() {
        runner.withInitializer(new ConfigDataApplicationContextInitializer())
            .run(context -> {
                assertThat(context).hasNotFailed();
                assertThat(context).doesNotHaveBean(EmbeddingModel.class);
            });
    }

    @Test
    @DisplayName("Selecting bedrock-titan, as the vector-search profile does, wires the model")
    void onWhenBedrockTitanSelected() {
        runner.withPropertyValues(
                "spring.ai.model.embedding=bedrock-titan",
                "spring.ai.bedrock.aws.region=eu-west-2",
                "spring.ai.bedrock.titan.embedding.model=amazon.titan-embed-text-v2:0"
            )
            .run(context -> {
                assertThat(context).hasNotFailed();
                assertThat(context).hasSingleBean(EmbeddingModel.class);
            });
    }

    @Test
    @DisplayName("The retired enabled property no longer decides anything")
    void enabledPropertyIsInert() {
        runner.withPropertyValues(
                "spring.ai.model.embedding=none",
                "spring.ai.bedrock.titan.embedding.enabled=true"
            )
            .run(context -> assertThat(context).doesNotHaveBean(EmbeddingModel.class));
    }
}
