define [
  'cs!views/editor/ObjectInputViewForObjects'
  'tpl!templates/editor/ObservationCapability.tpl'
], (ObjectInputViewForObjects, template) -> ObjectInputViewForObjects.extend

  template:  template

  render: ->
    unless 'observedPropertyName' in @data 
      @data.observedPropertyName = {}

    unless 'observedPropertyUnitOfMeasure' in @data 
      @data.observedPropertyUnitOfMeasure = {}

    unless 'procedureName' in @data 
      @data.procedureName = {}

    ObjectInputViewForObjects.prototype.render.apply @