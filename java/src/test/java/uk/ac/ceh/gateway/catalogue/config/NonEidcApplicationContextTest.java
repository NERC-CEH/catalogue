package uk.ac.ceh.gateway.catalogue.config;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import uk.ac.ceh.gateway.catalogue.upload.simple.UploadController;

import static org.junit.Assert.assertNotNull;

@ActiveProfiles({"auth:crowd", "upload:simple"})
@TestPropertySource
@ContextConfiguration(classes = WebConfig.class)
@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public class NonEidcApplicationContextTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test(expected = NoSuchBeanDefinitionException.class)
    public void hubbubUploadBeansPresent() {
        assertNotNull(applicationContext.getBean(UploadController.class));
        // No upload.UploadController has been created, will throw NoSuchBeanDefinitionException
        applicationContext.getBean(uk.ac.ceh.gateway.catalogue.upload.hubbub.UploadController.class);
    }
}
