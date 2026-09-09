package uk.ac.ceh.gateway.catalogue.mcp;

import io.modelcontextprotocol.server.McpSyncServer;
import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.servlet.function.RouterFunction;
import uk.ac.ceh.gateway.catalogue.CatalogueWebTest;

import java.util.Arrays;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The counterpart to {@link McpServerContextTest}: MCP must be absent from every profile that
 * does not ask for it.
 * <p>
 * {@code @Profile("mcp-server")} on {@link McpServerConfig} and {@link CatalogueMcpTools} was
 * never enough on its own. {@code spring-ai-starter-mcp-server-webmvc} is an unconditional
 * dependency and its auto-configuration defaults {@code spring.ai.mcp.server.enabled} to true,
 * so a live {@code mcpSyncServer} and an HTTP transport were built in every profile - the
 * profile only ever decided whether our own tools were attached to a server that was running
 * regardless. Two properties now close that, and this pins it: the failure mode is silent, and
 * a Spring AI upgrade introducing another always-on MCP auto-configuration would reopen it.
 *
 * @see McpServerContextTest for the profile-on side, which stops this being satisfied by
 *      disabling MCP outright
 */
@ActiveProfiles({"auth-crowd", "server-eidc", "search-basic"})
@CatalogueWebTest
@DisplayName("MCP server without the mcp-server profile")
class McpServerDisabledContextTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    @DisplayName("No MCP server is built")
    void noMcpServerIsBuilt() {
        assertThat(applicationContext.getBeanNamesForType(McpSyncServer.class)).isEmpty();
    }

    @Test
    @DisplayName("No MCP tools are exposed")
    void noMcpToolsAreExposed() {
        assertThat(applicationContext.getBeanNamesForType(CatalogueMcpTools.class)).isEmpty();
        assertThat(applicationContext.getBeanNamesForType(ToolCallbackProvider.class)).isEmpty();
    }

    /**
     * The transport is contributed as a {@code RouterFunction} bean, so an MCP endpoint would be
     * mounted without any controller of ours being involved.
     * <p>
     * Matched on the {@code @Bean} method's declaring configuration class rather than the bean
     * name: with the profile on the transport arrives as
     * {@code webMvcStreamableServerRouterFunction}, whose name contains no hint of MCP at all - a
     * name filter here silently passed whatever the configuration did, and would go on doing so if
     * the protocol changed again. {@code RouterFunction} itself cannot be asserted absent, because
     * any functional route the application later registers would share the type.
     */
    @Test
    @DisplayName("No MCP HTTP transport is mounted")
    void noMcpHttpTransportIsMounted() {
        val beanFactory = applicationContext.getAutowireCapableBeanFactory();
        val mcpRoutes = Arrays.stream(applicationContext.getBeanNamesForType(RouterFunction.class))
            .filter(name -> beanFactory instanceof ConfigurableListableBeanFactory configurable
                && Objects.toString(configurable.getBeanDefinition(name).getFactoryBeanName(), "")
                    .contains(".mcp."))
            .toList();
        assertThat(mcpRoutes).isEmpty();
    }

    /**
     * A catch-all over bean names, so the guards above cannot be quietly outflanked by a new
     * auto-configuration in a later Spring AI. Deliberately broad: nothing MCP shaped belongs in
     * a context that has not asked for MCP, not even an annotation scanner with nothing to scan.
     */
    @Test
    @DisplayName("Nothing MCP related is registered at all")
    void nothingMcpRelatedIsRegistered() {
        val mcpBeans = Arrays.stream(applicationContext.getBeanDefinitionNames())
            .filter(name -> name.toLowerCase().contains("mcp"))
            .sorted()
            .toList();
        assertThat(mcpBeans).isEmpty();
    }
}
