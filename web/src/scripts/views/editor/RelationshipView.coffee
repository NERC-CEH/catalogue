define [
  'cs!views/editor/ObjectInputView'
  'tpl!templates/editor/Relationship.tpl'
], (ObjectInputView, template) -> ObjectInputView.extend

  template:  template

  initialize: ->
    @render()

  render: ->
    ObjectInputView.prototype.render.apply @
    @$('select').val @model.get 'relation'
    @