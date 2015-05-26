define [
  'cs!models/editor/ResourceType'
], (ResourceType) ->
  describe 'ResourceType', ->
    resourceType = null

    beforeEach ->
      resourceType = new ResourceType()
      spyOn resourceType, 'set'

    it 'should set uri when value changed', ->
      resourceType.trigger 'change:value', resourceType, 'dataset'

      expect(resourceType.set).toHaveBeenCalledWith 'uri', resourceType.uris.dataset

    it 'should set empty string on unknown value', ->
      resourceType.trigger 'change:value', resourceType, 'feature'

      expect(resourceType.set).toHaveBeenCalledWith 'uri', ''