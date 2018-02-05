define [
  'cs!models/DocumentUploadModel'
], (DocumentUploadModel) -> DocumentUploadModel.extend
  accept: (name, file) ->
    @save @attributes,
      url: @url() + '/accept-upload-file?name=' + name + '&filename=' + encodeURIComponent(file)

  delete: (name, file) ->
    @save @attributes,
      url: @url() + '/delete-upload-file?name=' + name + '&filename=' + encodeURIComponent(file)

  ignore: (name, file) ->
    @save @attributes,
      url: @url() + '/delete-upload-file?name=' + name + '&filename=' + encodeURIComponent(file)

  move: (file, from, to) ->
    @save @attributes,
      url: @url() + '/move-upload-file?from=' + from + '&to=' + to + '&filename=' + encodeURIComponent(file)

  moveToDatastore: (files) ->
    @save @attributes,
      url: @url() + '/move-to-datastore'

  zip: ->
    @save @attributes,
      url: @url() + '/zip-upload-files'

  unzip: ->
    @save @attributes,
      url: @url() + '/unzip-upload-files'
