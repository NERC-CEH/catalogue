package uk.ac.ceh.gateway.catalogue.graph;

import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import lombok.val;

public class StyleBuilder {
  private GraphBuilder graphBuilder;
  private Style style;

  public StyleBuilder(GraphBuilder graphBuilder, String selector) {
    this.graphBuilder = graphBuilder;
    style = new Style();
    style.setSelector(selector);
    style.setStyle(Maps.newHashMap());
  }

  private String toKebab(String value) {
    return value.toLowerCase().replace("_", "-");
  }

  public StyleBuilder width(int size) {
    style.getStyle().put("width", size);
    return this;
  }

  public StyleBuilder widthOfLabel() {
    style.getStyle().put("width", "label");
    return this;
  }

  public StyleBuilder height(int size) {
    style.getStyle().put("height", size);
    return this;
  }

  public StyleBuilder heightOfLabel() {
    style.getStyle().put("height", "label");
    return this;
  }

  public StyleBuilder shape(Shape shape) {
    style.getStyle().put("shape", toKebab(shape.toString()));
    return this;
  }

  public StyleBuilder polygon(String points) {
    style.getStyle().put("shape", "polygon");
    style.getStyle().put("shape-polygon-points", points);
    return this;
  }

  public StyleBuilder backgroundColor(String color) {
    style.getStyle().put("background-color", color);
    return this;
  }

  public StyleBuilder backgroundBlacken(double dbl) {
    style.getStyle().put("background-blacked", dbl);
    return this;
  }

  public StyleBuilder backgroundOpactity(double dbl) {
    style.getStyle().put("background-opacity", dbl);
    return this;
  }

  public StyleBuilder borderWidth(int size) {
    style.getStyle().put("border-width", size);
    return this;
  }

  public StyleBuilder borderStyle(BorderStyle borderStyle) {
    style.getStyle().put("border-style", toKebab(borderStyle.toString()));
    return this;
  }

  public StyleBuilder borderColor(String color) {
    style.getStyle().put("border-color", color);
    return this;
  }

  public StyleBuilder borderOpactity(double dbl) {
    style.getStyle().put("border-opactity", dbl);
    return this;
  }

  public StyleBuilder padding(int size) {
    style.getStyle().put("padding", size);
    return this;
  }

  public StyleBuilder paddingRelativeTo(PaddingRelativeTo paddingRelativeTo) {
    style.getStyle().put("padding-relative-to", toKebab(paddingRelativeTo.toString()));
    return this;
  }

  public StyleBuilder compoundSizingWrtLabels(CompoundSizingWrtLabels compoundSizingWrtLabels) {
    style.getStyle().put("compound-sizing-wrt-labels", toKebab(compoundSizingWrtLabels.toString()));
    return this;
  }

  public StyleBuilder minWidth(int size) {
    style.getStyle().put("min-width", size);
    return this;
  }

  public StyleBuilder minWidthBiasLeft(int size) {
    style.getStyle().put("min-width-bias-left", size);
    return this;
  }

  public StyleBuilder minWidthBiasRight(int size) {
    style.getStyle().put("min-width-bias-right", size);
    return this;
  }

  public StyleBuilder minHeight(int size) {
    style.getStyle().put("min-height", size);
    return this;
  }

  public StyleBuilder minHeightBiasTop(int size) {
    style.getStyle().put("min-height-bias-top", size);
    return this;
  }

  public StyleBuilder minHeightBiasBottom(int size) {
    style.getStyle().put("min-height-bias-bottom", size);
    return this;
  }

  public StyleBuilder backgroundImage(String... images) {
    style.getStyle().put("background-image", images);
    return this;
  }

  public StyleBuilder backgroundImageCrossorigin(
      BackgroundImageCrossorigin backgroundImageCrossorigin) {
    style.getStyle().put(
        "background-image-crossorigin", toKebab(backgroundImageCrossorigin.toString()));
    return this;
  }

  public StyleBuilder backgroundImageOpactity(double opacity) {
    style.getStyle().put("background-image-opactity", opacity);
    return this;
  }

