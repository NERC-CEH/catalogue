import $ from 'jquery'
import { UploadModel, UploadView, DropzoneView } from '../../src/Upload'
import { File } from '../../src/File'
import { fakeClock, fakeServer } from '../../../test/support'

/**
 * The upload page could lose a depositor's work (NERC-CEH/dri-one#459): "Finish uploading" aborted
 * uploads in flight and removed the upload permission without asking, a finished zip reloaded the
 * page mid-queue, failed refreshes left spinners and stale rows, oversized files uploaded in full
 * before being refused, and errors showed no reason.
 */
describe('Upload page safety', () => {
  const id = 'ebaf006d-b014-4d8b-9131-7677e61519dd'
  let view

  function build (html) {
    $(document.body).html(`<div class="document-upload">${html}</div>`)
    view = new UploadView({ el: '.document-upload', model: new UploadModel({ id }) })
  }

  beforeEach(() => fakeServer.install())
  afterEach(() => {
    fakeServer.uninstall()
    $(document.body).empty()
  })

  describe('finishing', () => {
    beforeEach(() => build('<button class="finish"><i class="fa-solid fa-check"></i></button>'))

    it('refuses while files are still uploading', () => {
      view.dropzoneView = { dropzone: { getUploadingFiles: () => [{}], getQueuedFiles: () => [{}, {}] } }
      spyOn(window, 'alert')
      spyOn(window, 'confirm')

      view.$('.finish').trigger('click')

      expect(window.alert).toHaveBeenCalledWith('3 files are still uploading. Wait for them to finish before you finish uploading.')
      expect(window.confirm).not.toHaveBeenCalled()
      expect(fakeServer.requestCount()).toBe(0)
    })

    it('asks first, and does nothing if the depositor says no', () => {
      spyOn(window, 'confirm').and.returnValue(false)

      view.$('.finish').trigger('click')

      expect(window.confirm).toHaveBeenCalled()
      expect(fakeServer.requestCount()).toBe(0)
    })

    it('finishes once confirmed', () => {
      spyOn(window, 'confirm').and.returnValue(true)

      view.$('.finish').trigger('click')

      expect(fakeServer.lastRequest().url).toBe(`/upload/${id}/finish`)
    })
  })

  it('asks before moving every file to the datastore', () => {
    build('<button class="move-all"><i class="fa-solid fa-arrow-right"></i></button>')
    spyOn(window, 'confirm').and.returnValue(false)

    view.$('.move-all').trigger('click')

    expect(window.confirm).toHaveBeenCalled()
    expect(fakeServer.requestCount()).toBe(0)
  })

  it('reloads after a zip only once the whole queue has finished', () => {
    build('')
    view.dropbox.reset([])
    spyOn(view, 'reloadPage')

    view.uploaded({ name: 'Data.zip', size: 10 })
    view.uploaded({ name: 'other.csv', size: 10 })
    expect(view.reloadPage).not.toHaveBeenCalled()

    view.queueCompleted()
    expect(view.reloadPage).toHaveBeenCalledTimes(1)
  })

  describe('refreshing', () => {
    beforeEach(() => {
      build('<button class="validate-all"><i class="fa-solid fa-check"></i></button><div class="data-files"></div>')
      fakeClock.install()
    })
    afterEach(() => fakeClock.uninstall())

    it('shows an error when the refresh after an action fails', () => {
      spyOn(view, 'showInError')

      view.collectionSuccess({ currentTarget: view.$('.validate-all')[0] }, 'fa-solid fa-check', 'dropbox', 'dropbox', view.dropbox)
      fakeClock.tick(7000)
      fakeServer.lastRequest().respondWith({ status: 500 })

      expect(view.showInError).toHaveBeenCalled()
    })

    it('stops spinning when there is nothing to validate', () => {
      spyOn(view, 'showNormal')

      view.validate({ currentTarget: view.$('.validate-all')[0] }, 'dropbox', 'dropbox', view.dropbox, 'fa-solid fa-check')
      fakeServer.lastRequest().respondWith({ status: 400 })

      expect(view.showNormal).toHaveBeenCalled()
    })

    it('clears the table when a refresh comes back empty', () => {
      view.dropbox.reset([{ datasetId: id, datastore: 'dropbox', path: 'old.csv', status: 'VALID', bytes: 1 }])
      expect(view.$('.data-files').text()).toContain('old.csv')

      view.dropbox.reset([])

      expect(view.$('.data-files').text()).not.toContain('old.csv')
    })
  })

  it('updates the checksum and last-checked time after a validate', () => {
    const file = new File({ datasetId: id, datastore: 'dropbox', path: 'a.csv', status: 'NO_HASH', bytes: 1 })

    file.update({ datasetId: id, datastore: 'dropbox', path: 'a.csv', status: 'VALID', bytes: 2048, sha256: 'abc123', lastValidated: '2026-10-13T10:00:00' })

    expect(file.get('sha256')).toBe('abc123')
    expect(file.get('lastValidated')).toBe('2026-10-13T10:00:00')
    expect(file.get('size')).toBe('2.05 kB')
  })

  describe('the dropzone', () => {
    let dropzoneView

    beforeEach(() => {
      $(document.body).html('<div class="dropzone-container" data-max-file-size="2147483648"><div class="dropzone-files"></div><div class="fileinput-button"></div></div>')
      dropzoneView = new DropzoneView({ el: '.dropzone-container', success: () => {}, complete: () => {}, url: '/upload/x' })
    })

    it('refuses a file over the server limit before uploading it', () => {
      // 2 GiB in Dropzone's MiB
      expect(dropzoneView.dropzone.options.maxFilesize).toBe(2048)
    })

    it('shows why an upload failed', () => {
      const message = (error, xhr) => {
        const file = { previewElement: $('<div><span class="file-status"></span><span class="file-name"><i></i></span><button class="cancel"></button><span class="file-message"></span></div>')[0] }
        dropzoneView.dropzone.emit('error', file, error, xhr)
        return $(file.previewElement).find('.file-message').text()
      }

      expect(message('File is too big (3000MiB). Max filesize: 2048MiB.')).toBe('File is too big (3000MiB). Max filesize: 2048MiB.')
      expect(message({ title: 'Conflict', detail: 'data/a.csv already exists' }, { status: 409 })).toBe('data/a.csv already exists')
      expect(message('Filename is blank in upload to x', { status: 400 })).toBe('Filename is blank in upload to x')
      expect(message({}, { status: 403 })).toBe('Unauthorized')
    })
  })
})
