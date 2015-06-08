define [
  'cs!views/editor/ObjectInputView'
  'tpl!templates/editor/TopicCategory.tpl'
  'cs!models/editor/TopicCategory'
], (ObjectInputView, template, TopicCategory) -> ObjectInputView.extend
  template: template
  ModelType: TopicCategory

  render: ->
    ObjectInputView.prototype.render.apply @
    @$('select').val @model.get 'value'
    @
