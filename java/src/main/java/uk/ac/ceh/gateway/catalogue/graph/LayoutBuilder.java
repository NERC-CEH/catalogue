package uk.ac.ceh.gateway.catalogue.graph;

import com.google.common.collect.Maps;

import lombok.val;

public class LayoutBuilder {
  private GraphBuilder graphBuilder;

  public LayoutBuilder(GraphBuilder graphBuilder, String name) {
    this.graphBuilder = graphBuilder;
    val layout = this.graphBuilder.graph.getLayout();
    if (layout == null)
      this.graphBuilder.graph.setLayout(Maps.newHashMap());
    this.graphBuilder.graph.getLayout().put("name", name);
  }

  private String toKebab(String value) {
    return value.toLowerCase().replace("_", "-");
  }

  public LayoutBuilder fit(boolean fit) {
    return with("fit", fit);
  }

  public LayoutBuilder padding(int size) {
    return with("padding", size);
  }

  public LayoutBuilder zoom(int size) {
    return with("zoom", size);
  }

  public LayoutBuilder pan(int size) {
    return with("pan", size);
  }

  public LayoutBuilder boundingBox(String box) {
    return with("boundingBox", box);
  }

  public LayoutBuilder animate(boolean animate) {
    return with("animate", animate);
  }

  public LayoutBuilder animationDuration(int animationDuration) {
    return with("animationDuration", animationDuration);
  }

  public LayoutBuilder animationEasing(TransitionTimingFunction easing) {
    return with("animationEasing", toKebab(easing.toString()));
  }

  public LayoutBuilder avoidOverlap(boolean avoidOverlap) {
    return with("avoidOverlap", avoidOverlap);
  }

  public LayoutBuilder avoidOverlapPadding(int size) {
    return with("avoidOverlapPadding", size);
  }

  public LayoutBuilder nodeDimensionsIncludeLabels(boolean include) {
    return with("nodeDimensionsIncludeLabels", include);
  }

  public LayoutBuilder spacingFactor(double factor) {
    return with("spacingFactor", factor);
  }

  public LayoutBuilder condense(boolean include) {
    return with("condense", include);
  }

  public LayoutBuilder rows(int factor) {
    return with("rows", factor);
  }

  public LayoutBuilder cols(int factor) {
    return with("cols", factor);
  }

  public LayoutBuilder radius(int radians) {
    return with("radius", radians);
  }

  public LayoutBuilder startAngle(double radians) {
    return with("startAngle", radians);
  }

  public LayoutBuilder sweep(double radians) {
    return with("sweep", radians);
  }

  public LayoutBuilder clockwise(boolean clockwise) {
    return with("clockwise", clockwise);
  }

  public LayoutBuilder equidistant(boolean equidistant) {
    return with("equidistant", equidistant);
  }

  public LayoutBuilder minNodeSpacing(int size) {
    return with("minNodeSpacing", size);
  }

  public LayoutBuilder height(int size) {
    return with("height", size);
  }

  public LayoutBuilder width(int size) {
    return with("width", size);
  }

  public LayoutBuilder directed(boolean directed) {
    return with("directed", directed);
  }

  public LayoutBuilder circle(boolean circle) {
    return with("circle", circle);
  }

  public LayoutBuilder roots(String... roots) {
    return with("roots", roots);
  }

  public LayoutBuilder maximalAdjustments(int size) {
    return with("maximalAdjustments", size);
  }

  public LayoutBuilder refresh(int size) {
    return with("refresh", size);
  }

  public LayoutBuilder randomize(boolean randomize) {
    return with("randomize", randomize);
  }

  public LayoutBuilder componentSpacing(int size) {
    return with("componentSpacing", size);
  }

  public LayoutBuilder nodeOverlap(int size) {
    return with("nodeOverlap", size);
  }

  public LayoutBuilder nestingFactor(double size) {
    return with("nestingFactor", size);
  }

  public LayoutBuilder gravity(double size) {
    return with("gravity", size);
  }

  public LayoutBuilder numIter(int size) {
    return with("numIter", size);
  }

  public LayoutBuilder initialTemp(int size) {
    return with("initialTemp", size);
  }

  public LayoutBuilder coolingFactor(double size) {
    return with("coolingFactor", size);
  }

  public LayoutBuilder minTemp(int size) {
    return with("minTemp", size);
  }

  public LayoutBuilder weaver(boolean weaver) {
    return with("weaver", weaver);
  }

  public LayoutBuilder idealEdgeLength(int size) {
    return with("idealEdgeLength", size);
  }

  public LayoutBuilder edgeElasticity(int size) {
    return with("edgeElasticity", size);
  }

  public LayoutBuilder tile(boolean tile) {
    return with("tile", tile);
  }

  public LayoutBuilder tilingPaddingVertical(int size) {
    return with("tilingPaddingVertical", size);
  }

  public LayoutBuilder tilingPaddingHorizontal(int size) {
    return with("tilingPaddingHorizontal", size);
  }

  public LayoutBuilder gravityRangeCompound(double size) {
    return with("gravityRangeCompound", size);
  }

  public LayoutBuilder gravityCompound(double size) {
    return with("gravityCompound", size);
  }

  public LayoutBuilder gravityRange(double size) {
    return with("gravityRange", size);
  }

  public LayoutBuilder initialEnergyOnIncremental(double size) {
    return with("initialEnergyOnIncremental", size);
  }

  public LayoutBuilder with(String key, Object value) {
    this.graphBuilder.graph.getLayout().put(key, value);
    return this;
  }

  public GraphBuilder add() {
    return graphBuilder;
  }
}
