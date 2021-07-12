package uk.ac.ceh.gateway.catalogue.vocabularies;

public class KeywordVocabularyException extends RuntimeException{

    public KeywordVocabularyException(String message) {
        super(message);
    }

    public KeywordVocabularyException(Exception exception) {
        super(exception);
    }

    public KeywordVocabularyException(String s, Exception ex) {
    }
}
