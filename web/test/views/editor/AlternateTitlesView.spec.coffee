define [
  'backbone'
  'cs!models/Metadata'
  'cs!views/editor/AlternateTitlesView'
  'cs!models/editor/Value'
], (Backbone, Metadata, AlternateTitlesView) ->
  describe 'AlternateTitlesView', ->
    view = null
    model = new Metadata()

    beforeEach ->
      view = new AlternateTitlesView
        model: model

    describe 'when view is constructing', ->
      it 'should exist', ->
        expect(view).toBeDefined()

    describe 'when view is initialized without model', ->
      it 'should throw exception', ->
        expect(() -> new AlternateTitlesView()).toThrow(new Error('model is required'))

    describe 'when view is rendered', ->

      beforeEach ->
        do view.render

      it 'alternative title field should be empty', ->
        expect(view.$('#alternateTitle').val()).toEqual ''

    describe 'when adding new alternate title', ->

      beforeEach ->
        spyOn view, 'add'
        do view.delegateEvents
        do view.render

      it 'should call add method', ->
        view.$('#alternateTitleAdd').trigger 'click'
        expect(view.add).toHaveBeenCalled()

    describe 'when updating title', ->

      beforeEach ->
        model = new Metadata
          alternateTitles: ['Countryside Survey', 'CS']
        view = new AlternateTitlesView
          model: model
        spyOn model, 'set'
        do view.render

      it 'adding new title should change model', ->
        view.alternateTitles.add new Backbone.Model value: 'alternate'
        expect(model.set).toHaveBeenCalledWith 'alternateTitles',  ['Countryside Survey', 'CS', 'alternate']

      it 'edited title should change model', ->
        view.alternateTitles.at(0).set 'value', 'Test Survey'
        expect(model.set).toHaveBeenCalledWith 'alternateTitles', ['Test Survey', 'CS']

    describe 'when view is rendered with populated model', ->

      beforeEach ->
        model = new Metadata
          alternateTitles: ['Countryside Survey', 'CS']
        view = new AlternateTitlesView
          model: model
        do view.render

      it 'input fields should contain model values', ->
        expect(view.$('tbody input:eq(0)').val()).toEqual 'Countryside Survey'
        expect(view.$('tbody input:eq(1)').val()).toEqual 'CS'