package web;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testcontainers.containers.BrowserWebDriverContainer;
import org.testcontainers.containers.DockerComposeContainer;

import java.io.File;

import static java.lang.String.format;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

@Slf4j
public class SeleniumContainerTest {
    private static String WEB_SERVICE = "web_1";
    private static int WEB_PORT = 8080;

    @ClassRule
    public static DockerComposeContainer environment = new DockerComposeContainer(new File("../docker-compose.yml"))
        .withExposedService(WEB_SERVICE, WEB_PORT);


    @Rule
    public BrowserWebDriverContainer chrome = new BrowserWebDriverContainer()
                                                .withDesiredCapabilities(DesiredCapabilities.chrome());

    @Test
    @SneakyThrows
    public void simpleTest() {
        RemoteWebDriver driver = chrome.getWebDriver();

        String webUrl = environment.getServiceHost(WEB_SERVICE, WEB_PORT)
                        + ":" +
                        environment.getServicePort(WEB_SERVICE, WEB_PORT);

        String eidcCatalogue = format("%s/eidc/documents", webUrl);
        log.info("web url: {}", eidcCatalogue);

        driver.get(eidcCatalogue);
        File screenshot = driver.getScreenshotAs(OutputType.FILE);
        FileUtils.copyFile(screenshot, new File("/tmp/screenshot.png"));
        WebElement searchInput = driver.findElementByName("term");

        searchInput.sendKeys("land cover map");
        searchInput.submit();

        WebElement otherPage = driver.findElementByPartialLinkText("Land Cover Map");
        otherPage.click();

        String expectedText = driver.findElementById("document-description").getText();

        assertThat("Should have found LCM2015", expectedText, containsString("LCM"));
    }
}
