define [
  'jquery'
  'backbone'
  'cytoscape'
  'cytoscape-cose-bilkent'
  'file-saver'
], ($, Backbone, cytoscape, regCose) -> Backbone.View.extend
  cose:
    name: 'cose-bilkent',
    nodeDimensionsIncludeLabels: true,
    randomize: false,
    animate: false

  style: [{
      selector: 'node, edge'
      style:
        'label': 'data(label)'
        'text-outline-color': '#fff'
        'text-outline-width': 3
    }, {
      selector: 'edge'
      style:
        'curve-style': 'bezier'
        'line-color': '#2C3E50'
        'target-arrow-color': '#2C3E50'
        'target-arrow-shape': 'triangle'
    }, {
      selector: '.root'
      style:
        'background-color': '#FD7400'
    }, {
      selector: '.child'
      style:
        'background-color': '#3498DB'
    }, {
      selector: '.parent'
      style:
        'background-color': '#468966'
    }]
    

  shuffle: ->
    do @cy.layout(@cose).run

  initialize: ->
    regCose(cytoscape)
    $('#cy-zoom-in').click => do @zoomIn
    $('#cy-zoom-out').click => do @zoomOut
    $('#cy-fit').click => do @fit
    $('#cy-organise').click => do @shuffle
    $('#cy-screenshot').click => do @screenshot

    @cy = cytoscape({
        container: document.getElementById('cy'),
        @cose,
        elements: [],
        style: @style
    })
    @model.on 'sync', => do @render

  updateElements: (elements, group) ->
    updated = false
    elements.forEach((e) =>
      element = @cy.$('#' + e.data.id)
      if (element.length == 0)
        updated = true
        @cy.add
          classes: e.classes,
          data: e.data
      else
        element.data(e.data)
        element.classes(e.classes)
    )

    @cy[group]().each((c) =>
      shouldRemove = elements.filter((e) -> e.data.id == c.id()).length == 0
      if (shouldRemove)
        @cy.remove(c)
        updated = true
    )
    updated
  
  render: ->
    nodesUpdated = @updateElements @model.get('elements'), 'nodes'
    edgesUpdated = @updateElements @model.get('elements'), 'edges'
    do @shuffle if (nodesUpdated || edgesUpdated)

  zoomIn: ->
    currentZoom = @cy.zoom()
    @cy.zoom(currentZoom + 0.1)

  zoomOut: ->
    currentZoom = @cy.zoom()
    @cy.zoom(currentZoom - currentZoom / 4)

  fit: ->
    @cy.fit(10)

  screenshot: ->
    image = @cy.jpg({ output: 'blob' })
    saveAs image, 'network-' + @model.id + '-' + new Date().getTime() + '.jpg'
