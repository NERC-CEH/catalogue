package uk.ac.ceh.gateway.catalogue.mcp;

import io.modelcontextprotocol.server.McpSyncServer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.servlet.function.RouterFunction;
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

    /**
     * The server itself is switched on by {@code spring.ai.mcp.server.enabled} in
     * application-mcp-server.properties, overriding the false in application.properties that keeps
     * MCP out of every other profile. Without this assertion that pair could be satisfied by
     * turning MCP off everywhere, which would leave the tools above wired to nothing.
     *
     * @see McpServerDisabledContextTest
     */
    @Test
    @DisplayName("The server the tools attach to is actually running")
    void mcpServerIsBuilt() {
        assertThat(applicationContext.getBean(McpSyncServer.class)).isNotNull();
    }

    /**
     * Pins which transport is mounted, because Spring AI will silently mount a different one.
     * <p>
     * {@code McpServerProperties.protocol} has a field default of {@code STREAMABLE}, but the
     * transport is selected by {@code @ConditionalOnProperty} against the raw Environment, which
     * never sees that default - and only the SSE condition carries {@code matchIfMissing=true}.
     * Leaving {@code spring.ai.mcp.server.protocol} unset therefore mounted SSE, the transport
     * deprecated in Spring AI 2.0, in spite of the documented default. The property is now set
     * explicitly in application-mcp-server.properties; this fails if that line is dropped as
     * redundant, which reading the Spring AI documentation alone would suggest it is.
     */
    @Test
    @DisplayName("Streamable HTTP is the mounted transport, not the deprecated SSE one")
    void theMountedTransportIsStreamableHttp() {
        assertThat(applicationContext.getBeanNamesForType(RouterFunction.class))
            .contains("webMvcStreamableServerRouterFunction")
            .doesNotContain("webMvcSseServerRouterFunction");
    }
}
