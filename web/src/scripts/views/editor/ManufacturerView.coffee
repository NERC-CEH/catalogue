define [
  'underscore'
  'cs!views/editor/SelectView'
  'cs!models/Manufacturer'
], (_, SelectView, Manufacturer) -> SelectView.extend

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
    do @render

  render: ->
    SelectView.prototype.render.apply @
