define [
  'cs!collections/MetadataDocuments'
], (MetadataDocuments) ->
  describe "MetadataDocuments Collection", ->
    it "can set the documents by ids", ->
      documents = new MetadataDocuments
      ids = ['1', '2', '3']

      documents.setById ids

      expect(documents.length).toBe 3
    
    it "delegates setById to the set method", ->
      documents = new MetadataDocuments
      options   = {}
      ids       = ['1']

      spyOn documents, 'set'

      documents.setById ids, options

      expect(documents.set).toHaveBeenCalledWith [ id: '1' ], options