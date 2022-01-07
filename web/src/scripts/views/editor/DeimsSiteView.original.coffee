define [
  'underscore'
  'jquery'
  'cs!views/editor/ObjectInputView'
  'tpl!templates/editor/DeimsSite.tpl'
  'jquery-ui/autocomplete'
], (_, $, ObjectInputView, template) -> ObjectInputView.extend

  template: template

  initialize: ->
    ObjectInputView.prototype.initialize.apply @

    @$('.autocomplete').autocomplete
      minLength: 2
      source: (request, response) ->
        term = request.term.trim()
        if _.isEmpty term
          query = "/vocabulary/deims"
        else
          query = "/vocabulary/deims?query=#{request.term}"

        $.getJSON query, (data) ->
          response _.map data, (d) -> {value: d.title, label: d.title, id: d.id, url: d.url}

    @$('.autocomplete').on 'autocompleteselect', (event, ui) =>
        @model.set 'id', ui.item.id
        @$('.id').val ui.item.id
        @model.set 'title', ui.item.label
        @$('.title').val ui.item.label
        @model.set 'url', ui.item.url
        @$('.url').val ui.item.url