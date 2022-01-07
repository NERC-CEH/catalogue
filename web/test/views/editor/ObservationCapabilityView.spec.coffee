define [
  'underscore'
  'jquery'
  'cs!models/EditorMetadata'
  'cs!views/editor/ObservationCapabilityView'
], (_, $, EditorMetadata, ObservationCapabilityView) ->
  describe 'ObservationCapabilityView', ->
    view = null
    model = new EditorMetadata
      observedPropertyName:
        title: 'Test Title'
        href: 'http://example.com/test'
      unitOfMeasure:
        title: 'UOM'
        href: 'http://example.com/uom'

    beforeEach ->
      view = new ObservationCapabilityView
        model: model

    describe 'when view is constructing', ->
      it 'should exist', ->
        expect(view).toBeDefined()

    describe '_setObject', ->
      
      it '_setObject for object', ->
        objectName = 'test'
        attributeName = 'green'
        value = 'hot'

        view._setObject objectName, attributeName, value

        expect(model.has objectName).toBe true
        obj = model.get objectName
        expect(_.isObject obj).toBe true
        expect(_.has obj, attributeName).toBe true
        expect(obj[attributeName]).toBe 'hot'

      it '_setObject for string', ->
        objectName = 'text'
        attributeName = undefined
        value = 'black'

        view._setObject objectName, attributeName, value 

        expect(model.has objectName).toBe true
        attribute = model.get objectName
        expect(_.isObject attribute).toBe false
        expect(attribute).toBe 'black'
        