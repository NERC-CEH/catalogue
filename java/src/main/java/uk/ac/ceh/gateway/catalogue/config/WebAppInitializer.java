package uk.ac.ceh.gateway.catalogue.config;

import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;

public class WebAppInitializer implements WebApplicationInitializer {

    @Override
    public void onStartup(ServletContext servletContext) {
        AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext();
        rootContext.register(WebConfig.class);
        
        servletContext.addListener(new ContextLoaderListener(rootContext));
        
        DelegatingFilterProxy secFilter = new DelegatingFilterProxy("springSecurityFilterChain");
        FilterRegistration.Dynamic securityFilter = servletContext.addFilter("securityFilter", secFilter);
        securityFilter.addMappingForUrlPatterns(null, true, "/*");
        
        
        ServletRegistration.Dynamic dispatcher = servletContext.addServlet("dispatcher", new DispatcherServlet(rootContext));
        dispatcher.setLoadOnStartup(1);
        dispatcher.addMapping("/");    
    }
}