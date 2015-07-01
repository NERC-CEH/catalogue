define [
  'cs!views/editor/ObjectInputView'
  'tpl!templates/editor/Input.tpl'
], (ObjectInputView, template) -> ObjectInputView.extend

  initialize: (options) ->
    ObjectInputView.prototype.initialize.call @, options
    @modelAttribute = options.modelAttribute

  render: ->
    @$el.html template
      data:
        modelAttribute: @modelAttribute
        value: @model.get 'value'
    @