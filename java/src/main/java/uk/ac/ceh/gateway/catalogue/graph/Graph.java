package uk.ac.ceh.gateway.catalogue.graph;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Graph {
  private List<Node> elements;
  private Map<String, Object> layout;
  private List<Style> style;
}