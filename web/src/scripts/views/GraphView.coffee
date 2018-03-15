define [
  'jquery'
  'backbone'
  'cytoscape'
  'cytoscape-cose-bilkent'
  'file-saver'
], ($, Backbone, cytoscape, regCose) -> Backbone.View.extend
  cose:
    name: 'cose-bilkent'
    nodeDimensionsIncludeLabels: true
    randomize: false
    animate: 'end'
    animationDuration: 200
    idealEdgeLength: 200
    padding: 50

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
        'arrow-scale': 2
    }, {
      selector: 'node'
      style: {
        'background-image': 'data:image/svg+xml;utf8,' + encodeURIComponent '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 448 512" width="18" height="18" fill="#fff"><path d="M448 294.2v-76.4c0-13.3-10.7-24-24-24H286.2V56c0-13.3-10.7-24-24-24h-76.4c-13.3 0-24 10.7-24 24v137.8H24c-13.3 0-24 10.7-24 24v76.4c0 13.3 10.7 24 24 24h137.8V456c0 13.3 10.7 24 24 24h76.4c13.3 0 24-10.7 24-24V318.2H424c13.3 0 24-10.7 24-24z"></path></svg>'
      }
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
    }, {
      selector: '.open'
      style: {
        'background-image': 'data:image/svg+xml;utf8,' + encodeURIComponent '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 448 512" width="18" height="18" fill="#fff"><path d="M424 318.2c13.3 0 24-10.7 24-24v-76.4c0-13.3-10.7-24-24-24H24c-13.3 0-24 10.7-24 24v76.4c0 13.3 10.7 24 24 24h400z"></path></svg>'
      }
    }, {
      selector: '.root'
      style: {
        'background-image': 'none'
      }
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
    window.cy = @cy
    @model.on 'sync', =>
      do @render
      setTimeout (=> do @shuffle), 201

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

    @cy.removeListener 'tap'
    @cy.on('tap', 'node', (evt) =>
      rootNode = @cy.$('.root')[0]
      dijkstra = @cy.elements().dijkstra('#' + rootNode.id())

      if !evt.target.hasClass('open') and !evt.target.hasClass('root')
        $.getJSON('/graph/' + evt.target.id(), (data) =>
            @model.open evt.target.id(), data
            do @render
          )
      if evt.target.hasClass('open')
        evt.target.removeClass('open')
        edges = evt.target.connectedEdges((el) ->
          el.source().id() != rootNode.id() and el.target().id() != rootNode.id() and el.target().id() != evt.target.id()
        )
        edges.each (edge) ->
          edge.source().removeClass('open')
          edge.target().removeClass('open')
        edges.remove()

        connected = []
        cy.elements().bfs({
          roots: '#' + rootNode.id()
          visit: (currentNode) ->
            connected.push currentNode.id()
        })

        @cy.nodes().each (node) ->
          node.remove() if !connected.includes(node.id())
        
        nodes = @cy.json().elements.nodes || []
        edges = @cy.json().elements.edges || []
        @model.set 'elements', nodes.concat edges
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
