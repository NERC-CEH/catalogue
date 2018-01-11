define [
  'backbone'
  'underscore'
], (
  Backbone
  _
) -> Backbone.View.extend
  template: '<li><a href="/documents/<%= id %>"><%= title %></a></li>'

  initialize: ->
    @model.on 'sync', =>
      do @render
    do @model.fetch
    do @render

  render: ->
    $('#manufacturer-sensors').html('<ul class="list-unstyled"></ul>') if @model.attributes
    $('#manufacturer-sensors ul').append('<li>No sensors found for this manufacturer</li>') if @model.attributes.length == 0
    for key, sensor of @model.attributes
        $('#manufacturer-sensors ul').append(_.template(@template)(sensor))
