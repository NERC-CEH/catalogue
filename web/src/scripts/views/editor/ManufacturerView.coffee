define [
  'underscore'
  'cs!views/editor/SelectView'
  'cs!views/editor/InputView'
  'cs!models/Manufacturer'
], (_, SelectView, InputView, Manufacturer) -> SelectView.extend

  initialize: (options) ->
    manufacturer = new Manufacturer
    do manufacturer.fetch
    manufacturer.on 'sync', =>
        @options = [{ value: 'other', label: 'Other' }]
        for key, m of manufacturer.attributes
            @options.push
                value: m.id
                label: m.title
        do @render

    options.options = []
    SelectView.prototype.initialize.call @, options

    @model.on 'change', =>
      known = false
      for key, value of @options
        if value.value == @model.attributes.manufacturer
          known = true
      if known == false
        do manufacturer.fetch
      
      if @model.attributes.manufacturer == 'other' and $('#input-manufacturerName').length == 0
        $(@el).append(new InputView(
          model: @model
          modelAttribute: 'manufacturerName'
          label: 'Name'
        ).el.children[0])
        $(@el).append(new InputView(
          model: @model
          modelAttribute: 'manufacturerWebsite'
          label: 'Website'
        ).el.children[0])

  render: ->
    SelectView.prototype.render.apply @