  public StyleBuilder backgroundWidth(int size) {
    style.getStyle().put("background-width", size);
    return this;
  }

  public StyleBuilder backgroundHeight(int size) {
    style.getStyle().put("background-height", size);
    return this;
  }

  public StyleBuilder backgroundFit(BackgroundFit fitWidth, BackgroundFit fitHeight) {
    style.getStyle().put(
        "background-fit", toKebab(fitWidth.toString()) + " " + toKebab(fitHeight.toString()));
    return this;
  }

  public StyleBuilder backgroundFit(BackgroundFit fit) {
    style.getStyle().put("background-fit", toKebab(fit.toString()));
    return this;
  }

  public StyleBuilder backgroundRepeat(BackgroundRepeat repeat) {
    style.getStyle().put("background-repeat", toKebab(repeat.toString()));
    return this;
  }

  public StyleBuilder backgroundPositionX(int x) {
    style.getStyle().put("background-position-x", x);
    return this;
  }

  public StyleBuilder backgroundPositionY(int y) {
    style.getStyle().put("background-position-y", y);
    return this;
  }

  public StyleBuilder backgroundWidthRelativeTo(BackgroundRelativeTo backgroundRelativeTo) {
    style.getStyle().put("background-width-relative-to", toKebab(backgroundRelativeTo.toString()));
    return this;
  }

  public StyleBuilder backgroundHeightRelativeTo(BackgroundRelativeTo backgroundRelativeTo) {
    style.getStyle().put("background-height-relative-to", toKebab(backgroundRelativeTo.toString()));
    return this;
  }

  public StyleBuilder backgroundClip(BackgroundClip backgroundClip) {
    style.getStyle().put("background-clip", toKebab(backgroundClip.toString()));
    return this;
  }

  public StyleBuilder pieSize(int size) {
    style.getStyle().put("pie-size", size);
    return this;
  }

  public StyleBuilder pieBackgroundColor(int i, String color) {
    style.getStyle().put(String.format("pie-%d-background-color", i), color);
    return this;
  }

  public StyleBuilder pieBackgroundSize(int i, int size) {
    style.getStyle().put(String.format("pie-%d-background-size", i), size);
    return this;
  }

  public StyleBuilder pieBackgroundOpactity(int i, int opacity) {
    style.getStyle().put(String.format("pie-%d-background-opacity", i), opacity);
    return this;
  }

  public StyleBuilder curveStyle(CurveStyle curveStyle) {
    style.getStyle().put("curve-style", toKebab(curveStyle.toString()));
    return this;
  }

  public StyleBuilder lineColor(String color) {
    style.getStyle().put("line-color", color);
    return this;
  }

  public StyleBuilder lineStyle(LineStyle lineStyle) {
    style.getStyle().put("line-style", toKebab(lineStyle.toString()));
    return this;
  }

  public StyleBuilder controlPointStepSize(int size) {
    style.getStyle().put("control-point-step-size", size);
    return this;
  }

  public StyleBuilder controlPointStepDistance(int size) {
    style.getStyle().put("control-point-step-distance", size);
    return this;
  }

  public StyleBuilder controlPointStepWeight(int size) {
    style.getStyle().put("control-point-step-weight", size);
    return this;
  }

  public StyleBuilder loopDirection(String angle) {
    style.getStyle().put("loop-direction", angle);
    return this;
  }

  public StyleBuilder loopSweep(String angle) {
    style.getStyle().put("loop-sweep", angle);
    return this;
  }

  public StyleBuilder controlPointDistances(int... distances) {
    style.getStyle().put(
        "control-point-distances", StringUtils.join(ArrayUtils.toObject(distances), " "));
    return this;
  }

  public StyleBuilder controlPointWeights(int... weights) {
    style.getStyle().put(
        "control-point-weights", StringUtils.join(ArrayUtils.toObject(weights), " "));
    return this;
  }

