define [
  'cs!views/editor/ContactsView'
  'cs!models/Metadata'
], (ContactsView, Metadata) ->
  describe 'ContactsView', ->
    view = null
    model = new Metadata()

    beforeEach ->
      view = new ContactsView model: model

    describe 'when view is constructing', ->
      it 'should exist', ->
        expect(view).toBeDefined()

    describe 'when view is initialized without model', ->
      it 'should throw exception', ->
        expect(() -> new ContactsView()).toThrow(new Error('model is required'))

    describe 'when view is rendered', ->

      beforeEach ->
        do view.render

      it 'should generate html', ->
        expect(view.$el.html()).not.toBeNull()

    describe 'when updating Contacts info', ->

      beforeEach ->
        spyOn model, 'set'

      it 'should call updateModel method', ->
        view.contacts.trigger 'add'
        expect(model.set).toHaveBeenCalled()