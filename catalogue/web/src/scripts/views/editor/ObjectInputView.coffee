define [
  'underscore'
  'backbone'
  'tpl!templates/editor/Validation.tpl'
], (_, Backbone, validationTemplate) -> Backbone.View.extend

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
