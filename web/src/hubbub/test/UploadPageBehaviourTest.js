import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import $ from 'jquery'
import { UploadModel, UploadView } from '../src/Upload'
import { fakeServer } from '../../test/support'

// The data upload page as an EIDC administrator sees it (templates/html/upload/hubbub/upload.ftlh):
// each store's files in a table, what each file can have done to it, those actions carried out and
// the tables kept up to date, and the buttons acting on a whole store. Hubbub is faked; its answers
// are shaped as it sends them (dri-one #473).
describe('The upload page', () => {
  const id = 'ebaf006d-b014-4d8b-9131-7677e61519dd'
  const base = `/upload/${id}`
  let view

  const file = (path, datastore, status, extra = {}) => ({ path, datastore, status, bytes: 2048, datasetId: id, ...extra })
  const store = (files, meta = {}) => JSON.stringify({ data: files, meta: { currentPage: 1, lastPage: 1, pageSize: 20, totalFiles: files.length, ...meta } })
  const json = body => ({ status: 200, contentType: 'application/json', responseText: JSON.stringify(body) })
  const button = (classes, icon) => `<button class="btn btn-uploader ${classes}"><i class="btn-icon fa-fw fa-${icon} fa-solid"></i></button>`
  const section = (name, title, inner) => `
    <section class="document-upload-section ${name}">
      <div class="section-header">${title} <span class="total-files"></span></div>
      ${inner}
    </section>`
  const files = name => `<div class="${name}-files"><h3 class="no-documents text-center">NO FILES IN ${name.toUpperCase()}</h3></div>`

  const arrivals = [
    file('rain/2020.csv', 'dropbox', 'VALID', { sha256: 'abc123', lastValidated: '2026-10-01T09:30:00' }),
    file('rain/2021.csv', 'dropbox', 'CHANGED_HASH'),
    file('rain/2022.csv', 'dropbox', 'MOVING_TO'),
    file('rain/old.csv', 'dropbox', 'MISSING')
  ]

  function open ({ dropbox = store(arrivals), datastore = store([file('rain/2019.csv', 'eidchub', 'VALID')]), metadata = store([]) } = {}) {
    $(document.body).html(`
      <div class="container is-admin" id="document-upload" data-guid="${id}">
        <div class="container document-upload">
          ${section('upload', 'Upload', `
            <div class="dropzone-container" data-max-file-size="1073741824">
              <div class="dropzone-files"></div>
              ${button('fileinput-button', 'file-upload')}
              ${button('finish', 'check')}
            </div>`)}
          ${section('dropbox', 'Arrivals', `${files('data')} ${button('load data', 'download')} ${button('data-action move-all', 'level-down-alt')} ${button('data-action hash-dropbox', 'wave-square')}`)}
          ${button('data-action register-dropbox', 'folder-plus')}
          ${section('metadata', 'Supporting documents', `${files('metadata')} ${button('load metadata', 'download')}`)}
          ${section('datastore', 'Datastore', `${files('datastore')} ${button('load datastore', 'download')}`)}
          ${button('file-action validate-all', 'check')}
        </div>
      </div>
      <script id="dropbox-data" type="application/json">${dropbox}</script>
      <script id="datastore-data" type="application/json">${datastore}</script>
      <script id="metadata-data" type="application/json">${metadata}</script>`)
    // As initHubbub does
    view = new UploadView({ el: '.document-upload', model: new UploadModel({ id: $('#document-upload').data('guid') }) })
  }

  const rows = name => $(`.${name}-files tbody tr`).filter((i, row) => !$(row).find('.dt-empty').length)
  const row = (name, path) => rows(name).filter((i, tr) => $(tr).find('td').first().text() === path)
  const paths = name => rows(name).map((i, tr) => $(tr).find('td').first().text()).get()
  const actions = $row => $row.find('.dropdown-item').map((i, item) => item.textContent).get()
  const act = ($row, action) => $row.find(`.dropdown-item[data-action="${action}"]`).trigger('click')
  const total = name => $(`.document-upload-section.${name} .total-files`).text()
  const isWarning = $button => $button.find('i').hasClass('fa-exclamation-triangle')

  beforeEach(() => {
    fakeServer.install()
    vi.useFakeTimers()
  })

  afterEach(() => {
    view.dropzoneView?.dropzone.destroy()
    vi.useRealTimers()
    fakeServer.uninstall()
    $(document.body).empty()
  })

  describe('on arrival', () => {
    beforeEach(() => open())

    it('shows each store\'s files and how many there are', () => {
      expect(paths('data')).toEqual(['rain/2021.csv', 'rain/old.csv', 'rain/2022.csv', 'rain/2020.csv'])
      expect(paths('datastore')).toEqual(['rain/2019.csv'])
      expect(total('dropbox')).toBe('4 files')
      expect(total('datastore')).toBe('1 file')
    })

    it('says so when a store is empty, and offers no more to load', () => {
      expect($('.metadata-files .no-documents').text()).toBe('NO FILES IN METADATA')
      expect(total('metadata')).toBe('0 files')
      expect($('.load.metadata').css('display')).toBe('none')
    })

    it('shows a file\'s path as folders, its checksum, and when it was last checked', () => {
      const $cells = row('data', 'rain/2020.csv').find('td')

      expect($cells.eq(0).find('.folder-separator').length).toBe(1)
      expect($cells.eq(2).find('span').attr('title')).toBe('abc123')
      expect($cells.eq(4).text()).toBe('2026-10-01 09:30')
    })

    it('marks a file with a problem, and explains it', () => {
      const $missing = row('data', 'rain/old.csv')

      expect($missing.hasClass('table-danger')).toBe(true)
      expect($missing.find('.file-status-message').text()).toBe('THIS FILE IS MISSING')
      expect(row('data', 'rain/2020.csv').hasClass('table-danger')).toBe(false)
    })

    it('offers each file what can be done to it', () => {
      expect(actions(row('data', 'rain/2020.csv'))).toEqual(['Validate', 'Move to datastore', 'Move to metadata', 'Delete'])
      expect(actions(row('data', 'rain/2021.csv'))).toEqual(['Validate', 'Accept', 'Delete'])
      expect(actions(row('data', 'rain/2022.csv'))).toEqual(['Validate', 'Cancel', 'Delete'])
      expect(actions(row('data', 'rain/old.csv'))).toEqual(['Validate', 'Ignore', 'Delete'])
      expect(actions(row('datastore', 'rain/2019.csv'))).toEqual(['Validate', 'Move to metadata'])
    })

    it('filters a store by status, or by part of a file\'s name', () => {
      const $status = $('.data-files .status-filter')
      expect($status.find('option').map((i, option) => option.value).get()).toEqual(['', 'CHANGED_HASH', 'MISSING', 'MOVING_TO', 'VALID'])

      $status.val('MISSING').trigger('change')
      expect(paths('data')).toEqual(['rain/old.csv'])
      $status.val('').trigger('change')

      const $search = $('.data-files .column-search').first()
      $search.val('2021').trigger('keyup')
      expect(paths('data')).toEqual(['rain/2021.csv'])
      expect($search.siblings('.clear-search').css('display')).not.toBe('none')

      $search.siblings('.clear-search').trigger('click')
      expect(paths('data').length).toBe(4)
      expect($search.val()).toBe('')
    })
  })

  it('sorts by size as sizes, not as text', () => {
    open({ dropbox: store([file('a.csv', 'dropbox', 'VALID', { bytes: 900 }), file('b.csv', 'dropbox', 'VALID', { bytes: 5 * 1024 * 1024 }), file('c.csv', 'dropbox', 'VALID', { bytes: 20 * 1024 })]) })

    $('.data-files thead tr').first().find('th').eq(1).trigger('click')
    // DataTables redraws for a header click on the next turn
    vi.advanceTimersByTime(1)

    expect(paths('data')).toEqual(['a.csv', 'c.csv', 'b.csv'])
  })

  describe('a file\'s actions', () => {
    beforeEach(() => open())

    it('accepts a changed file, then shows it as Hubbub now has it', () => {
      act(row('data', 'rain/2021.csv'), 'Accept')
      expect(fakeServer.lastRequest().method).toBe('POST')
      expect(fakeServer.lastRequest().url).toBe(`${base}/dropbox/accept?path=rain%2F2021.csv`)
      expect(row('data', 'rain/2021.csv').find('.spinner-border').css('display')).not.toBe('none')
      fakeServer.lastRequest().respondWith(json({}))

      vi.advanceTimersByTime(3000)
      expect(fakeServer.lastRequest().url).toBe(`${base}/dropbox?path=rain%2F2021.csv`)
      fakeServer.lastRequest().respondWith(json({ data: [file('rain/2021.csv', 'dropbox', 'VALID', { sha256: 'def456' })] }))

      const $row = row('data', 'rain/2021.csv')
      expect($row.hasClass('table-danger')).toBe(false)
      expect(actions($row)).toEqual(['Validate', 'Move to datastore', 'Move to metadata', 'Delete'])
    })

    it('moves a file to the datastore, from one table to the other', () => {
      act(row('data', 'rain/2020.csv'), 'Move to datastore')
      expect(fakeServer.lastRequest().url).toBe(`${base}/dropbox/move?path=rain%2F2020.csv&to=eidchub`)
      fakeServer.lastRequest().respondWith(json({}))
      vi.advanceTimersByTime(3000)
      expect(fakeServer.lastRequest().url).toBe(`${base}/eidchub?path=rain%2F2020.csv`)
      fakeServer.lastRequest().respondWith(json({ data: [file('rain/2020.csv', 'eidchub', 'VALID')] }))

      expect(paths('data')).not.toContain('rain/2020.csv')
      expect(paths('datastore')).toEqual(['rain/2019.csv', 'rain/2020.csv'])
      expect(total('dropbox')).toBe('3 files')
      expect(total('datastore')).toBe('2 files')
    })

    it('moves a file to the empty supporting documents, which then gets a table of its own', () => {
      act(row('data', 'rain/2020.csv'), 'Move to metadata')
      expect(fakeServer.lastRequest().url).toBe(`${base}/dropbox/move?path=rain%2F2020.csv&to=supporting-documents`)
      fakeServer.lastRequest().respondWith(json({}))
      vi.advanceTimersByTime(3000)
      fakeServer.lastRequest().respondWith(json({ data: [file('rain/2020.csv', 'supporting-documents', 'VALID')] }))

      expect(paths('metadata')).toEqual(['rain/2020.csv'])
      expect($('.metadata-files .no-documents').length).toBe(0)
      expect(total('metadata')).toBe('1 file')
    })

    it('deletes a file once confirmed, and not otherwise', () => {
      const confirm = vi.spyOn(window, 'confirm').mockReturnValue(false)
      act(row('data', 'rain/old.csv'), 'Delete')
      expect(confirm).toHaveBeenCalledWith('Delete file: rain/old.csv?')
      expect(fakeServer.requestCount()).toBe(0)

      confirm.mockReturnValue(true)
      act(row('data', 'rain/old.csv'), 'Delete')
      expect(fakeServer.lastRequest().method).toBe('DELETE')
      expect(fakeServer.lastRequest().url).toBe(`${base}/dropbox?path=rain%2Fold.csv`)
      fakeServer.lastRequest().respondWith({ status: 204, contentType: 'application/json', responseText: '' })

      expect(paths('data')).not.toContain('rain/old.csv')
      expect(total('dropbox')).toBe('3 files')
    })

    it('unregisters a file it is told to ignore', () => {
      vi.spyOn(window, 'confirm').mockReturnValue(true)

      act(row('data', 'rain/old.csv'), 'Ignore')
      expect(fakeServer.lastRequest().url).toBe(`${base}/dropbox/unregister?path=rain%2Fold.csv`)
      fakeServer.lastRequest().respondWith(json({}))

      expect(paths('data')).not.toContain('rain/old.csv')
    })

    it('cancels a move once confirmed', () => {
      vi.spyOn(window, 'confirm').mockReturnValue(true)

      act(row('data', 'rain/2022.csv'), 'Cancel')

      expect(window.confirm).toHaveBeenCalledWith('Cancel moving file: rain/2022.csv?')
      expect(fakeServer.lastRequest().url).toBe(`${base}/dropbox/cancel?path=rain%2F2022.csv`)
    })

    it('shows a warning, and lets the action be tried again, when it fails', () => {
      const $row = row('data', 'rain/2021.csv')
      act($row, 'Accept')
      fakeServer.lastRequest().respondWith({ status: 500, contentType: 'text/plain', responseText: '' })

      expect($row.find('.action-fail').css('display')).not.toBe('none')
      expect($row.find('.spinner-border').css('display')).toBe('none')
      expect($row.find('.dropdown-toggle').prop('disabled')).toBe(false)
    })

    it('shows a warning when Hubbub no longer has the file afterwards', () => {
      const $row = row('data', 'rain/2021.csv')
      act($row, 'Validate')
      fakeServer.lastRequest().respondWith(json({}))
      vi.advanceTimersByTime(3000)

      fakeServer.lastRequest().respondWith(json({ data: [] }))

      expect($row.find('.action-fail').css('display')).not.toBe('none')
      expect($row.find('.spinner-border').css('display')).toBe('none')
    })
  })

  describe('totals', () => {
    it('says "0 files" once the last file has gone', () => {
      open({ dropbox: store([file('only.csv', 'dropbox', 'MISSING')]) })
      vi.spyOn(window, 'confirm').mockReturnValue(true)

      act(row('data', 'only.csv'), 'Delete')
      fakeServer.lastRequest().respondWith({ status: 204, contentType: 'application/json', responseText: '' })

      expect(total('dropbox')).toBe('0 files')
    })
  })

  describe('loading more', () => {
    it('adds the next page to the table, and hides the button after the last', () => {
      open({ dropbox: store(arrivals.slice(0, 2), { lastPage: 2, pageSize: 2, totalFiles: 3 }) })
      expect($('.load.data').css('display')).not.toBe('none')

      $('.load.data').trigger('click')
      expect(fakeServer.lastRequest().url).toBe(`${base}/dropbox?page=2&size=2`)
      fakeServer.lastRequest().respondWith(json({ data: [arrivals[3]], meta: { currentPage: 2, lastPage: 2 } }))

      expect(paths('data')).toContain('rain/old.csv')
      expect(paths('data').length).toBe(3)
      expect($('.load.data').css('display')).toBe('none')
    })

    it('shows a warning when the next page can\'t be loaded', () => {
      open({ dropbox: store(arrivals.slice(0, 2), { lastPage: 2, pageSize: 2 }) })

      $('.load.data').trigger('click')
      fakeServer.lastRequest().respondWith({ status: 502, contentType: 'text/plain', responseText: '' })

      expect(isWarning($('.load.data'))).toBe(true)
    })
  })

  describe('whole stores', () => {
    beforeEach(() => open())

    it('validates every store, and refreshes each from Hubbub a few seconds later', () => {
      $('.validate-all').trigger('click')

      expect(fakeServer.requestCount()).toBe(3)
      expect($('.validate-all').prop('disabled')).toBe(true)
    })

    it('keeps the warning when one store fails to validate, though the others succeed', () => {
      const $button = $('.validate-all')
      $button.trigger('click')
      const requests = ['dropbox', 'supporting-documents', 'eidchub']
        .map(name => ({ name, request: findRequest(`${base}/${name}/validate`) }))

      requests[0].request.respondWith({ status: 500, contentType: 'text/plain', responseText: '' })
      requests[1].request.respondWith({ status: 400, contentType: 'text/plain', responseText: '' })
      requests[2].request.respondWith(json({}))
      vi.advanceTimersByTime(7000)
      findRequest(`${base}/eidchub?page=1&size=20`).respondWith(json({ data: [file('rain/2019.csv', 'eidchub', 'VALID')], meta: { currentPage: 1, lastPage: 1, totalFiles: 1 } }))

      expect(isWarning($button)).toBe(true)
    })

    it('moves every arrival to the datastore once confirmed, and says the arrivals are empty as the page does', () => {
      vi.spyOn(window, 'confirm').mockReturnValue(true)

      $('.move-all').trigger('click')
      expect(fakeServer.lastRequest().url).toBe(`${base}/dropbox/move?to=eidchub`)
      fakeServer.lastRequest().respondWith(json({}))
      vi.advanceTimersByTime(7000)
      fakeServer.lastRequest().respondWith(json({ data: [file('rain/2019.csv', 'eidchub', 'VALID'), file('rain/2020.csv', 'eidchub', 'VALID')], meta: { currentPage: 1, lastPage: 1, totalFiles: 2 } }))

      expect(paths('data')).toEqual([])
      expect($('.data-files .no-documents').text()).toBe('NO FILES IN DATA')
      expect(total('dropbox')).toBe('0 files')
      expect(paths('datastore')).toEqual(['rain/2019.csv', 'rain/2020.csv'])
    })

    it('says the arrivals are empty as the page does, when moving all from an empty store', () => {
      $(document.body).empty()
      open({ dropbox: store([]) })
      vi.spyOn(window, 'confirm').mockReturnValue(true)

      $('.move-all').trigger('click')
      fakeServer.lastRequest().respondWith(json({}))
      vi.advanceTimersByTime(7000)
      fakeServer.lastRequest().respondWith(json({ data: [file('rain/2019.csv', 'eidchub', 'VALID')], meta: { currentPage: 1, lastPage: 1, totalFiles: 1 } }))

      expect($('.data-files .no-documents').map((i, message) => message.textContent).get()).toEqual(['NO FILES IN DATA'])
    })

    it('generates checksums for the arrivals, then refreshes them', () => {
      $('.hash-dropbox').trigger('click')
      expect(fakeServer.lastRequest().url).toBe(`${base}/hash`)
      fakeServer.lastRequest().respondWith(json({}))
      vi.advanceTimersByTime(7000)

      expect(fakeServer.lastRequest().url).toBe(`${base}/dropbox?page=1&size=20`)
    })
  })

  it('adds uploaded files to the arrivals once the queue is done', () => {
    open()
    const dropzone = view.dropzoneView.dropzone

    dropzone.emit('success', { name: 'Rain 2023.csv', size: 4096, previewElement: document.createElement('div') })
    dropzone.emit('success', { name: 'notes.txt', fullPath: 'docs/Notes.txt', size: 10, previewElement: document.createElement('div') })
    expect(paths('data')).not.toContain('rain-2023.csv')
    dropzone.emit('queuecomplete')

    expect(paths('data')).toContain('rain-2023.csv')
    expect(paths('data')).toContain('docs/notes.txt')
    expect(total('dropbox')).toBe('6 files')
  })

  // The fake server's requests in the order they were made, to answer one that isn't the latest
  function findRequest (url) {
    const request = fakeServer.requests().find(each => each.url === url)
    expect(request, url).toBeDefined()
    return request
  }
})