  public StyleBuilder edgeDistances(EdgeDistance... edgeDistances) {
    val distances = Lists.newArrayList(edgeDistances)
                        .stream()
                        .map(edgeDistance -> toKebab(edgeDistance.toString()))
                        .collect(Collectors.toList());
    style.getStyle().put("edge-distances", String.join(" ", distances));
    return this;
  }

  public StyleBuilder segmentPointDistances(int... distances) {
    style.getStyle().put(
        "segment-point-distances", StringUtils.join(ArrayUtils.toObject(distances), " "));
    return this;
  }

  public StyleBuilder segmentPointWeights(int... weights) {
    style.getStyle().put(
        "segment-point-weights", StringUtils.join(ArrayUtils.toObject(weights), " "));
    return this;
  }

  public StyleBuilder haystackRadius(double radius) {
    style.getStyle().put("haystack-radius", radius);
    return this;
  }

  public StyleBuilder arrowColor(ArrowPosition pos, String color) {
    val position = toKebab(pos.toString());
    style.getStyle().put(String.format("%s-arrow-color", position), color);
    return this;
  }

  public StyleBuilder arrowShape(ArrowPosition pos, ArrowShape shape) {
    val position = toKebab(pos.toString());
    style.getStyle().put(String.format("%s-arrow-shape", position), toKebab(shape.toString()));
    return this;
  }

  public StyleBuilder arrowFill(ArrowPosition pos, ArrowFill fill) {
    val position = toKebab(pos.toString());
    style.getStyle().put(String.format("%s-arrow-fill", position), toKebab(fill.toString()));
    return this;
  }

  public StyleBuilder arrowScale(int size) {
    style.getStyle().put("arrow-scale", size);
    return this;
  }

  public StyleBuilder sourceEndpoint(EdgeEndpoint endpoint) {
    style.getStyle().put("source-endpoint", toKebab(endpoint.toString()));
    return this;
  }

  public StyleBuilder sourceEndpoint(int x, int y) {
    style.getStyle().put("source-endpoint", y);
    return this;
  }

  public StyleBuilder sourceEndpoint(String angle) {
    style.getStyle().put("source-endpoint", angle);
    return this;
  }

  public StyleBuilder display(Display display) {
    style.getStyle().put("display", toKebab(display.toString()));
    return this;
  }

  public StyleBuilder visbility(Visibility visibility) {
    style.getStyle().put("visbility", toKebab(visibility.toString()));
    return this;
  }

  public StyleBuilder opacity(double opacity) {
    style.getStyle().put("opacity", opacity);
    return this;
  }

  public StyleBuilder zIndex(int zIndex) {
    style.getStyle().put("z-index", zIndex);
    return this;
  }

  public StyleBuilder zCompoundDepth(ZCompoundDepth zCompoundDepth) {
    style.getStyle().put("z-compound-depth", toKebab(zCompoundDepth.toString()));
    return this;
  }

  public StyleBuilder zIndexCompare(ZIndexCompare zIndexCompare) {
    style.getStyle().put("z-index-compare", toKebab(zIndexCompare.toString()));
    return this;
  }

  public StyleBuilder label(String label) {
    style.getStyle().put("label", label);
    return this;
  }

  public StyleBuilder dataLabel() {
    style.getStyle().put("data-label", "data('label')");
    return this;
  }

  public StyleBuilder sourceLabel(String label) {
    style.getStyle().put("source-label", label);
    return this;
  }

  public StyleBuilder targetLabel(String label) {
      style.getStyle().put("target-label", label);
    return this;
  }

  public StyleBuilder color(String color) {
    style.getStyle().put("color", color);
    return this;
  }

  public StyleBuilder textOpacity(double opacity) {
    style.getStyle().put("text-opacity", opacity);
    return this;
  }

  public StyleBuilder fontFamily(String fontFamily) {
    style.getStyle().put("font-family", fontFamily);
    return this;
  }

  public StyleBuilder fontSize(int size) {
    style.getStyle().put("font-size", size);
    return this;
  }

  public StyleBuilder fontStyle(String fontStyle) {
    style.getStyle().put("font-style", fontStyle);
    return this;
  }

  public StyleBuilder fontWeight(int weight) {
    style.getStyle().put("font-weight", weight);
    return this;
  }

