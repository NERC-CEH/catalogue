define [
  'backbone'
  'tpl!templates/editor/ResourceType.tpl'
], (Backbone, template) -> Backbone.View.extend

  events:
    'change #resourceType': 'select'

  initialize: ->
    if not @model
      throw new Error('model is required')

  render: ->
    @$el.html template
    resourceType = @model.get 'resourceType'
    if resourceType
      @$('#resourceType').val resourceType
    return @

  select: ->
    resourceType = @$('#resourceType').val()

    if resourceType
      @model.set
        'resourceType': resourceType
        'type': resourceType