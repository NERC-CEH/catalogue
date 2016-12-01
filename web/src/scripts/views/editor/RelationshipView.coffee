define [
  'cs!views/editor/ObjectInputView'
  'tpl!templates/editor/Relationship.tpl'
], (ObjectInputView, template) -> ObjectInputView.extend

  template:  template

  # Need to set select form element