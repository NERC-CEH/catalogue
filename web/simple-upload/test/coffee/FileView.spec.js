/* eslint-disable
    no-return-assign,
    no-undef,
    no-unused-vars,
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
  'backbone',
  'cs!views/upload/simple/FileView'
], ($, Backbone, FileView) => describe('FileView', function () {
  let el = null
  const collection = null
  let model = null
  let view = null

  beforeEach(function () {
    el = $('ul').appendTo($('body'))
    spyOn(FileView.prototype, 'remove').and.callThrough()
    spyOn(FileView.prototype, 'render').and.callThrough()
    model = new Backbone.Model({
      message: 'Hello World',
      type: 'info'
    })
    return view = new FileView({
      el,
      model
    })
  })

  afterEach(() => el.remove())

  it('has DOM events', function () {
    expect(view.events['change input']).toBeDefined()
    return expect(view.events['change input']).toEqual('select')
  })

  return it('rerenders when model changed', function () {
    // when
    model.set('toDelete', true)

    // then
    return expect(view.render).toHaveBeenCalled()
  })
}))
