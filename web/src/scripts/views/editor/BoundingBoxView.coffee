define [
  'cs!views/editor/ObjectInputView',
  'tpl!templates/editor/BoundingBox.tpl'
], (ObjectInputView, template) -> ObjectInputView.extend

  template: template

  events:
    'change select': 'predefined'

  predefined: (event) ->
    console.dir event

