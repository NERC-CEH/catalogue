import $ from 'jquery'
import { DropzoneView } from '../src/Upload'
import { File } from '../src/File'

describe('Test DropzoneView', function () {
  let view = null

  beforeEach(function () {
    const dropzoneSuccess = file => {
      const filename = file.name.toLowerCase().replaceAll(' ', '-')
      const model = new File({
        bytes: file.size,
        name: filename,
        path: `/dropbox/${this.model.get('id')}/${filename}`,
        status: 'WRITING',
        check: true
      })
      this.dropbox.add(model)
      return $(file.previewElement).remove()
    }
    const container = $('<div class ="dropzone-container"><div class="dropzone-files"><div class="fileinput-button"/></div></div>')
    $(document.body).html(container)

    view = new DropzoneView({
      el: '.dropzone-container',
      success: dropzoneSuccess,
      url: '/file/post'
    })
  })

  it('is defined and dropzoneOptions works', function () {
    // then
    expect(view).toBeDefined()
  })
})
