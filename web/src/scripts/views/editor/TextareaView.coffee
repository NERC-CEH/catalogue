define [
  'backbone'
  'tpl!templates/editor/Textarea.tpl'
], (Backbone, template) -> Backbone.View.extend

  events:
    'change': 'updateModel'

  initialize: ->
    do @render

  render: ->  @$el.html template _.clone @model.attributes

  updateModel: (event) ->
    target = event.currentTarget
    metadata = @model.getMetadata()
    metadata.set 'title', target.value
    @model.set 'metadata', metadata
    console.log "new title: #{metadata.get 'title'}"
