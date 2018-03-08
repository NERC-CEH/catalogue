package uk.ac.ceh.gateway.catalogue.graph;

import com.google.common.collect.Lists;

import lombok.val;

public class NodeBuilder {
  private GraphBuilder graphBuilder;
  private Node node;

  public NodeBuilder(GraphBuilder graphBuilder, String id) {
    this.graphBuilder = graphBuilder;
    node = new Node();
    node.getData().put("id", id);
  }

  public NodeBuilder data(String key, Object value) {
    node.getData().put(key, value);
    return this;
  }

  public NodeBuilder label(String label) {
    node.getData().put("label", label);
    return this;
  }

  public NodeBuilder source(String id) {
    node.getData().put("source", id);
    return this;
  }

  public NodeBuilder target(String id) {
    node.getData().put("target", id);
    return this;
  }

  public NodeBuilder position(int x, int y) {
    node.setPosition(new Position(x, y));
    return this;
  }

  public NodeBuilder selected(boolean selected) {
    this.node.setSelected(selected);
    return this;
  }

  public NodeBuilder selectable(boolean selectable) {
    this.node.setSelectable(selectable);
    return this;
  }

  public NodeBuilder locked(boolean locked) {
    this.node.setLocked(locked);
    return this;
  }

  public NodeBuilder grabbale(boolean grabbale) {
    this.node.setGrabbale(grabbale);
    return this;
  }

  public NodeBuilder classes(String classes) {
    this.node.setClasses(classes);
    return this;
  }

  public NodeBuilder classes(String... classes) {
    this.node.setClasses(String.join(" ", classes));
    return this;
  }

  public GraphBuilder add() {
    val elements = graphBuilder.graph.getElements();
    if (elements == null)
      graphBuilder.graph.setElements(Lists.newArrayList());
    graphBuilder.graph.getElements().add(node);
    return graphBuilder;
  }
}
