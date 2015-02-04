define [
  'jquery'
  'cs!models/EditorApp'
], ($, EditorApp) ->
  describe "EditorApp", ->
    app = null

    beforeEach ->
      app = new EditorApp()

    it "triggers 'loaded' event with new document", ->
      spyOn app, 'trigger'

      app.newDocument()

      expect(app.trigger).toHaveBeenCalledWith 'loaded'

    it "triggers 'loaded' event with loading document", ->

      spyOn($, 'ajax').and.callFake (settings) ->
        settings.success
          id: 'e40b53d4-6699-4557-bd55-10d196ece9ea'
          title: 'test'

      spyOn app, 'trigger'

      app.loadDocument 'e40b53d4-6699-4557-bd55-10d196ece9ea'
      expect(app.trigger).toHaveBeenCalledWith 'loaded'

    it "triggers 'error' event with loading unknown document", ->

      spyOn($, 'ajax').and.callFake (settings) ->
        settings.error
          some: 'text'

      spyOn app, 'trigger'

      app.loadDocument 'unknown'
      expect(app.trigger).toHaveBeenCalledWith 'error', 'Unable to load metadata for: unknown'