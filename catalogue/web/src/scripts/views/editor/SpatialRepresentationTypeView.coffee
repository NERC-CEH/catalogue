define [
  'underscore'
  'cs!views/editor/ParentStringView'
  'tpl!templates/editor/SpatialRepresentationType.tpl'
], (_, ParentStringView, childTemplate) -> ParentStringView.extend

  childTemplate: childTemplate

  render: ->
    ParentStringView.prototype.render.apply @
    _.each @array, (string, index) =>
      @$("#input#{ @data.modelAttribute}#{ index } select").val string