  public StyleBuilder fontWeight(String weight) {
    style.getStyle().put("font-weight", weight);
    return this;
  }

  public StyleBuilder textTransform(TextTransform transform) {
    style.getStyle().put("text-transform", toKebab(transform.toString()));
    return this;
  }

  public StyleBuilder textWrap(TextWrap wrap) {
    style.getStyle().put("text-wrap", toKebab(wrap.toString()));
    return this;
  }

  public StyleBuilder textMaxWidth(int size) {
    style.getStyle().put("text-max-width", size);
    return this;
  }

  public StyleBuilder textHAlign(TextHAlign align) {
    style.getStyle().put("text-h-align", toKebab(align.toString()));
    return this;
  }

  public StyleBuilder textVAlign(TextVAlign align) {
    style.getStyle().put("text-v-align", toKebab(align.toString()));
    return this;
  }

  public StyleBuilder sourceTextOffset(int offset) {
    style.getStyle().put("source-text-offset", offset);
    return this;
  }

  public StyleBuilder targetTextOffset(int offset) {
    style.getStyle().put("target-text-offset", offset);
    return this;
  }

  public StyleBuilder textMarginX(int margin) {
    style.getStyle().put("text-margin-x", margin);
    return this;
  }

  public StyleBuilder sourceTextMarginX(int margin) {
    style.getStyle().put("source-text-margin-x", margin);
    return this;
  }

  public StyleBuilder targetTextMarginX(int margin) {
    style.getStyle().put("target-text-margin-x", margin);
    return this;
  }

  public StyleBuilder textMarginY(int margin) {
    style.getStyle().put("text-margin-y", margin);
    return this;
  }

  public StyleBuilder sourceTextMarginY(int margin) {
    style.getStyle().put("source-text-margin-y", margin);
    return this;
  }

  public StyleBuilder targetTextMarginY(int margin) {
    style.getStyle().put("target-text-margin-y", margin);
    return this;
  }

  public StyleBuilder textRotation(String degress) {
    style.getStyle().put("text-rotation", degress);
    return this;
  }

  public StyleBuilder sourceTextRotation(String degress) {
    style.getStyle().put("source-text-rotation", degress);
    return this;
  }

  public StyleBuilder targetTextRotation(String degress) {
    style.getStyle().put("target-text-rotation", degress);
    return this;
  }

  public StyleBuilder textAutorotate() {
      style.getStyle().put("text-autorotate", "autorotate");
    return this;
  }

  public StyleBuilder textOutlineColor(String color) {
    style.getStyle().put("text-outline-color", color);
    return this;
  }

  public StyleBuilder textOutlineOpacity(double opacity) {
    style.getStyle().put("text-outline-opacity", opacity);
    return this;
  }

  public StyleBuilder textOutlineWidth(int size) {
    style.getStyle().put("text-outline-width", size);
    return this;
  }

  public StyleBuilder textBackgroundColor(String color) {
    style.getStyle().put("text-background-color", color);
    return this;
  }

  public StyleBuilder textBackgroundOpacity(double opacity) {
    style.getStyle().put("text-background-opacity", opacity);
    return this;
  }

  public StyleBuilder textBackgroundShape(TextBackgroundShape textBackgroundShape) {
    style.getStyle().put("text-background-shape", toKebab(textBackgroundShape.toString()));
    return this;
  }

  public StyleBuilder textBackgroundPadding(int size) {
    style.getStyle().put("text-background-padding", size);
    return this;
  }

  public StyleBuilder textBorderColor(String color) {
    style.getStyle().put("text-border-color", color);
    return this;
  }

  public StyleBuilder textBorderOpacity(double opacity) {
    style.getStyle().put("text-border-opacity", opacity);
    return this;
  }

  public StyleBuilder textBorderStyle(BorderStyle borderStyle) {
    style.getStyle().put("text-border-style", toKebab(borderStyle.toString()));
    return this;
  }

  public StyleBuilder textBorderWidth(int size) {
    style.getStyle().put("text-border-width", size);
    return this;
  }

