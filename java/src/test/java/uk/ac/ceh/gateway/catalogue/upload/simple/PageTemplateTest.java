package uk.ac.ceh.gateway.catalogue.upload.simple;

import freemarker.template.Configuration;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.util.StreamUtils;
import uk.ac.ceh.gateway.catalogue.model.Catalogue;
import uk.ac.ceh.gateway.catalogue.services.CatalogueService;

import java.io.File;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.BDDMockito.given;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class PageTemplateTest {

    @Mock
    private CatalogueService catalogueService;

    private Map<String, Object> configureModel() {
        val files = Arrays.asList(
                new FileInfo("data1.csv"),
                new FileInfo("data2.csv"),
                new FileInfo("name with spaces.csv")
        );
        val model = new HashMap<String, Object>();
        model.put("id", "123-456-789");
        model.put("title", "Upload Test");
        model.put("files", files);
        model.put("message", new UploadController.ErrorMessage("Test error"));
        model.put("catalogueKey", "test");
        return model;
    }

    @SneakyThrows
    private Configuration configureFreemarker() {
        val config = new Configuration(Configuration.VERSION_2_3_23);
        config.setDirectoryForTemplateLoading(new File("../templates"));
        config.setSharedVariable("catalogues", catalogueService);
        return config;
    }

    @Test
    @SneakyThrows
    public void render() {
        //given
        val expected = StreamUtils.copyToString(
                getClass().getResourceAsStream("page.html"),
                StandardCharsets.UTF_8
        );
        val model = configureModel();
        val config = configureFreemarker();
        val catalogue = Catalogue.builder()
                .id("test")
                .title("Test Catalogue")
                .url("https://example.com")
                .build();

        given(catalogueService.retrieve("test")).willReturn(catalogue);

        val template = config.getTemplate("html/upload/simple/upload.ftl");
        val out = new StringWriter();

        //when
        template.process(model, out);

        //then
        assertThat(out.toString(), equalTo(expected));
    }
}