define [
  'jquery'
  'backbone'
  'cs!views/upload/simple/FileView'
], ($, Backbone, FileView) ->

  describe "FileView", ->
    el = null
    collection = null
    model = null
    view = null

    beforeEach ->
      el = $('ul').appendTo($('body'))
      spyOn(FileView.prototype, 'remove').and.callThrough()
      spyOn(FileView.prototype, 'render').and.callThrough()
      model = new Backbone.Model
        message: 'Hello World'
        type: 'info'
      view = new FileView
        el: el
        model: model

    afterEach ->
      el.remove()

    it 'has DOM events', ->
      expect(view.events['change input']).toBeDefined()
      expect(view.events['change input']).toEqual('select')

    it 'rerenders when model changed', ->
      #when
      model.set('toDelete', true)

      #then
      expect(view.render).toHaveBeenCalled()
