package uk.ac.ceh.gateway.catalogue.graph;

import java.util.Map;

import lombok.Data;

@Data
public class Style {
  private String selector;
  private Map<String, Object> style;
}
