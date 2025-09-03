package uk.ac.ceh.gateway.catalogue.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI catalogueOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("EIDC Catalogue API Documentation")
                .description("This API helps to search and retrieve EIDC Catalogue metadata")
                .version("1.0.0")
            );
    }

    @Bean
    public OpenApiCustomizer showPredefinedEndpoint() {
        Map<String, Set<String>> pathsToKeep = Map.of(
            "/documents/{file}", Set.of("GET")
        );

        return openApi -> {
            openApi.getPaths().keySet().removeIf(path ->
                !pathsToKeep.containsKey(path)
            );

            openApi.getPaths().forEach((path, pathItem) -> {
                Set<String> methodsToKeep = pathsToKeep.getOrDefault(path, Set.of());

                pathItem.readOperations().forEach(operation -> {
                    if (operation.getParameters() != null) {
                        operation.setParameters(operation.getParameters().stream()
                            .filter(param -> {
                                if (param.getSchema() == null) return true;
                                String ref = param.getSchema().get$ref();
                                return ref == null || !ref.endsWith("CatalogueUser");
                            })
                            .collect(Collectors.toList())
                        );
                    }
                });

                if (!methodsToKeep.contains("GET")) pathItem.setGet(null);
                if (!methodsToKeep.contains("POST")) pathItem.setPost(null);
                if (!methodsToKeep.contains("PUT")) pathItem.setPut(null);
                if (!methodsToKeep.contains("DELETE")) pathItem.setDelete(null);
                if (!methodsToKeep.contains("PATCH")) pathItem.setPatch(null);
                if (!methodsToKeep.contains("HEAD")) pathItem.setHead(null);
                if (!methodsToKeep.contains("OPTIONS")) pathItem.setOptions(null);
                if (!methodsToKeep.contains("TRACE")) pathItem.setTrace(null);
            });

            openApi.getPaths().entrySet().removeIf(entry ->
                entry.getValue().readOperations().isEmpty()
            );
        };
    }
}
