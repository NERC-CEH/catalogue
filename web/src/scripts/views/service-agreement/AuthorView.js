define [
  'cs!views/editor/ObjectInputView'
  'tpl!templates/service-agreement/Author.tpl'
], (ObjectInputView, template) -> ObjectInputView.extend

  template: template

  modify: (event) ->
    ObjectInputView.prototype.modify.call @, event
    @model.set 'role', 'author'

