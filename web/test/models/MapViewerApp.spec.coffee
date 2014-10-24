define [
  'cs!models/MapViewerApp'
], (MapViewerApp) ->
  describe "MapViewerApp", ->
    app = null

    beforeEach ->
      app = new MapViewerApp

    it "handles errors from its metadata documents", ->
      spyOn app, 'trigger'
      docs = app.metadataDocuments

      docs.trigger 'error', docs, 'some response'

      expect(app.trigger).toHaveBeenCalledWith 'error', docs, 'some response'

    it "handles resource-errors from its metadata documents", ->
      spyOn app, 'trigger'
      docs = app.metadataDocuments

      docs.trigger 'resources-error', docs, 'some response'

      expect(app.trigger).toHaveBeenCalledWith 'error', docs, 'some response'