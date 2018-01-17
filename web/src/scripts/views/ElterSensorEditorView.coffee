define [
  'underscore'
  'backbone'
  'tpl!templates/sensor.tpl'
  'tpl!templates/DefaultParameter.tpl'
], (
  _
  Backbone
  sensorTpl
  DefaultParameter
) -> Backbone.View.extend
  timeout: null
  initialize: ->
    do @model.fetch
    @model.on 'sync', =>
      do @updateManufacturers
      $('#loading').hide()
      do @renderDefaultParameters
      do @initInputs

    $('form').submit (event) ->
      do $('#form input, #form textarea, #form select').blur
      do event.preventDefault
    
    do @initInputs
  
  renderDefaultParameters: ->
    $('#form input, #form textarea, #form select').unbind 'change'
    defaultParameters = @model.get('defaultParameters') || []
    $('#defaultParameters').html('')
    for index, defaultParameter of defaultParameters
      $('#defaultParameters').append(DefaultParameter
        index: index
        value: defaultParameter.value
      )
    $('#defaultParameters').append(DefaultParameter
      index: defaultParameters.length
      value: ''
    )
  
  initInputs: ->
    $('#form input, #form textarea, #form select').unbind 'change'
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
        toUpdate = toUpdate || []
        toUpdate[index] = toUpdate[index] || {}
        toUpdate[index][key] = value
        @model.set name, toUpdate
      else
        @model.set name, value
      do @save

    $('#defaultParameters .delete').unbind 'click'
    $('#defaultParameters .delete').on 'click', (evt) =>
      target = $(evt.target)
      target = target.parent() if !target.hasClass('delete')
      input = target.parent().find('input')

      name = input.attr('name')
      value = input.val()
      if value != ''
        listOfMaps = /(\w+)\[(\d+)\]\[\'(\w+)\'\]/
        matched = name.match(listOfMaps)
        if matched != null
          name = matched[1]
          index = parseInt matched[2], 10
          toUpdate = @model.get name
          toUpdate = toUpdate || []
          toUpdate.splice(index, 1)
          @model.set name, toUpdate
          do @save

  save: ->
    do @model.save
    $('#saved').show()
    clearTimeout @timeout
    @timeout = setTimeout (-> $('#saved').hide()), 2000


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
