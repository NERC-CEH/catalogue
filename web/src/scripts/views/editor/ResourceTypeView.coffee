define [
  'backbone'
  'cs!models/editor/ResourceType'
  'tpl!templates/editor/ResourceType.tpl'
], (Backbone, ResourceType, template) -> Backbone.View.extend

  events:
    'change #resourceType': 'select'

  initialize: ->
    if not @model
      throw new Error('model is required')

    @resourceType = new ResourceType @model.get 'resourceType'

  render: ->
    @$el.html template
    @$('#resourceType').val @resourceType.get 'value'
    return @

  select: ->
    value = @$('#resourceType').val()

    if value
      @resourceType.set 'value', value
      @model.set
        'resourceType': @resourceType
        'type': @resourceType.get 'value'

    else
      @model.unset 'resourceType'