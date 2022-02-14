/* eslint-disable
    no-multi-str,
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
  'cs!views/upload/simple/AppView'
], AppView => describe('AppView', function () {
  const template =
    '<div id="messages"> \
<div id="messages-tools"></div> \
<ul id="messages-list"></ul> \
</div> \
<form id="simple-upload-dropzone" action="/upload/test"> \
</form> \
<div> \
<div id="files-tools"></div> \
<ul id="files-list"></ul> \
</div> \
<script id="files-data" type="application/json"> \
[ \
{"name": "data1.csv", "urlEncodedName": "data1.csv"}, \
{"name": "data two.csv", "urlEncodedName": "data%20two.csv"}, \
] \
</script> \
<script id="messages-data" type="application/json"> \
{"message": "Test message", "type": "info"} \
</script>'
  let el = null
  let view = null

  beforeEach(function () {
    el = $(template).appendTo($('body'))
    return view = new AppView({
      el,
      url: '/upload/test'
    })
  })

  afterEach(() => el.remove())

  xit('has properties', function () {
    expect(view.url).toBeDefined()
    expect(view.files).toBeDefined()
    return expect(view.messages).toBeDefined()
  })

  return xit('loads data from page', function () {
    expect(view.files.length).toEqual(2)
    return expect(view.messages.length).toEqual(1)
  })
}))
