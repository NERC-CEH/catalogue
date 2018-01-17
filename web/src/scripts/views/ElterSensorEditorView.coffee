define [
  'underscore'
  'backbone'
  'tpl!templates/sensor.tpl'
], (
  _
  Backbone
  sensorTpl
) -> Backbone.View.extend
  timeout: null
  initialize: ->
    do @model.fetch
    @model.on 'sync', =>
      do @updateManufacturers
      $('#loading').hide()

    $('form').submit (event) ->
      do $('#form input, #form textarea, #form select').blur
      do event.preventDefault

    $('#form input, #form textarea, #form select').on 'change', (evt) =>
      value = evt.target.value
      name = evt.target.name
      listOfMaps = /(\w+)\[(\d+)\]\[\'(\w+)\'\]/
      matched = name.match(listOfMaps)
      if matched != null
        name = matched[1]
        index = parseInt matched[2], 10
        key = matched[3]
        toUpdate = @model.get name
        toUpdate[index] = toUpdate[index] || {}
        toUpdate[index][key] = value
        @model.set name, toUpdate
      else
        @model.set name, value
      do @model.save
      $('#saved').show('fast')
      clearTimeout @timeout
      @timeout = setTimeout (-> $('#saved').hide('fast')), 2000

  updateManufacturers: ->
    if $('#manufacturer').length
      $.ajax
        type: 'GET'
        url: '/elter/manufacturers'
        headers:
          Accept: "application/json"
        success: (manufacturers) =>
          $('#manufacturer').html('<option value=""></option>')
          for index, manufacturer of manufacturers
            $('#manufacturer').append('<option value="' + manufacturer.id + '">' + manufacturer.title + '</option>')
          $('#manufacturer').append('<option value="other"">Other</option>')
          $('#manufacturer').val(@model.get('manufacturer') || 'other')
