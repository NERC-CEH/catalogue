define [
  'cs!views/upload/simple/AppView'
], (AppView) ->

  describe 'AppView', ->
    template =
      '<div id="messages">
        <div id="messages-tools"></div>
        <ul id="messages-list"></ul>
      </div>
      <form id="simple-upload-dropzone"
      <div>
        <div id="files-tools"></div>
        <ul id="files-list"></ul>
      </div>
      <script id="files-data" type="application/json">
        [
          {"name": "data1.csv", "urlEncodedName": "data1.csv"},
          {"name": "data two.csv", "urlEncodedName": "data%20two.csv"},
        ]
      </script>
      <script id="messages-data" type="application/json">
        {"message": "Test message", "type": "info"}
      </script>'
    el = null
    view = null

    beforeEach ->
      el = $(template).appendTo($('body'))
      view = new AppView
        el: el
        url: '/upload/test'

    afterEach ->
      el.remove()

    it 'has properties', ->
      expect(view.url).toBeDefined()
      expect(view.files).toBeDefined()
      expect(view.messages).toBeDefined()

    it 'loads data from page', ->
      expect(view.files.length).toEqual(2)
      expect(view.messages.length).toEqual(1)
