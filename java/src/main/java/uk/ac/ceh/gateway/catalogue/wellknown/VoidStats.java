package uk.ac.ceh.gateway.catalogue.wellknown;

import java.util.Map;

public record VoidStats(long entities, long triples, Map<String, Long> classEntityCounts) {}
