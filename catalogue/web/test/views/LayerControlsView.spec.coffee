define [
  'jquery'
  'cs!views/LayerControlsView'
  'cs!models/OnlineResourceLayer'
  'cs!models/OnlineResource'
  'cs!models/MetadataDocument'
  'cs!collections/OnlineResources'
], ($, LayerControlsView, OnlineResourceLayer, OnlineResource, MetadataDocument, OnlineResources) ->
  describe "LayerControlsView", ->
    el = null
    layer = null
    view = null

    beforeEach ->
      el = $('<div></div>').hide().appendTo $('body') #el is hidden at start
      doc = new MetadataDocument
      resources = new OnlineResources [new OnlineResource], metadataDocument: doc

      layer = new OnlineResourceLayer
        title:          'title'
        visibility:     false
        onlineResource: resources.at(0)

      view = new LayerControlsView el: el, model: layer

    afterEach ->
      do el.remove

    it "clicking checkbox toggles visibility", ->
      spyOn layer, 'set'
      $('input.visibility', el).trigger 'click'

      expect(layer.set).toHaveBeenCalledWith 'visibility', true

    it "updates the toggle when the model changes", ->
      layer.set 'visibility', true
      visible = $('input.visibility', el).is ':checked'

      expect(visible).toBe true

    it "updates the model when the slider moves", ->
      $('.slider', el).trigger 'slide', value : 0.8

      expect(layer.get 'visibility').toBe true
      expect(layer.get 'opacity').toBe 0.8