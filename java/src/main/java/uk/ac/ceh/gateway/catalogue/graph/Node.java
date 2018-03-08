package uk.ac.ceh.gateway.catalogue.graph;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.collect.Maps;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Node {
  private Map<String, Object> data;
  private Position position;
  private Boolean selected;
  private Boolean selectable;
  private Boolean locked;
  private Boolean grabbale;
  private String classes;

  public Node() {
    data = Maps.newHashMap();
  }
}
