define [
  'cs!views/editor/SingleObjectView'
  'cs!models/editor/ResourceType'
  'tpl!templates/editor/ResourceType.tpl'
], (SingleObjectView, ResourceType, template) -> SingleObjectView.extend
  template: template
  modelAttribute: 'resourceType'
  ModelType: ResourceType

  initialize: ->
    SingleObjectView.prototype.initialize.apply @
    @listenTo @model, 'change:resourceType', (model, value) ->
      @model.set 'type', value.value

  render: ->
    SingleObjectView.prototype.render.apply @
    @$('select').val @dataEntry.get 'value'
