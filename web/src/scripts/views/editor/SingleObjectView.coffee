define [
  'cs!views/editor/SingleView'
], (SingleView) -> SingleView.extend

  initialize: (options) ->
    SingleView.prototype.initialize.call @, options
    do @render
    new options.ObjectInputView
      el: @$('.dataentry')
      model: @model