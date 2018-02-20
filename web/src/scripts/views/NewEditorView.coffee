define [
  'underscore'
  'backbone'
  'tpl!templates/NewFormError.tpl'
  'tpl!templates/DefaultParameter.tpl'
  'cs!views/ElterEditorViewFunctions'
], (
  _
  Backbone
  NewFormError
  DefaultParameter
  ElterEditorViewFunctions
) -> Backbone.View.extend
  timeout: null
  shouldSave: false
  autoSave: false
  shouldRedirect: false
  initialize: (options) ->
    @elter = ElterEditorViewFunctions(@, @model)
    if $('.new-form').length == 0
      do $('#new-document-model').modal
      $('.new-document-title').html(@model.title)
      @shouldRedirect = true
    else
      $('.new-document-title-input').removeAttr 'required'
      @autoSave = true
      do @model.fetch

    @model.on 'sync', =>
      if @shouldRedirect
        if @model.get('id')
          window.location.href = '/documents/' + @model.get('id')
        else
          window.location.reload()
      else
        $('#loading').hide()
        do @updateLinks
        do @initInputs
        do @renderDefaultParameters
        do @elter


    $('form').submit (evt) =>
      do evt.preventDefault
      do $('#form input, #form textarea, #form select').blur

      inputs = document.querySelectorAll 'input, textarea, select'
      for index, input of inputs
        @shouldSave = false if input.value == '' and input.required
        @shouldSave = false if input.value != '' and input.pattern and !new RegExp(input.pattern).test(input.value)

      if @shouldSave
        @shouldSave = false
        do @model.save
        do $('#saved').show
        clearTimeout @timeout
        @timeout = setTimeout (-> $('#saved').hide()), 1000
      else
        do @formValidation

    $('.save').click =>
      do @save
    do @initDelete
    do @initInputs

  formValidation: ->
    inputs = document.querySelectorAll 'input, textarea, select'
    for index, input of inputs
      failRequired = input.value == '' and input.required
      failMatch = input.value != '' and input.pattern and !new RegExp(input.pattern).test(input.value)
      if failRequired or failMatch
        $input = $(input)
        errorMessage = $input.data('errorMessage')
        errorName = $input.data('errorName')
        $input.addClass('error') if !$input.hasClass('error')
        if $('#' + errorName + '-error').length == 0
          $input.after NewFormError
            name: errorName
            message: errorMessage
      else if typeof input == 'object'
        $input = $(input)
        $input.removeClass('error')
        errorMessage = $input.data('errorMessage')
        errorName = $input.data('errorName')
        $('#' + errorName + '-error').remove()
    for index, input of inputs
      failRequired = input.value == '' and input.required
      failMatch = input.value != '' and input.pattern and !new RegExp(input.pattern).test(input.value)
      if failRequired or failMatch
        $(input).focus()
        break

  initDelete: ->
    $('.delete-document').unbind 'click'
    $('.delete-document').click =>
      do @model.destroy
        success: => window.location.href = '/' + @model.get('catalogue') + '/documents'
        error: => window.location.href = '/' + @model.get('catalogue') + '/documents'

  updateLinks: ->
    $('.value-link').each (index, link) =>
      link = $(link)
      name = link.data('name')
      format = link.data('format')
      value = @model.get(name) || '#'
      value = format.replace('{' + name + '}', value) if format and format != '' and format != '#'
      link.find('a').attr('href', value)
  
  renderDefaultParameters: ->
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
    do @initInputs
  
  initInputs: ->
    $('#form input, #form textarea, #form select').unbind 'change'
    $('#form input, #form textarea, #form select').change (evt) =>
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
      do @save if @autoSave

    $('.delete-defaultParameter').unbind 'click'
    $('.delete-defaultParameter').click (evt) =>
      target = $(evt.target)
      target = target.parent() if !target.hasClass('delete-defaultParameter')
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

