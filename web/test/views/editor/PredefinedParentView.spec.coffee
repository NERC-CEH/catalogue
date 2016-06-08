define [
  'backbone'
  'cs!views/editor/PredefinedParentView'
  'cs!views/editor/BoundingBoxView'
  'cs!models/GeminiMetadata'
], (Backbone, PredefinedParentView, BoundingBoxView, GeminiMetadata) ->

  describe 'PredefinedParentView', ->
    view = model = null

    beforeEach ->
      model = new GeminiMetadata
      view = new PredefinedParentView
        model: model
        modelAttribute: 'boundingBoxes'
        label: 'Spatial Extents'
        ObjectInputView: BoundingBoxView
        multiline: true
        predefined:
          England:
            northBoundLatitude: 55.812
            eastBoundLongitude: 1.7675
            southBoundLatitude: 49.864
            westBoundLongitude: -6.4526

    describe 'clicking England', ->

      beforeEach ->
        spyOn view.collection, 'add'

      it 'should add new model to collection', ->
        view.$('.dropdown').addClass 'open'
        view.$('li:eq(1)').trigger 'click'

        expect(view.collection.add).toHaveBeenCalledWith
          northBoundLatitude: 55.812
          eastBoundLongitude: 1.7675
          southBoundLatitude: 49.864
          westBoundLongitude: -6.4526