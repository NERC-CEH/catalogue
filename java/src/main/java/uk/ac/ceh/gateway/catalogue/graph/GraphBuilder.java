package uk.ac.ceh.gateway.catalogue.graph;

import java.util.Map;

import com.google.common.collect.Maps;

import org.apache.jena.ext.com.google.common.collect.Lists;

import lombok.val;

public class GraphBuilder {
  Graph graph;

  public GraphBuilder() {
    this.graph = new Graph();
  }

  public GraphBuilder withElement(Node node) {
    val elements = this.graph.getElements();
    if (elements == null)
      this.graph.setElements(Lists.newArrayList());
    this.graph.getElements().add(node);
    return this;
  }

  public GraphBuilder withElements(Node... nodes) {
    val elements = this.graph.getElements();
    if (elements == null)
      this.graph.setElements(Lists.newArrayList());
    this.graph.getElements().addAll(Lists.newArrayList(nodes));
    return this;
  }

  public GraphBuilder withStyle(Style style) {
    val graphStyle = this.graph.getStyle();
    if (graphStyle == null)
      this.graph.setStyle(Lists.newArrayList());
    this.graph.getStyle().add(style);
    return this;
  }

  public GraphBuilder withStyles(Style... styles) {
    val graphStyle = this.graph.getStyle();
    if (graphStyle == null)
      this.graph.setStyle(Lists.newArrayList());
    this.graph.getStyle().addAll(Lists.newArrayList(styles));
    return this;
  }

  public GraphBuilder withLayout(Map<String, Object> layout) {
    this.graph.setLayout(Maps.newHashMap());
    return this;
  }

  public NodeBuilder node(String id) {
    return new NodeBuilder(this, id);
  }

  public StyleBuilder style(String selector) {
    return new StyleBuilder(this, selector);
  }

  public LayoutBuilder layout(String name) {
    return new LayoutBuilder(this, name);
  }

  public LayoutBuilder nullLayout() {
    return new LayoutBuilder(this, "null");
  }

  public LayoutBuilder randomLayout() {
    return new LayoutBuilder(this, "random");
  }

  public LayoutBuilder presetLayout() {
    return new LayoutBuilder(this, "preset");
  }

  public LayoutBuilder gridLayout() {
    return new LayoutBuilder(this, "grid");
  }

  public LayoutBuilder circleLayout() {
    return new LayoutBuilder(this, "circle");
  }

  public LayoutBuilder concentricLayout() {
    return new LayoutBuilder(this, "concentric");
  }

  public LayoutBuilder breadthFirstLayout() {
    return new LayoutBuilder(this, "breadthFirst");
  }

  public LayoutBuilder coseLayout() {
    return new LayoutBuilder(this, "cose");
  }

  public LayoutBuilder coseBilkentLayout() {
    return new LayoutBuilder(this, "coseBilkent");
  }

  public Graph build() {
    return graph;
  }
}
