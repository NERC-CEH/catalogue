package uk.ac.ceh.gateway.catalogue.config;

import uk.ac.ceh.components.vocab.Vocabulary;

import java.util.List;

public interface VocabularyConfig {
    List<Vocabulary> keywords();

    List<Vocabulary> codelists();
}
