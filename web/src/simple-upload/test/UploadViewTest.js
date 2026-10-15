import { afterEach, beforeEach, describe, expect, it } from 'vitest'
import $ from 'jquery'
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

  // dri-one #456: the server requires filename, which was only sent for files inside a dropped folder
  describe('sending', () => {
    function sentFilename (file) {
      const data = new FormData()
      view.dropzone.emit('sending', file, null, data)
      return data.get('filename')
    }

    it('sends the name of a file picked or dropped on its own', () => {
      expect(sentFilename({ name: 'data.csv' })).toBe('data.csv')
    })

    it('sends the path of a file inside a dropped folder', () => {
      expect(sentFilename({ name: 'data.csv', fullPath: 'folder/data.csv' })).toBe('folder/data.csv')
    })
  })

  it('lists an uploaded file under the path it was stored at', () => {
    view.dropzone.emit('success', { name: 'data.csv', fullPath: 'folder/data.csv' })

    // name is the File id
    expect(files.get('folder/data.csv')).toBeDefined()
    expect(files.get('data.csv')).toBeUndefined()
  })

  describe('reporting a failed upload', () => {
    function reported (error) {
      view.dropzone.emit('error', { name: 'data.csv' }, error)
      return messages.last().toJSON()
    }

    it('shows the message from the upload controller', () => {
      expect(reported({ message: 'data.csv already exists', type: 'error' }))
        .toEqual({ message: 'Failed to upload data.csv: data.csv already exists', type: 'error' })
    })

    it('shows a problem detail from the global error handler', () => {
      expect(reported({ title: 'Bad Request', detail: 'Required parameter filename is not present' }).message)
        .toBe('Failed to upload data.csv: Required parameter filename is not present')
    })

    it('shows a plain-text error, such as one Dropzone raises itself', () => {
      expect(reported('File is too big').message).toBe('Failed to upload data.csv: File is too big')
    })

    it('falls back to a general message', () => {
      expect(reported({}).message).toBe('Failed to upload data.csv')
    })
  })
})
