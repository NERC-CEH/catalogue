define [
  'cs!models/editor/ResourceIdentifier'
], (ResourceIdentifier) ->
  describe 'ResourceIdentifier', ->

    resourceIdentifier = null

    it 'should set code and codeSpace when generateCeh is true', ->
      resourceIdentifier = new ResourceIdentifier generateCeh: true
      expect(resourceIdentifier.get 'codeSpace').toEqual 'CEH:EIDC:'
      expect(resourceIdentifier.get 'code').not.toBeNull()

    it 'not set code and codeSpace when generateCeh is false or missing', ->
      resourceIdentifier = new ResourceIdentifier()
      expect(resourceIdentifier.get 'codeSpace').toEqual ''
      expect(resourceIdentifier.get 'code').toEqual ''