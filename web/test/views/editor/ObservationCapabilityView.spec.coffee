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

    describe 'modify ObservationCapability', ->
     
      it 'modify', ->
        view.$el.find('#observedCapabilities0NameTitle').val('test text').trigger('change')

        console.log _.keys(view.$el.find('#observedCapabilities0NameTitle'))

        propertyName = model.get('observedPropertyName')

        expect(propertyName.title).toEqual 'test text'
        