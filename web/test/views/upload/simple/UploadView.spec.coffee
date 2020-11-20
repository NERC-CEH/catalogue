define [
  'jquery'
  'backbone'
  'cs!collections/upload/simple/FileCollection'
  'cs!views/upload/simple/UploadView'
], ($, Backbone, FileCollection, UploadView) ->

  describe "UploadView", ->
    el = null
    template =
      '<form action="/upload/test"
          class="dropzone"
          id="simple-upload-dropzone"
       ></form>'
    files = null
    messages = null
    view = null

    beforeEach ->
      el = $(template).appendTo($('body'))
      files = new FileCollection
        url: '/upload/test'
      messages = new Backbone.Collection()
      view = new UploadView
        el: el
        files: files
        messages: messages

    afterEach ->
      el.remove()

    it 'renders', ->
      expect(view).toBeDefined()
