define [
  'underscore'
  'jquery'
  'cs!views/editor/ObjectInputView'
  'tpl!templates/editor/RelatedRecord.tpl'
  'jquery-ui/autocomplete'
], (_, $, ObjectInputView, template) -> ObjectInputView.extend  

  template: template

  initialize: ->
    ObjectInputView.prototype.initialize.apply @
    catalogue = $('html').data('catalogue')
    
    @$('.autocomplete').autocomplete
      minLength: 2
      source: (request, response) ->
        term = request.term.trim()
        if _.isEmpty term
          query = "/#{catalogue}/documents"
        else
          query = "/#{catalogue}/documents?term=#{request.term}"
        
        $.getJSON query, (data) ->
          response _.map data.results, (d) -> {value: d.title, label: d.title, identifier: d.identifier, type: d.resourceType}

    @$('.autocomplete').on 'autocompleteselect', (event, ui) =>
        @model.set 'identifier', ui.item.identifier
        @$('.identifier').val ui.item.identifier
        @model.set 'href', 'https://catalogue.ceh.ac.uk/id/' + ui.item.identifier
        @$('.href').val 'https://catalogue.ceh.ac.uk/id/' + ui.item.identifier
        @model.set 'associationType',  ui.item.type
        @$('.associationType').val ui.item.type
        @model.set 'title', ui.item.label
        @$('.title').val ui.item.label


  render: -> 
    ObjectInputView.prototype.render.apply @
    @$('select.rel').val @model.get 'rel'
    @
