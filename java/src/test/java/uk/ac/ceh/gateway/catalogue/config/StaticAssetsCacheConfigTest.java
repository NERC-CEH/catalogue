package uk.ac.ceh.gateway.catalogue.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("StaticAssetsCacheConfig")
@SpringJUnitWebConfig(StaticAssetsCacheConfigTest.Config.class)
class StaticAssetsCacheConfigTest {
    private static final Path STATIC;

    static {
        try {
            STATIC = Files.createTempDirectory("static");
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Configuration
    @EnableWebMvc
    @Import(StaticAssetsCacheConfig.class)
    static class Config {}

    @DynamicPropertySource
    static void staticLocation(DynamicPropertyRegistry registry) {
        // Without the trailing slash, as docker compose sets it
        registry.add("spring.web.resources.static-locations", () -> "file:" + STATIC);
    }

    @Autowired private WebApplicationContext context;
    private MockMvc mvc;

    @BeforeEach
    void setUp() throws IOException {
        for (String directory : StaticAssetsCacheConfig.HASHED) {
            Files.createDirectories(STATIC.resolve(directory));
        }
        Files.writeString(STATIC.resolve("scripts/main.5e6f7a8b.js"), "console.log('main')");
        Files.writeString(STATIC.resolve("css/base.9c0d1e2f.css"), "body{}");
        Files.writeString(STATIC.resolve("assets/fa-solid-900.93f719f4.woff2"), "font");
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    @DisplayName("lets browsers keep webpack's hashed scripts, stylesheets and assets for a year")
    void cachesHashedFilesForAYear() throws Exception {
        for (String path : new String[]{
            "/static/scripts/main.5e6f7a8b.js", "/static/css/base.9c0d1e2f.css", "/static/assets/fa-solid-900.93f719f4.woff2"
        }) {
            mvc.perform(get(path))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "max-age=31536000, public, immutable"));
        }
    }

    @Test
    @DisplayName("serves the file from its directory in the static location")
    void servesTheFile() throws Exception {
        mvc.perform(get("/static/scripts/main.5e6f7a8b.js"))
            .andExpect(content().string("console.log('main')"));
    }

    @Test
    @DisplayName("doesn't reach outside those directories")
    void staysInItsDirectories() throws Exception {
        Files.writeString(STATIC.resolve("manifest.json"), "{}");

        mvc.perform(get("/static/scripts/../manifest.json")).andExpect(status().isNotFound());
        mvc.perform(get("/static/manifest.json")).andExpect(status().isNotFound());
    }
}
