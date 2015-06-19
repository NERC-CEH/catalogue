define [
  'cs!views/editor/ObjectInputView'
  'tpl!templates/editor/ResourceType.tpl'
], (ObjectInputView, template) -> ObjectInputView.extend

  template: template

  initialize: ->
    ObjectInputView.prototype.initialize.apply @
    @listenTo @model, 'change:resourceType', (model, value) ->
      @model.set 'type', value.value

  render: ->
    ObjectInputView.prototype.render.apply @
    @$('select').val @model.get 'value'
    @
