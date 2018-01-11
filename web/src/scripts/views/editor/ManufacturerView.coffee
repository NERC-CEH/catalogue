define [
  'underscore'
  'cs!views/editor/SelectView'
  'cs!views/editor/InputView'
  'cs!models/Manufacturer'
], (_, SelectView, InputView, Manufacturer) -> SelectView.extend

  initialize: (options) ->
    manufacturer = new Manufacturer
    do manufacturer.fetch
    manufacturer.on 'change', =>
        for key, manufacturer of manufacturer.attributes
            @options.push
                value: manufacturer.id
                label: manufacturer.title
        do @render

    options.options = [{ value: 'other', label: 'Other' }]
    SelectView.prototype.initialize.call @, options

    @model.on 'change', =>
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
