define [
  'cs!models/Metadata'
  'cs!views/editor/ResourceTypeView'
], (Metadata, ResourceTypeView) ->
  describe 'SingleObjectView', ->
    view = null
    model = new Metadata()

    beforeEach ->
      view = new ResourceTypeView
        model: model

    describe 'when view is constructing', ->
      it 'should exist', ->
        expect(view).toBeDefined()

    describe 'when view is rendered with populated model', ->

      beforeEach ->
        model = new Metadata
          resourceType:
            value: 'dataset'

        view = new ResourceTypeView
          model: model
        do view.render

      it 'there should be dataset selected', ->
        expect(view.$('#resourceType').val()).toEqual 'dataset'

    describe 'selecting Resource Type to model', ->

      beforeEach ->
        spyOn model, 'set'
        do view.render

      it 'should set model', ->
        view.$('#resourceType').val('series').trigger 'change'

        expect(model.set).toHaveBeenCalled()