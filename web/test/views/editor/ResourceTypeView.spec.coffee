define [
  'cs!models/Metadata'
  'cs!views/editor/ResourceTypeView'
], (Metadata, ResourceTypeView) ->
  describe 'ResourceTypeView', ->
    view = null
    model = new Metadata()

    beforeEach ->
      view = new ResourceTypeView
        model: model

    describe 'when view is constructing', ->
      it 'should exist', ->
        expect(view).toBeDefined()

    describe 'when view is initialized without model', ->
      it 'should throw exception', ->
        expect(() -> new ResourceTypeView()).toThrow(new Error('model is required'))

    describe 'when view is rendered with populated model', ->

      beforeEach ->
        model = new Metadata
          resourceType: 'dataset'

        view = new ResourceTypeView
          model: model
        do view.render

      it 'there should be 2 items in the collection', ->
        expect(view.$('#resourceType').val()).toEqual 'dataset'

    describe 'selecting Resource Type', ->

      beforeEach ->
        spyOn view, 'select'
        do view.delegateEvents
        do view.render

      it 'should call select', ->
        view.$('#resourceType').trigger 'change'

        expect(view.select).toHaveBeenCalled()

    describe 'selecting Resource Type to model', ->

      beforeEach ->
        spyOn model, 'set'
        view = new ResourceTypeView
          model: model
        do view.render

      it 'should set model', ->
        view.$('#resourceType').val('series').trigger 'change'

        expect(model.set).toHaveBeenCalled()