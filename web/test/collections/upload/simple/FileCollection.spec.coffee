define [
  'cs!collections/upload/simple/FileCollection'
  'cs!models/upload/simple/File'
], (FileCollection, File) ->

  describe 'FileCollection', ->

    it 'has properties', ->
      collection = new FileCollection
        url: '/upload/993c5778-e139-4171-a57f-7a0f396be4b8'

      expect(collection.url).toEqual('/upload/993c5778-e139-4171-a57f-7a0f396be4b8')
      expect(collection.model).toEqual(File)