  public StyleBuilder minZoomedFintSize(int size) {
    style.getStyle().put("min-zoomed-fint-size", size);
    return this;
  }

  private String isToString (boolean is) {
    return is ? "yes" : "no";
  }

  public StyleBuilder textEvents(boolean is) {
    style.getStyle().put("text-events", isToString(is));
    return this;
  }

  public StyleBuilder events(boolean is) {
    style.getStyle().put("events", isToString(is));
    return this;
  }

  public StyleBuilder overlayColor(String color) {
    style.getStyle().put("overlay-color", color);
    return this;
  }

  public StyleBuilder overlayPadding(int size) {
    style.getStyle().put("overlay-padding", size);
    return this;
  }

  public StyleBuilder overlayOpacity(double opacity) {
    style.getStyle().put("overlay-opacity", opacity);
    return this;
  }

  public StyleBuilder ghost(boolean is) {
    style.getStyle().put("ghost", is);
    return this;
  }

  public StyleBuilder ghostOffsetX(int offset) {
    style.getStyle().put("ghost-offset-x", offset);
    return this;
  }

  public StyleBuilder ghostOffsetT(int offset) {
    style.getStyle().put("ghost-offset-t", offset);
    return this;
  }

  public StyleBuilder ghostOpacity(double opacity) {
    style.getStyle().put("ghost-opacity", opacity);
    return this;
  }

  public StyleBuilder transitionProperty(String property) {
    style.getStyle().put("transition-property", property);
    return this;
  }

  public StyleBuilder transitionDuration(String time) {
    style.getStyle().put("transition-duration", time);
    return this;
  }

  public StyleBuilder transitionDelay(String time) {
    style.getStyle().put("transition-delay", time);
    return this;
  }

  public StyleBuilder transitionTimingFunction(TransitionTimingFunction function) {
    style.getStyle().put("transition-timing-function", toKebab(function.toString()));
    return this;
  }

  public StyleBuilder transitionTimingFunctionSpring(int tension, int friction) {
    style.getStyle().put("transition-timing-function-spring", String.format("spring(%d, %d)", tension, friction));
    return this;
  }

  public StyleBuilder transitionTimingCubicBezier(int x1, int y1, int x2, int y2) {
    style.getStyle().put("transition-timing-cubic-bezier", String.format("cubic-bezier(%d, %d, %d, %d)", x1, y1, x2, y2));
    return this;
  }

  public StyleBuilder activeBgColor(String color) {
    style.getStyle().put("active-bg-color", color);
    return this;
  }

  public StyleBuilder activeBgOpacity(double opacity) {
    style.getStyle().put("active-bg-opacity", opacity);
    return this;
  }

  public StyleBuilder activeBgSize(int size) {
    style.getStyle().put("active-bg-size", size);
    return this;
  }

  public StyleBuilder selectionBoxColor(String color) {
    style.getStyle().put("selection-box-color", color);
    return this;
  }

  public StyleBuilder selectionBoxBorderColor(String color) {
    style.getStyle().put("selection-box-border-color", color);
    return this;
  }

  public StyleBuilder selectionBoxBorderWidth(int size) {
    style.getStyle().put("selection-box-border-width", size);
    return this;
  }

  public StyleBuilder selectionBoxOpactity(double opacity) {
    style.getStyle().put("selection-box-opactity", opacity);
    return this;
  }

  public StyleBuilder outsideTextureBgColor(String color) {
    style.getStyle().put("outside-texture-bg-color", color);
    return this;
  }

  public StyleBuilder outsideTextureBgOpacity(double opacity) {
    style.getStyle().put("outside-texture-bg-opacity", opacity);
    return this;
  }

  public StyleBuilder with(String key, Object value) {
    style.getStyle().put(key, value);
    return this;
  }

  public GraphBuilder add() {
    val styles = graphBuilder.graph.getStyle();
    if (styles == null)
      graphBuilder.graph.setStyle(Lists.newArrayList());
    graphBuilder.graph.getStyle().add(style);
    return graphBuilder;
  }
}
