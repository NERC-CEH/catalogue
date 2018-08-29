define [
  'backbone'
  'cs!views/DocumentUploadMessage'
], (Backbone, message) -> Backbone.View.extend
  initialize: (options) ->
    @model.on 'sync', =>
      do @render
      do $('.loading').remove
      $('.messages').hide 'fast'
    do @model.fetch

  renderFile: (document, el, invalid) ->
    item = $("<tr></tr>")
    item.addClass('danger') if invalid
    item.attr('id', document.id)

    icon = $('<td></td>')
    iconFa = $('<i></i>')

    if document.path.endsWith('zip')
      iconFa.addClass('fas fa-file-archive')
    else
      iconFa.addClass('fas fa-file-alt')
    icon.append(iconFa)
    item.append(icon)

    filename = $('<td></td>')
    filename.text(document.name)
    item.append(filename)

    filehash = $('<td></td>')
    filehash.text(document.hash)
    item.append(filehash)

    filelocation = $('<td></td>')
    filelocation.attr('colspan', 2) if not invalid
    filelocation.text(document.physicalLocation)
    item.append(filelocation)

    if invalid
      error = $('<td></td>')
      errorMessage = $('<b></b>')
      errorMessage.text('INVALID')
      error.append(errorMessage)
      item.append(error)

    el.append(item)
  
  renderNoFiles: (el) ->
    item = $('<tr></tr>')
    message = $('<td></tr>')
    message.addClass('text-center')
    message.attr('colspan', 5)
    messageContainer = $('<b></b>')
    messageContainer.text('There are no data files')
    message.append(messageContainer)
    item.append(message)
    el.append(item)
  
  renderFiles: (files, name) ->
    documents = files[name]['documents'] || []
    invalid = files[name]['invalid'] || []

    if documents.length == 0
      @renderNoFiles $(".#{name} .files-list")

    for key, document of documents
      @renderFile document, $(".#{name} .files-list")
    if $(".#{name} .files-list-invalid").length
      for key, document of invalid
        @renderFile document, $(".#{name} .files-list-invalid"), yes

  renderChecksums: ->
    checksums = []

    files = @model.get('uploadFiles').datastore.documents || {}
    files = (value for own prop, value of files)
    files.sort (left, right) ->
      return -1 if left.name < right.name
      return 1 if left.name > right.name
      return 0
    for index, file of files
      checksums.push file.name + ',' + file.hash

    files = @model.get('uploadFiles').datastore.invalid || {}
    files = (value for own prop, value of files)
    files.sort (left, right) ->
      return -1 if left.name < right.name
      return 1 if left.name > right.name
      return 0
    for index, file of files
      checksums.push file.name + ',' + file.hash

    href = 'data:text/csv;charset=utf-8,' + encodeURI(checksums.join('\n'))

    $('.downloadChecksum').attr('disabled', no)
    $('.downloadChecksum').attr('href', href)

  render: ->
    files = @model.get 'uploadFiles'
    if $('.documents').length
      @renderFiles(files, 'documents')
    @renderFiles(files, 'datastore')
    @renderFiles(files, 'plone')
    do @renderChecksums
