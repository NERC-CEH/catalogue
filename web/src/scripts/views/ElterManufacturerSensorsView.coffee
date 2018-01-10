define [
  'backbone'
  'underscore'
], (
  Backbone
  _
) -> Backbone.View.extend
  template: '<li><a href="/documents/<%= id %>"><%= title %></a></li>'

  initialize: ->
    @model.on 'change', =>
      do @render
    do @model.fetch

  render: ->
    $('#manufacturer-sensors').html('<ul class="list-unstyled"></ul>') if @model.attributes
    for key, sensor of @model.attributes
        $('#manufacturer-sensors ul').append(_.template(@template)(sensor))
        