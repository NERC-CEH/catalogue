package uk.ac.ceh.gateway.catalogue.quality;


import lombok.NonNull;
import lombok.Value;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static uk.ac.ceh.gateway.catalogue.quality.Results.Severity.*;


@Value
public class Results {
    List<MetadataCheck> problems;
    String id, message;

    public Results(@NonNull List<MetadataCheck> problems, @NonNull String id) {
        this(problems, id, "");
    }

    public Results(@NonNull List<MetadataCheck> problems, @NonNull String id, @NonNull String message) {
        this.problems = problems.stream()
                .sorted(Comparator.comparingInt(o -> o.getSeverity().priority))
                .collect(Collectors.toList());
        this.id = id;
        this.message = message;
    }

    public long getErrors() {
        return problems.stream()
                .filter(m -> ERROR.equals(m.getSeverity()))
                .count();
    }

    public long getWarnings() {
        return problems.stream()
                .filter(m -> WARNING.equals(m.getSeverity()))
                .count();
    }

    public long getInfo() {
        return problems.stream()
                .filter(m -> INFO.equals(m.getSeverity()))
                .count();
    }

    public enum Severity {
        ERROR(1), WARNING(2), INFO(3);

        final int priority;

        Severity(int priority) {
            this.priority = priority;
        }
    }


}
