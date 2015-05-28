define [
  'backbone'
  'cs!views/editor/AlternateTitlesItemView'
], (Backbone, AlternateTitlesItemView) ->
  describe 'AlternateTitlesItemView', ->
    view = null
    model = new Backbone.Model value: ''

    beforeEach ->
      view = new AlternateTitlesItemView
        model: model

    describe 'when view is constructing', ->
      it 'should exist', ->
        expect(view).toBeDefined()

    describe 'when view is initialized without model', ->
      it 'should throw exception', ->
        expect(() -> new AlternateTitlesItemView()).toThrow(new Error('model is required'))

    describe 'when view is rendered', ->

      beforeEach ->
        do view.render

      it 'alternative title field should be empty', ->
        expect(view.$(':input').val()).toEqual ''

    describe 'when editing alternate title', ->

      beforeEach ->
        spyOn view, 'modify'
        do view.delegateEvents
        do view.render

      it 'should call modify method', ->
        view.$(':input').trigger 'change'
        expect(view.modify).toHaveBeenCalled()

    describe 'when adding new alternate title', ->

      beforeEach ->
        view.add = true
        spyOn view, 'addNew'
        do view.delegateEvents
        do view.render

      it 'should call addNew method', ->
        view.$('#alternateTitleAdd').trigger 'click'
        expect(view.addNew).toHaveBeenCalled()

    describe 'when removing alternate title', ->

      beforeEach ->
        spyOn view, 'remove'
        do view.delegateEvents
        do view.render

      it 'should call remove method', ->
        view.$('button').trigger 'click'
        expect(view.remove).toHaveBeenCalled()

    describe 'when view is rendered with populated model', ->

      beforeEach ->
        model = new Backbone.Model
          value: 'Test Value'
        view = new AlternateTitlesItemView
          model: model
        do view.render

      it 'input field should contain model value', ->
        expect(view.$(':input').val()).toEqual 'Test Value'