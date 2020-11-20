define [
  'cs!models/upload/simple/File'
], (File) ->

  describe 'File', ->
    model = new File()
    
    it 'has properties', ->
      expect(model.get('toDelete')).toBe(false)
