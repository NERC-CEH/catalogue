define [
  'cs!views/editor/ObjectInputView'
  'cs!models/editor/ResourceType'
  'tpl!templates/editor/ResourceTypeInput.tpl'
], (ObjectInputView, ResourceType, template) -> ObjectInputView.extend

  ModelType: ResourceType
  modelAttribute: 'resourceType'
  template: template

  initialize: ->
    ObjectInputView.prototype.initialize.apply @
    @listenTo @model, 'change:resourceType', (model, value) ->
      @model.set 'type', value.value

  render: ->
    ObjectInputView.prototype.render.apply @
    @$('select').val @dataEntry.get 'value'
    @
