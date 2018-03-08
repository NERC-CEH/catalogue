package uk.ac.ceh.gateway.catalogue.graph;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Test;

import lombok.SneakyThrows;
import lombok.val;

public class GraphTest {
  @Test
  @SneakyThrows
  public void graph() {
    val graph = new GraphBuilder()
      .node("node id")
        .label("node label")
        .data("key", "value")
        .add()
      .node("edge id")
        .source("node id 1")
        .target("node id 2")
        .add()
      .style("edge")
        .arrowColor(ArrowPosition.MID_TARGET, "red")
        .controlPointWeights(1, 2, 3)
        .edgeDistances(EdgeDistance.INTERSECTION, EdgeDistance.CONTROL_POINT_WEIGHT)
        .dataLabel()
        .transitionTimingFunctionSpring(10, 10)
        .transitionTimingCubicBezier(1, 1, 2, 2)
        .add()
      .layout("my-layout")
        .circle(true)
        .edgeElasticity(100)
        .gravity(0.5)
        .add()
      .build();

    val expected = "{\"elements\":[{\"data\":{\"id\":\"node id\",\"label\":\"node label\",\"key\":\"value\"}},{\"data\":{\"id\":\"edge id\",\"source\":\"node id 1\",\"target\":\"node id 2\"}}],\"layout\":{\"edgeElasticity\":100,\"gravity\":0.5,\"name\":\"my-layout\",\"circle\":true},\"style\":[{\"selector\":\"edge\",\"style\":{\"edge-distances\":\"intersection control-point-weight\",\"control-point-weights\":\"1 2 3\",\"transition-timing-cubic-bezier\":\"cubic-bezier(1, 1, 2, 2)\",\"transition-timing-function-spring\":\"spring(10, 10)\",\"mid-target-arrow-color\":\"red\",\"data-label\":\"data('label')\"}}]}";
    val expectedGraph = new ObjectMapper().readValue(expected, Graph.class);

    assertThat(graph, equalTo(expectedGraph));        
  }
}