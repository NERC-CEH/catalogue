define [
  'cs!views/editor/ObjectInputView'
  'tpl!templates/editor/ResourceIdentifier.tpl'
], (ObjectInputView, template) -> ObjectInputView.extend

  template:  template

  events:
    'click #resourceIdentifiersAddNewEidc': 'generate'

  generate: ->
    do @model.generateIdentifier