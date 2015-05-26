define [
  'backbone'
  'underscore'
  'tpl!templates/editor/ContactsItem.tpl'
  'cs!models/editor/Contact'
], (Backbone, _, template, Contact) -> Backbone.View.extend

  events:
    'click #contactsAdd': 'add'
    'click button.remove': 'remove'
    'change': 'modify'

  initialize: (options) ->
    if not @model
      throw new Error('model is required')

    @index = if @model.collection then @model.collection.indexOf @model else 'Add'

    if options.contacts
      @contacts = options.contacts

  render: ->
    @$el.html template _.extend @model.toJSON(), index: @index
    @$('select').val @model.get 'role'
    return @

  add: ->
    if @contacts
      @contacts.add @model.clone(), at: 0
      @model = new Contact()
      do @render

  remove: ->
    if @model.collection
      @model.collection.remove @model

  modify: (event) ->
    $target = $(event.target)
    name = $target.data('name')

    if _.contains(['deliveryPoint', 'city', 'administrativeArea', 'country', 'postalCode'], name)
      address = _.clone @model.get 'address'
      address[name] = $target.val()
      @model.set 'address', address
    else
      @model.set name, $target.val()
