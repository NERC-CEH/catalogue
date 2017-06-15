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
          query = "/#{catalogue}/documents?term=state:published AND view:public AND documentType:CEH_MODEL"
        else
          query = "/#{catalogue}/documents?term=state:published AND view:public AND documentType:CEH_MODEL AND #{request.term}"
        
        $.getJSON query, (data) ->
          response _.map data.results, (d) -> {value: d.title, label: d.title, identifier: d.identifier}

    @$('.autocomplete').on 'autocompleteselect', (event, ui) =>
        @model.set 'id', ui.item.identifier
        @$('.identifier').val ui.item.identifier

  render: ->
    ObjectInputView.prototype.render.apply @
    @$('select.spatial-extent').val @model.get 'spatialExtentOfApplication'
    @$('select.spatial-data').val @model.get 'availableSpatialData'
    @