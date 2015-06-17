define [
  'cs!routers/EditorRouter'
], (EditorRouter) ->
  describe "EditorRouter", ->
    it 'can load a document by metadata identifier', ->
      model = jasmine.createSpyObj 'model', ['loadDocument']

      router = new EditorRouter model: model
      router.loadRecord 'e40b53d4-6699-4557-bd55-10d196ece9ea'

      expect(model.loadDocument).toHaveBeenCalledWith 'e40b53d4-6699-4557-bd55-10d196ece9ea'

    it 'can create new document', ->
      model = jasmine.createSpyObj 'model', ['newDocument']

      router = new EditorRouter model: model
      router.newRecord()

      expect(model.newDocument).toHaveBeenCalled()