import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.Rule;
import org.junit.Test;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testcontainers.containers.BrowserWebDriverContainer;

import java.util.Map;

import static java.lang.String.format;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@Slf4j
public class EachTest {

    @Rule
    public BrowserWebDriverContainer chrome = new BrowserWebDriverContainer()
        .withDesiredCapabilities(DesiredCapabilities.chrome());

    @Test
    @SneakyThrows
    public void individualDocument() {
        //given
        String expectedTitle = "Woodlands survey flora data 1971-2001";
        RemoteWebDriver driver = chrome.getWebDriver();

        String web_host = System.getenv("WEB_HOST");
        web_host = "172.21.0.1:8080";

        Map<String, String> envs = System.getenv();

        log.info(envs.toString());

        if (web_host == null && web_host.isEmpty()) {
            fail("No web_host found in environment");
        }

        String docUrl = format(
            "http://%s/documents/2d023ce9-6dbe-4b4f-a0cd-34768e1455ae",
            web_host
        );

        log.info(docUrl);

        //when
        driver.get(docUrl);

        //then
        String actualText = driver.findElementById("document-title").getText();
        assertThat("Title should be: Woodlands survey flora data 1971-2001", actualText, containsString(expectedTitle));

    }
}
