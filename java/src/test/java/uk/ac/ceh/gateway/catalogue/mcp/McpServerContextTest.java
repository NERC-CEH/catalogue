package uk.ac.ceh.gateway.catalogue.mcp;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import uk.ac.ceh.gateway.catalogue.CatalogueWebTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The "mcp-server" profile had no context test, so nothing proved its beans could actually be
 * constructed — the tools are only reachable over MCP, and unit tests build them by hand with mocks.
 * That gap hid a constructor dependency the application context cannot satisfy.
 */
@ActiveProfiles({"auth-crowd", "server-eidc", "search-basic", "mcp-server"})
@CatalogueWebTest
@DisplayName("MCP server context")
class McpServerContextTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    @DisplayName("Tools and the callback provider that exposes them are wired")
    void mcpToolsAreWired() {
        assertThat(applicationContext.getBean(CatalogueMcpTools.class)).isNotNull();
        assertThat(applicationContext.getBean(ToolCallbackProvider.class)).isNotNull();
    }
}
