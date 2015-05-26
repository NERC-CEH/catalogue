define [
  'backbone'
  'cs!views/editor/ContactsItemView'
  'cs!models/editor/Contact'
], (Backbone, ContactsItemView, Contact) ->

  describe 'ContactsItemView', ->
    view = null
    model = null

    beforeEach ->
      model = new Contact()
      view = new ContactsItemView model: model

    describe 'when view is constructing', ->
      it 'should exist', ->
        expect(view).toBeDefined()

    describe 'when view is initialized without model', ->
      it 'should throw exception', ->
        expect(() -> new ContactsItemView()).toThrow(new Error('model is required'))

    describe 'when view is constructed with Contact collection', ->
      it 'should have contacts property', ->
        view = new ContactsItemView model: model, contacts: new Backbone.Collection()
        expect(view.contacts).toBeDefined()

    describe 'when view is rendered', ->

      beforeEach ->
        do view.render

      it 'should generate html', ->
        expect(view.$el.html()).not.toBeNull()

    describe 'add contact', ->

      beforeEach ->
        spyOn view, 'add'
        do view.delegateEvents
        do view.render

      it 'click add', ->
        view.$('button').trigger 'click'
        expect(view.add).toHaveBeenCalled()

      it 'has index of Add', ->
        expect(view.index).toEqual 'Add'

    describe 'remove contact', ->

      beforeEach ->
        collection = new Backbone.Collection [
          new Contact()
        ], Contact
        view = new ContactsItemView model: collection.at 0
        spyOn view, 'remove'
        do view.delegateEvents
        do view.render

      it 'click remove', ->
        view.$('button.remove').trigger 'click'
        expect(view.remove).toHaveBeenCalled()

      it 'has index of zero', ->
        expect(view.index).toEqual 0

    describe 'change an input', ->

      beforeEach ->
        spyOn view, 'modify'
        do view.delegateEvents
        do view.render

      it 'change input', ->
        view.$('input:first').trigger 'change'
        expect(view.modify).toHaveBeenCalled()

    describe 'add to collection', ->

      beforeEach ->
        collection = new Backbone.Collection()
        view = new ContactsItemView model: new Contact(), contacts: collection
        do view.render

      it 'should add model to contacts', ->
        do view.add
        expect(view.contacts.length).toEqual 1

    describe 'remove from collection', ->

      it 'should remove model from contacts', ->
        collection = new Backbone.Collection [
          new Contact()
        ], Contact
        view = new ContactsItemView model: collection.at 0
        do view.render
        do view.remove

        expect(collection.length).toEqual 0

    describe 'set a role', ->

      beforeEach ->
        spyOn model, 'set'
        view = new ContactsItemView model: model
        do view.render

      it 'change role', ->
        view.modify target: $('<select data-name="role"><option value="author" selected>Author</option><option value="another">Another</option></select>')
        expect(model.set).toHaveBeenCalledWith 'role', 'author'

    describe 'change an input', ->

      beforeEach ->
        spyOn model, 'set'
        view = new ContactsItemView model: model
        do view.render

      it 'change role', ->
        view.modify target: $('<input data-name="individualName" value="Bert">')
        expect(model.set).toHaveBeenCalledWith 'individualName', 'Bert'

    describe 'change an address postcode', ->

      beforeEach ->
        spyOn model, 'set'
        view = new ContactsItemView model: model
        do view.render

      it 'change postcode', ->
        view.modify target: $('<input data-name="postalCode" value="LA1 4AP">')
        expect(model.set).toHaveBeenCalledWith 'address', postalCode: 'LA1 4AP'
