define [
  'backbone'
  'tpl!templates/editor/Lineage.tpl'
], (Backbone, template) -> Backbone.View.extend

  events:
    'change': 'updateModel'

  render: ->
    @$el.html template
      value: @model.get 'lineage'
    return @

  updateModel: ->
    @model.set 'lineage', @$("#input-lineage").val()