define [
  'underscore'
  'backbone'
  'tpl!templates/DefaultParameter.tpl'
  'cs!views/ElterEditorViewFunctions'
], (
  _
  Backbone
  DefaultParameter
  ElterEditorViewFunctions
) -> Backbone.View.extend
  timeout: null
  shouldSave: false
  autoSave: false
  shouldRedirect: false
  initialize: (options) ->
    @elter = ElterEditorViewFunctions(@, @model)
    if $('.new-document').length != 0
      do $('#new-document-modal').modal
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

      inputs = document.querySelectorAll '.value input, .value textarea, .value select'
      for input in inputs
        failRequired = !input.disabled && input.value == '' and input.required
        failMatch = !input.disabled && input.value != '' and input.pattern and !new RegExp(input.pattern).test(input.value)
        $(input).removeClass('is-errored')

        name = input.name
        listMatcher = /(\w+)\[.*/
        matched = name.match(listMatcher)
        name = matched[1] if matched != null

        $('#value-' + name).removeClass('is-errored')
        if failRequired || failMatch
          @shouldSave = false
          $(input).addClass('is-errored')
          $('#value-' + name).addClass('is-errored')
      
      if @shouldSave
        @shouldSave = false
        do @model.save
        do $('#saved').show
        clearTimeout @timeout
        @timeout = setTimeout (-> $('#saved').hide()), 1000

    $('.save').click =>
      do @save
    do @initDelete
    do @initInputs

  initDelete: ->
    $('.delete-document').unbind 'click'
    $('.delete-document').click =>
      do @model.destroy
        success: => window.location.href = '/' + @model.get('catalogue') + '/documents'
        error: => window.location.href = '/' + @model.get('catalogue') + '/documents'

  updateLinks: ->
      $('.value-link input, .value-link select').each (index, input) ->
        href = window.location.href
        href = href + '#' if (!href.endsWith('#'))
        href = input.value || href
        $('#value-' + input.name + ' .value-href').attr('href', href)
  
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

      list = /(\w+)\[(\d+)\]/
      listMatched = name.match(list)
      keyValue = /(\w+)\[(\d+)\]\[\'(\w+)\'\]/
      keyValueMatched = name.match(keyValue)
      if keyValueMatched != null
        name = keyValueMatched[1]
        index = parseInt keyValueMatched[2], 10
        key = keyValueMatched[3]
        toUpdate = @model.get name
        toUpdate = toUpdate || []
        toUpdate[index] = toUpdate[index] || {}
        toUpdate[index][key] = value
        @model.set name, toUpdate
      else if listMatched != null
        name = listMatched[1]
        index = parseInt listMatched[2], 10
        toUpdate = (@model.get name) || []
        toUpdate[index] = value
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
 
  showOther: (name) ->
    $('.other-' + name).removeClass('is-hidden')
    $('.other-' + name).addClass('is-visible')
    $('.other-' + name + ' input').val('')
    $('.other-' + name + ' input').prop('disabled', no)
    $('.other-' + name + ' input').focus()

  hideOther: (name) ->
    $('.other-' + name).addClass('is-hidden')
    $('.other-' + name).removeClass('is-visible')
    $('.other-' + name + ' input').val('')
    $('.other-' + name + ' input').prop('disabled', yes)
    $('.other-' + name + ' input').blur()

  initOtherable: (name) ->
    $('#' + name).unbind 'change'
    $('#' + name).change =>
      @hideOther  name
      value = $('#' + name).val()
      @model.set(name, value)
      if value == 'other'
        @showOther name
      else
        @hideOther name
        do @save

  updateOtherable: (name, url, renderValues, renderOther) ->
    @hideOther name
    if $('#' + name).length
      $.ajax
        type: 'GET'
        url: url
        headers:
          Accept: "application/json"
        success: (values) =>
          @hideOther name
          $('#' + name).html('')
          renderValues(values)
          renderOther()
          $('#' + name).val(@model.get(name))
    @initOtherable name
