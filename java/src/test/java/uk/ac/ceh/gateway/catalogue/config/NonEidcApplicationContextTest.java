package uk.ac.ceh.gateway.catalogue.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import uk.ac.ceh.gateway.catalogue.upload.simple.UploadController;

import static org.junit.Assert.assertNotNull;

@ActiveProfiles({"auth:crowd", "upload:simple"})
@TestPropertySource
@ContextConfiguration(classes = WebConfig.class)
@WebAppConfiguration
@ExtendWith(SpringExtension.class)
public class NonEidcApplicationContextTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void hubbubUploadBeansPresent() {
        Assertions.assertThrows(NoSuchBeanDefinitionException.class, () -> {
            assertNotNull(applicationContext.getBean(UploadController.class));
            // No upload.UploadController has been created, will throw NoSuchBeanDefinitionException
            applicationContext.getBean(uk.ac.ceh.gateway.catalogue.upload.hubbub.UploadController.class);
        });
    }
}
