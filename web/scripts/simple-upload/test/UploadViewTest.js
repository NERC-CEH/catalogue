
import Backbone from 'backbone'
import { FileCollection, UploadView } from '../src/File'

describe('Test UploadView', function () {
  let el = null
  const template = '<form action="/upload/test" id="simple-upload-dropzone"></form>'
  let files = null
  let messages = null
  let view = null

  beforeEach(function () {
    el = $(template).appendTo($('body'))
    files = new FileCollection({ url: '/upload/test' })
    messages = new Backbone.Collection()
    view = new UploadView({
      el,
      files,
      messages
    })
  })

  afterEach(() => el.remove())

  it('renders', () => expect(view).toBeDefined())
})
