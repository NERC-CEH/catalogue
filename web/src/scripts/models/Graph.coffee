define [
  'underscore'
  'backbone'
], (_, Backbone) -> Backbone.Model.extend
  url: "/graph"
  initialize: (id) ->
    @id = id
    @url = "/graph/#{id}"
    @urlRoot = "/graph/#{id}"
    do @fetch

  open: (selected, data) ->
    currentElements = [].concat @get 'elements'
    currentElements = currentElements.nodes if currentElements.nodes
    for newElement in data.elements
      shouldAdd = true
      for currentElement in currentElements
        shouldAdd = false if currentElement.data.id == newElement.data.id
      currentElements.push newElement if shouldAdd
    for currentElement in currentElements
      currentElement.classes += ' open' if currentElement.data.id == selected and !currentElement.classes.includes('open')
    @set 'elements', currentElements
  
  close: (selected) ->
    currentElements = [].concat @get 'elements'
    index = -1
    for i, currentElement of currentElements
      index = i if currentElement.data.id.includes(selected)
    currentElements.splice(i, 1)
    @set 'elements', currentElements