/* eslint-disable
    no-return-assign,
    no-undef,
*/
// TODO: This file was created by bulk-decaffeinate.
// Fix any style issues and re-enable lint.
/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
define([
  'jquery',
  'cs!models/EditorMetadata',
  'cs!views/GeminiEditorView',
  'bootstrap'
], ($, Metadata, EditorView) => describe('EditorView', function () {
  let view = null
  const model = new Metadata()

  beforeEach(() => view = new EditorView({ model }))

  describe('when view is constructing', () => it('should exist', () => expect(view).toBeDefined()))

  describe('when exit clicked', function () {
    beforeEach(function () {
      spyOn(view, 'exit');
      (view.delegateEvents)()
      return (view.render)()
    })

    return it('exit clicked', function () {
      view.$('#editorExit').trigger('click')
      return expect(view.exit).toHaveBeenCalled()
    })
  })

  describe('when next clicked', function () {
    beforeEach(function () {
      spyOn(view, 'next');
      (view.delegateEvents)()
      return (view.render)()
    })

    return it('next method called', function () {
      view.$('#editorNext').trigger('click')
      return expect(view.next).toHaveBeenCalled()
    })
  })

  describe('when back clicked', function () {
    beforeEach(function () {
      spyOn(view, 'back');
      (view.delegateEvents)()
      return (view.render)()
    })

    return it('back clicked', function () {
      view.$('#editorBack').prop('disabled', false).trigger('click')
      return expect(view.back).toHaveBeenCalled()
    })
  })

  describe('when save clicked', function () {
    beforeEach(function () {
      spyOn(view, 'save').and.callThrough()
      spyOn(model, 'save');
      (view.delegateEvents)()
      return (view.render)()
    })

    return it('clicking Save calls method', function () {
      view.$('#editorSave').prop('disabled', false).trigger('click')
      expect(view.save).toHaveBeenCalled()
      return expect(model.save).toHaveBeenCalled()
    })
  })

  return describe('when delete clicked', function () {
    beforeEach(function () {
      spyOn(model, 'destroy')
      return (view.render)()
    })

    return it('clicking Delete calls method', function () {
      view.$('#confirmDeleteYes').trigger('click')
      return expect(model.destroy).toHaveBeenCalled()
    })
  })
}))
