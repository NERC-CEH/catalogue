define [
  'cs!views/editor/ObjectInputView'
  'tpl!templates/editor/Input.tpl'
], (ObjectInputView, template) -> ObjectInputView.extend

  initialize: (options) ->
    ObjectInputView.prototype.initialize.call @, options
    @modelAttribute = options.modelAttribute

  render: ->
    console.log JSON.stringify @model.toJSON()
    console.dir @model
    @$el.html template
      data:
        modelAttribute: @modelAttribute
        value: @model.get 'value'
    @