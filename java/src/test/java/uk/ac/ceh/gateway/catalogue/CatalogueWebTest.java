package uk.ac.ceh.gateway.catalogue;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@TestPropertySource("classpath:uk/ac/ceh/gateway/catalogue/test.properties")
@SpringBootTest()
public @interface CatalogueWebTest {
}
