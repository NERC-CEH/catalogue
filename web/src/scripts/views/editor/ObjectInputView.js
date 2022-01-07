define [
  'underscore'
  'jquery'
  'backbone'
  'cs!collections/Positionable'
  'tpl!templates/editor/Validation.tpl'
], (_, $, Backbone, Positionable, validationTemplate) -> Backbone.View.extend

  events:
    'change': 'modify'

  initialize: (options) ->
    @data = options
    @listenTo @model, 'remove', -> do @remove
    @listenTo @model, 'change', (model) ->
      $validation = @$('>.validation')
      if model.isValid()
        $validation.hide()
      else
        $validation.show()
        $validationList = $('div.warnings', $validation)
        $validationList.html ''
        _.each model.validationError, (error) ->
          $validationList.append $("<p>#{error.message}</p>")
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

    return false # disable bubbling

  ###
  Defines a sortable list view which is bound to a positionable collection.
  The supplied `view` callback function is required to generate a constructed
  child view element which will be renederd on to the list
  ###
  createList: (collection, selector, view)->
    element = @$ selector
    addView = => 
      newView = view.apply @, arguments
      element.append newView.el

    resetView = =>
      do element.empty
      collection.each addView, @

    @listenTo collection, 'add', addView
    @listenTo collection, 'reset', resetView

    pos = null
    if !(@data.disabled == 'disabled')
      element.sortable
        start: (event, ui) =>
          pos = ui.item.index()
        update: (event, ui) =>
          collection.position pos, ui.item.index()

    do resetView
    return collection