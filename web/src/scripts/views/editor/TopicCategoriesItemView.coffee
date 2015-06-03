define [
  'cs!views/editor/ChildView'
  'tpl!templates/editor/TopicCategoriesItem.tpl'
  'cs!models/editor/TopicCategory'
], (ChildView, template, TopicCategory) -> ChildView.extend
  template: template
  ModelType: TopicCategory

  render: ->
    ChildView.prototype.render.apply @
    @$('select').val @model.get 'value'
