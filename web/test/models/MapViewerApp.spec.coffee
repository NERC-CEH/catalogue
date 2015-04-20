define [
  'cs!models/MapViewerApp'
  'cs!models/MetadataDocument'
], (MapViewerApp, MetadataDocument) ->
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

    it "can return the layers collection", ->
      layers = app.getLayers()

      expect(layers).toBe app.layers

    it "fetches the metadata records when the metadata ids change", ->
      doc = new MetadataDocument
      doc.fetch = jasmine.createSpy 'fetch'
      spyOn app.layers, 'reset'
      app.metadataDocuments.setById = jasmine.createSpy 'setById'
      app.metadataDocuments.add doc


      app.set 'metadataIds', ['1', '2', 'Gemini-4']

      expect(app.layers.reset).toHaveBeenCalled()
      expect(app.metadataDocuments.setById).toHaveBeenCalledWith ['1', '2', 'Gemini-4']
      expect(doc.fetch).toHaveBeenCalled()

    it "adds newly synced metadata documents layers to the layers collection", ->
      doc = new MetadataDocument
      doc.getLayers =-> ['layer1', 'layer2']
      spyOn app.layers, 'add'

      app.metadataDocuments.trigger 'resources-sync', doc

      expect(app.layers.add).toHaveBeenCalledWith ['layer1', 'layer2']