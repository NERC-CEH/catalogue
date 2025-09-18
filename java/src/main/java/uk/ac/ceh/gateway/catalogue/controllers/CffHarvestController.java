package uk.ac.ceh.gateway.catalogue.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.services.CffHarvestService;

import java.util.Map;

@RestController
@RequestMapping("/documents")
@RequiredArgsConstructor
public class CffHarvestController {

    private final CffHarvestService harvestService;
    private final DocumentRepository documentRepository;

    @PreAuthorize("@permission.userCanCreate(#catalogue)")
    @PostMapping(value = "/harvestCff", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> harvestFromCff(
        @ActiveUser CatalogueUser user,
        @RequestBody Map<String, String> body,
        @RequestParam("catalogue") String catalogue
    ) {

        String cffUrl = body.get("url");
        if (cffUrl == null || cffUrl.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Missing 'url' field in request body"
            ));
        }

        try {
            GeminiDocument doc = harvestService.createGeminiFromCff(cffUrl.trim());

            MetadataDocument saved = documentRepository.saveNew(
                user,
                doc,
                catalogue,
                "new Gemini Document created from GitHub CFF"
            );

            return ResponseEntity.ok(Map.of("id", saved.getId()));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Invalid CFF file: " + e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "Unexpected error while harvesting CFF: " + e.getMessage()
            ));
        }
    }
}


