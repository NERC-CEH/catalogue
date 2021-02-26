package uk.ac.ceh.gateway.catalogue.services;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@ToString
public class IncomingCitationService {

    public IncomingCitationService() {
    }

    public int getIncomingCitationCount(GeminiDocument geminiDocument){

        List incomingCitations = geminiDocument.getSupplemental().stream()
                .filter(s -> s.getType() == "isReferencedBy" ||
                        s.getType() == "isSupplementTo").collect(Collectors.toList());
        return incomingCitations.size();
    }
}
