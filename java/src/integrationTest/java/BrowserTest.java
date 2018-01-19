import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.URL;

import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@Slf4j
public class BrowserTest {
    private final RemoteWebDriver driver;
    private final String webHost;
    private final String webPort;

    @SneakyThrows
    public BrowserTest() {
        driver = new RemoteWebDriver(
            new URL(
                "http",
                System.getProperty("chrome.host"),
                parseInt(System.getProperty("chrome.tcp.4444"), 10),
                "/wd/hub"
            ),
            new ChromeOptions()
        );
        webHost = System.getProperty("web.host");
        webPort = System.getProperty("web.tcp.8080");
    }

    @Test
    @SneakyThrows
    public void getIndividualDocument() {
        //given
        String expectedTitle = "Woodlands survey flora data 1971-2001";

        //when
        driver.get(format("http://%s:%s/documents/2d023ce9-6dbe-4b4f-a0cd-34768e1455ae", webHost, webPort));

        //then
        String actualText = driver.findElementById("document-title").getText();
        assertThat("Title should be: Woodlands survey flora data 1971-2001", actualText, containsString(expectedTitle));

    }

    @Test
    @SneakyThrows
    public void getPublicationPage() {
        //given
        String expectedTitle = "Publication - Environmental Information Data Centre";

        //when
        driver.get(format("http://%s:%s/documents/2d023ce9-6dbe-4b4f-a0cd-34768e1455ae/publication", webHost, webPort));

        //then
        String actualTitle = driver.getTitle();
        assertThat("Page title should be correct", actualTitle, equalTo(expectedTitle));

    }

    @Test
    @SneakyThrows
    public void getPermissionPage() {
        //given
        String expectedTitle = "Permissions - Environmental Information Data Centre";

        //when
        driver.get(format("http://%s:%s/documents/2d023ce9-6dbe-4b4f-a0cd-34768e1455ae/permission", webHost, webPort));

        //then
        String actualTitle = driver.getTitle();
        assertThat("Page title should be correct", actualTitle, equalTo(expectedTitle));

    }


    @Test
    @SneakyThrows
    public void searchResults() {
        //given
        int minRecords = 0;

        //when
        driver.get(format("http://%s:%s/eidc/documents", webHost, webPort));

        //then
        int numRecords = parseInt(driver.findElementById("num-records").getText(), 10);
        assertThat("Should have found documents", numRecords, greaterThan(minRecords));
    }
}
