define [
  'underscore'
  'backbone'
  'cs!collections/Positionable'
  'tpl!templates/editor/Validation.tpl'
], (_, Backbone, Positionable, validationTemplate) -> Backbone.View.extend

  events:
    'change': 'modify'

  initialize: (options) ->
    @data = options
    @listenTo @model, 'remove', -> do @remove
    @listenTo @model, 'change', (model) ->
      if model.isValid()
        @$('.validation').hide()
      else
        @$('.validation').show()
        $validation = @$('.validation ul')
        $validation.html ''
        _.each model.validationError, (error) ->
          $validation.append $("<li>#{error.message}</li>")
    do @render

  render: ->
    @$el.html @template data:  _.extend {}, @data, @model.attributes
    @$el.append validationTemplate()
    @

  modify: (event) ->
    $target = $(event.target)
    name = $target.data('name')
    value = $target.val()

    if not value
      @model.unset name
    else
      @model.set name, value

  ###
  Defines a sortable list view which is bound to a given attribute on the model.
  The supplied `add` callback function is required to generate a constructed
  child view element which will be renederd on to the list
  ###
  createList: (name, selector, add)->
    element = @$ selector
    collection = new Positionable []
    addChild = => 
      view = add.apply @, arguments
      element.append view.el

    @listenTo collection, 'add', addChild
    @listenTo collection, 'reset', => 
      do element.empty
      collection.each addChild, @
    @listenTo collection, 'add remove change position', =>
      @model.set name, collection.toJSON()

    pos = null
    element.sortable
      start: (event, ui) =>
        pos = ui.item.index()
      update: (event, ui) =>
        collection.position pos, ui.item.index()

    collection.reset @model.get name
    return collection