package uk.ac.ceh.gateway.catalogue.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import uk.ac.ceh.gateway.catalogue.services.KeywordSuggestionsService;

import java.util.*;

@Slf4j
@RestController
@Profile("keyword-suggestions")
public class KeywordSuggestionsController {
    private final KeywordSuggestionsService service;

    public KeywordSuggestionsController(KeywordSuggestionsService service) {
        this.service = service;
        log.info("Creating");
    }

    @GetMapping("/documents/{file}/suggestKeywords")
    @PreAuthorize("@permission.userCanEdit(#file)")
    public List<KeywordSuggestionsService.Suggestion> getSuggestions(@PathVariable String file) {
        return service.getSuggestions(file);
    }
}
