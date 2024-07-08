package uk.ac.ceh.gateway.catalogue.quality;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@Qualifier("monitoring")
public class MonitoringQualityService implements MetadataQualityService {
    @Override
    public Results check(String id) {
        return null;
    }
}
