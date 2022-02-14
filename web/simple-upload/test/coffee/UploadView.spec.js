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
  'backbone',
  'cs!collections/upload/simple/FileCollection',
  'cs!views/upload/simple/UploadView'
], ($, Backbone, FileCollection, UploadView) => describe('UploadView', function () {
  let el = null
  const template = '<form action="/upload/test" id="simple-upload-dropzone"></form>'
  let files = null
  let messages = null
  let view = null

  beforeEach(function () {
    el = $(template).appendTo($('body'))
    files = new FileCollection({ url: '/upload/test' })
    messages = new Backbone.Collection()
    return view = new UploadView({
      el,
      files,
      messages
    })
  })

  afterEach(() => el.remove())

  return xit('renders', () => expect(view).toBeDefined())
}))
