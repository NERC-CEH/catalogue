define [
  'cs!models/editor/TopicCategory'
], (TopicCategory) ->
  describe 'TopicCategory', ->
    topicCategory = null

    beforeEach ->
      topicCategory = new TopicCategory()
      spyOn topicCategory, 'set'

    it 'should set uri when value changed', ->
      topicCategory.trigger 'change:value', topicCategory, 'biota'

      expect(topicCategory.set).toHaveBeenCalledWith 'uri', 'http://inspire.ec.europa.eu/metadata-codelist/TopicCategory/biota'

    it 'should set empty string on unknown value', ->
      topicCategory.trigger 'change:value', topicCategory, 'unknown'

      expect(topicCategory.set).toHaveBeenCalledWith 'uri', ''