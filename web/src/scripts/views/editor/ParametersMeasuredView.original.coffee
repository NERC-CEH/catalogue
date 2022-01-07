define [
  'cs!views/editor/ObjectInputViewForObjects'
  'tpl!templates/editor/ParametersMeasured.tpl'
], (ObjectInputViewForObjects, template) -> ObjectInputViewForObjects.extend

  template:  template

  render: ->
    unless 'name' in @data
      @data.name = {}

    unless 'unitOfMeasure' in @data
      @data.unitOfMeasure = {}

    ObjectInputViewForObjects.prototype.render.apply @
