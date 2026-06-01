package uk.ac.ceh.gateway.catalogue.config;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import uk.ac.ceh.gateway.catalogue.mcp.CatalogueMcpTools;

/**
 * Activates the Spring AI MCP server when the "mcp-server" profile is active.
 * Spring AI's WebMVC auto-configuration wires SSE transport at /mcp/sse and /mcp/messages.
 * Properties in application-mcp-server.properties set the server name and version.
 */
@Configuration
@Profile("mcp-server")
public class McpServerConfig {

    @Bean
    public ToolCallbackProvider catalogueToolCallbackProvider(CatalogueMcpTools tools) {
        return MethodToolCallbackProvider.builder().toolObjects(tools).build();
    }
}
