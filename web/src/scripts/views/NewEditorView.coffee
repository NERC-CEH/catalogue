define [
  'underscore'
  'backbone'
  'tpl!templates/DefaultParameter.tpl'
  'cs!views/ElterViewFunctions'
  'tpl!templates/SelectList.tpl'
], (
  _
  Backbone
  DefaultParameter
  ElterViewFunctions
  SelectList
) -> Backbone.View.extend
  timeout: null
  shouldSave: false
  autoSave: false
  shouldRedirect: false
  fns: {}
  addedFns: ->
    @fns.elter = ElterViewFunctions(@, @model)

  initialize: (options) ->
    do @addedFns
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
        do @initInputs
        do @initDeleteButtons
        do @renderDefaultParameters
        do @updateLinks
        for index, fn of @fns
          do fn

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
        for key of @model.attributes
          @model.unset key if $('#value-' + key).length
        do @updateModel
        do @model.save
        do $('#saved').show
        clearTimeout @timeout
        @timeout = setTimeout (-> $('#saved').hide()), 1000

    $('.save').unbind 'click'
    $('.save').click =>
      do @save
    do @initDelete
    do @initInputs
    do @initDeleteButtons
  
  updateModel: ->
    $('#form input, #form textarea, #form select').each (index, element) =>
      value = element.value
      name = element.name
      if name
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
          toUpdate[index][key] = value if value != "" and value != null and typeof value != 'undefined'
          value = toUpdate
        else if listMatched != null
          name = listMatched[1]
          index = parseInt listMatched[2], 10
          toUpdate = (@model.get name) || []
          toUpdate[index] = value if value != "" and value != null and typeof value != 'undefined'
          value = toUpdate

        if element.type == 'radio'
          $('input[name="' + name + '"').each (index, input) ->
            value = input.value if input.checked

        @model.set name, value

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
      do @save if @autoSave

  getDeleteParent: (current) ->
    current = $(current)
    return current if (current.hasClass('delete-parent'))
    return current if current.length == 0
    return @getDeleteParent(current.parent())

  initDeleteButtons: ->
    $('.delete').unbind 'click'
    $('.delete').click (evt) =>
      parent = @getDeleteParent(evt.target)
      parent.remove() if (parent.length)
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
    $('#' + name + ' select').unbind 'change'
    $('#' + name + ' select').change (evt) =>
      target = $(evt.target)
      @hideOther  name
      value = target.val()
      if value == 'other'
        @showOther name
      else
        @hideOther name
        do @save

  updateOtherable: (name, url, renderValues) ->
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
          @initOtherable name
          do @updateLinks
          do @initDeleteButtons
    @initOtherable name
  
  updateSelectList: (name, url) ->
    @updateOtherable name, url, (allDocuments) =>
        allDocs = []
        for allDoc in allDocuments
          allDocs.push allDoc if allDoc.id != @model.get 'id'

        documentsTmp = @model.get(name) || []
        documents = []
        for r in documentsTmp
          if r != null and r != '' and typeof r != 'undefined'
            documents.push r

        if (allDocs.length > 0 and documents.length > 0)
          for index, docId of documents
            title = docId
            options = []
            for allDoc in allDocs
              selected = allDoc.id == docId
              options.push(
                value: allDoc.id
                label: allDoc.title
                selected: selected
              )
              title = allDoc.title if selected

            $('#' + name).append(SelectList(
              name: name
              index: index
              id: docId
              title: title
              options: options
              hasLink: true
            ))

        options = []
        for allDoc in allDocs
          selected = allDoc.id == docId
          options.push(
            value: allDoc.id
            label: allDoc.title
            selected: false
          )
        $('#' + name).append(SelectList(
          name: name
          index: documents.length
          id: ''
          title: ''
          options: [{ value: '', label: '', selected: true }].concat(options)
          hasLink: false
        ))
