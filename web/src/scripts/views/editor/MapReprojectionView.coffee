define [
  'underscore'
  'cs!views/editor/ObjectInputView'
  'tpl!templates/editor/MapReprojection.tpl'
], (_, ObjectInputView, template) -> ObjectInputView.extend

  template: template

  events: _.extend {}, ObjectInputView.prototype.events,
    'click button.remove': 'delete'

  delete: -> @model.collection.remove @model