import $ from 'jquery'
import { SimpleUploadView } from '../src/App'

describe('Test SimpleUploadView', function () {
  const template =
        `<div id="messages">
<div id="messages-tools"></div>
<ul id="messages-list"></ul>
</div>
<form id="simple-upload-dropzone" action="/upload/test">
</form>
<div>
<div id="files-tools"></div>
<ul id="files-list"></ul>
</div>`

  it('is defined', function () {
    const el = $(template).appendTo($('body'))
    const view = new SimpleUploadView({
      el,
      url: '/upload/test'
    })
    expect(view).toBeDefined()
  })
})
