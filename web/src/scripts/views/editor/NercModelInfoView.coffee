define [
  'underscore'
  'jquery'
  'cs!views/editor/ObjectInputView'
  'tpl!templates/editor/ModelInfo.tpl'
  'jquery-ui/autocomplete'
], (_, $, ObjectInputView, template) -> ObjectInputView.extend

  template: template

  initialize: ->
    ObjectInputView.prototype.initialize.apply @
    catalogue = $('html').data('catalogue')
    
    @$('.autocomplete').autocomplete
      minLength: 0
      source: (request, response) ->
        term = request.term.trim()
        if _.isEmpty term
          query = "/#{catalogue}/documents?term=documentType:nerc-model"
        else
          query = "/#{catalogue}/documents?term=documentType:nerc-model AND #{request.term}"
        
        $.getJSON query, (data) ->
          response _.map data.results, (d) -> {value: d.title, label: d.title, identifier: d.identifier}

    @$('.autocomplete').on 'autocompleteselect', (event, ui) =>
        @model.set 'id', ui.item.identifier
        @$('.identifier').val ui.item.identifier

  render: ->
    ObjectInputView.prototype.render.apply @
    @$('select.spatial-data').val @model.get 'availableSpatialData'
    @