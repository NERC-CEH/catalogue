define [
  'underscore'
  'backbone'
  'tpl!templates/Sensor.tpl'
  'tpl!templates/DefaultParameter.tpl'
  'tpl!templates/Manufacturer.tpl'
], (
  _
  Backbone
  Sensor
  DefaultParameter
  Manufacturer
) -> Backbone.View.extend
  timeout: null
  shouldSave: false
  shouldRedirect: false
  initialize: ->
    if $('.new-form').length == 0
      $('.search').hide()
      $('.search').after(Sensor())
      @shouldRedirect = true
    else
      do @model.fetch

    @model.on 'sync', =>
      $('#loading').hide()
      do @renderDefaultParameters
      do @initInputs
      do @updateManufacturers
      do @updateLinks
      window.location.href = '/documents/' + @model.get('id') if @shouldRedirect

    $('form').submit (evt) =>
      do evt.preventDefault
      evt.stopImmediatePropagation()
      do $('#form input, #form textarea, #form select').blur
      $('input:required').each (index, input) ->
        @shouldSave = false if input.value == ''

      if @shouldSave
        @shouldSave = false
        do @model.save
        do $('#saved').show
        clearTimeout @timeout
        @timeout = setTimeout (-> $('#saved').hide()), 1000

    do @initInputs
    return false
  
  updateLinks: ->
    $('.value-link').each (index, link) =>
      link = $(link)
      name = link.data('name')
      format = link.data('format')
      value = @model.get(name) || '#'
      value = format.replace('{' + name + '}', value) if format
      link.find('a').attr('href', value)
  
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
    @shouldSave = true
    $('#form').submit()

  updateManufacturers: ->
    $('.other-manufacturer').remove()

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
          $('#manufacturer').append('<option id="other-manufacturer" value="other"">Other</option>')
          $('#manufacturer').val(@model.get('manufacturer'))

    $('#manufacturer').unbind 'change'
    $('#manufacturer').change =>
      value = $('#manufacturer').val()
      @model.set('manufacturer', value)
      if value == 'other'
        $('#manufacturer').parent().after(Manufacturer())
        do @initInputs
      else
        do @save
