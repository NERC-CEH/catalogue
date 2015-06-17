package uk.ac.ceh.gateway.catalogue.events;

import com.google.common.eventbus.EventBus;
import java.util.Collection;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * The following spring bean registers the components which are annotated as 
 * subscribers onto the event bus
 * @author Christopher Johnson
 */
@Component
@Slf4j
public class EventBusRegistry {
    
    @Autowired ApplicationContext context;
    @Autowired EventBus bus;
    
    @PostConstruct
    public void registerSubscribers() {
        Collection<Object> subscribers = context.getBeansWithAnnotation(Subscriber.class).values();
        subscribers.stream().forEach((subscriber) -> {
            log.info("Registering: {} as an event subscriber", subscriber.getClass().getSimpleName());
            bus.register(subscriber);
        });
    }
}
