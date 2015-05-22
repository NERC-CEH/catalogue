define [
  'backbone'
  'tpl!templates/editor/Contacts.tpl'
  'cs!views/editor/ContactsItemView'
  'cs!models/editor/Contact'
], (Backbone, template, ItemView, Contact) -> Backbone.View.extend

  initialize: ->
    if not @model
      throw new Error('model is required')

    @contacts = new Backbone.Collection @model.get('responsibleParties'), model: Contact

    @listenTo @contacts, 'add remove change', @updateModel

  addOne: (contact) ->
    view = new ItemView
      model: contact
      className: 'component'
    @$('#contacts').append view.render().el

  addAll: ->
    @$('#contacts').html('')
    @contacts.each @addOne, @

  render: ->
    @$el.html template

    do @addAll

    contact = new ItemView
      el: @$('#contactsAddNew')
      model: new Contact
      contacts: @contacts
    do contact.render

    return @

  updateModel: ->
    if @contacts.length > 0
      @model.set 'responsibleParties', @contacts
    else
      @model.unset 'responsibleParties'
    do @addAll