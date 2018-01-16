import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.URL;

import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

@Slf4j
public class SearchTest {

    @Test
    @SneakyThrows
    public void someTest() {
        String webHost = System.getProperty("web.host");
        String webPort = System.getProperty("web.tcp.8080");
        String chromeHost = System.getProperty("chrome.host");
        int chromePort = parseInt(System.getProperty("chrome.tcp.4444"), 10);

        RemoteWebDriver driver = new RemoteWebDriver(
            new URL("http", chromeHost, chromePort, "/wd/hub"),
            new ChromeOptions()
        );

        driver.get(format("http://%s:%s/eidc/documents", webHost, webPort));

        assertThat("some", driver.getTitle(), equalTo("Search - Environmental Information Data Centre"));
    }
}
