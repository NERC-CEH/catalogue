package uk.ac.ceh.gateway.catalogue;

import jakarta.servlet.Filter;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.AbstractFilterRegistrationBean;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Comparator;

public abstract class AbstractMvcTest {

    @Autowired
    protected WebApplicationContext wac;

    protected MockMvc mvc;

    @BeforeEach
    void setUpMockMvc() {
        var builder = MockMvcBuilders.webAppContextSetup(wac)
            .apply(SecurityMockMvcConfigurers.springSecurity());
        wac.getBeansOfType(AbstractFilterRegistrationBean.class).values().stream()
            .sorted(Comparator.comparingInt(AbstractFilterRegistrationBean::getOrder))
            .forEach(frb -> {
                Filter filter = frb.getFilter();
                @SuppressWarnings("unchecked")
                String[] patterns = ((java.util.Collection<String>) frb.getUrlPatterns()).toArray(new String[0]);
                if (patterns.length > 0) {
                    assert filter != null;
                    builder.addFilter(filter, patterns);
                } else {
                    assert filter != null;
                    builder.addFilters(filter);
                }
            });
        mvc = builder.build();
    }
}
