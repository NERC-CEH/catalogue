import { afterEach, beforeEach, describe, expect, it } from 'vitest'
import { vi } from 'vitest'
import $ from 'jquery'
import { DataPreviewView } from '../src'
import { fakeServer } from '../../test/support'

const PACKAGE = '/download/abc?url=https://data-package.ceh.ac.uk/data/abc'
const DOCUMENTS = 'https://data-package.ceh.ac.uk/sd/abc.zip'

// The markup the record page renders (Task 7), in its section and contents entry
const page = ({ map = false, packageUrl = PACKAGE, documentsUrl = DOCUMENTS } = {}) => `
  <div id="page">
    <div id="tableOfContents"><a class="list-group-item" href="#segment-map">Data Preview</a></div>
    <div id="segment-map">
      <h2>Data Preview</h2>
      ${map ? '<div class="wms-map-preview"></div>' : ''}
      <div class="data-preview" data-record-id="abc" data-package-url="${packageUrl}" data-supporting-documents-url="${documentsUrl}">
        <div class="row g-3">
          <div class="col-lg-7 data-preview-data"></div>
          <div class="col-lg-5 data-preview-documents"></div>
        </div>
      </div>
    </div>
  </div>`

const entry = (path, label = path) => ({ path, label, bytes: 10, sha256: 'x' })
const manifest = (overrides = {}) => ({
  dataFiles: [entry('a.csv'), entry('fish_biometry .csv', 'fish_biometry')],
  dataCount: 2,
  dataCountIsMinimum: false,
  totalFiles: 2,
  documents: [],
  ...overrides
})
const sample = (overrides = {}) => ({ header: ['h1', 'h2', 'h3', 'h4', 'h5', 'h6'], rows: [['1', '2', '3', '4', '5', '6']], moreRows: false, previewable: true, ...overrides })
// The data endpoint's answer for a table: a DocumentPreview of kind TABLE
const dataTable = (overrides = {}) => ({ kind: 'TABLE', text: null, table: sample(overrides), truncated: false })
const json = (body, status = 200) => ({ status, responseText: JSON.stringify(body) })

describe('DataPreviewView', () => {
  let $page
  let view

  beforeEach(() => fakeServer.install())
  afterEach(() => {
    view?.remove()
    view = null
    $page?.remove()
    fakeServer.uninstall()
  })

  const start = (html = page()) => {
    $page = $(html).appendTo(document.body)
    view = new DataPreviewView({ el: $page.find('.data-preview')[0] })
    return view
  }
  const urls = () => fakeServer.requests().map(request => request.url)
  const answer = (matcher, response) => {
    const request = fakeServer.requests().find(each => each.url.includes(matcher) && !each.status)
    expect(request, matcher).toBeTruthy()
    request.respondWith(response)
  }
  const text = selector => $page.find(selector).text()
  const tabs = selector => $page.find(`${selector} [role=tab]`).map((i, tab) => $(tab).text()).get()
  const manifestThen = async (manifestBody, dataBody = dataTable()) => {
    start()
    answer('/documents/abc/data-preview', json(manifestBody))
    await vi.waitFor(() => expect(urls().some(url => url.includes('/data?'))).toBe(true))
    answer('/data?', json(dataBody))
    // Not view.ready: that waits for the first document too, which a spec answers itself
    await vi.waitFor(() => expect($page.find('.data-preview-data table, .data-preview-data p').length).toBeGreaterThan(0))
  }

  it('shows Loading preview… while the manifest is pending', () => {
    start()

    expect(urls()).toEqual(['/documents/abc/data-preview'])
    expect(text('.data-preview')).toContain('Loading preview…')
  })

  it('shows Loading preview… in the documents pane during the manifest load too', () => {
    start()

    expect(text('.data-preview-documents')).toBe('Loading preview…')
  })

  it('shows a placeholder, not the previous file, while an uncached tab loads', async () => {
    await manifestThen(manifest())

    $page.find('.data-preview-data [role=tab]').eq(1).trigger('click')

    expect(text('.data-preview-data .data-preview-panel')).toBe('Loading preview…')
    expect($page.find('.data-preview-data table').length).toBe(0)
  })

  it('makes a tab per CSV file, selects the first and fetches it', async () => {
    await manifestThen(manifest())

    expect(tabs('.data-preview-data')).toEqual(['a.csv', 'fish_biometry'])
    expect($page.find('.data-preview-data [role=tab]').first().attr('aria-selected')).toBe('true')
    expect($page.find('.data-preview-data [role=tab]').last().attr('aria-selected')).toBe('false')
    expect(urls()).toContain('/documents/abc/data-preview/data?path=a.csv')
    expect($page.find('.data-preview-data table th').length).toBe(6)
  })

  it('URL-encodes the path, fetches another tab once and reuses it', async () => {
    await manifestThen(manifest())
    const second = () => $page.find('.data-preview-data [role=tab]').eq(1)
    const first = () => $page.find('.data-preview-data [role=tab]').eq(0)

    second().trigger('click')
    expect(urls()).toContain('/documents/abc/data-preview/data?path=fish_biometry%20.csv')
    answer('fish_biometry%20.csv', json(dataTable({ header: ['only'], rows: [['x']] })))
    await vi.waitFor(() => expect($page.find('.data-preview-data table th').length).toBe(1))
    first().trigger('click')
    await vi.waitFor(() => expect($page.find('.data-preview-data table th').length).toBe(6))
    const before = fakeServer.requestCount()
    second().trigger('click')
    await vi.waitFor(() => expect($page.find('.data-preview-data table th').length).toBe(1))

    expect(fakeServer.requestCount()).toBe(before)
    expect(second().attr('aria-selected')).toBe('true')
  })

  const row = ['1', '2', '3', '4', '5', '6']
  const sized = bytes => manifest({ dataFiles: [{ ...entry('a.csv'), bytes }], dataCount: 1, totalFiles: 1 })
  const note = () => text('.data-preview-data .data-preview-note')

  it('says how many rows and columns, and the file size, when there are more rows', async () => {
    await manifestThen(sized(393216), dataTable({ moreRows: true, rows: Array.from({ length: 50 }, () => row) }))

    expect(note()).toContain('First 50 rows of a 384 KB file · 6 columns')
  })

  it('says how many rows and columns, and the file size, when it has them all', async () => {
    await manifestThen(sized(1229), dataTable({ rows: Array.from({ length: 6 }, () => row) }))

    expect(note()).toContain('All 6 rows of a 1.2 KB file · 6 columns')
  })

  it('leaves the size out when the listing gives none', async () => {
    await manifestThen(sized(0), dataTable({ rows: [row, row, row] }))

    expect(note()).toContain('All 3 rows · 6 columns')
  })

  it('links the record\'s download beside a table', async () => {
    await manifestThen(manifest())

    const $link = $page.find('.data-preview-data .data-preview-note a')
    expect($link.text()).toBe('Download the data')
    expect($link.attr('href')).toBe(PACKAGE)
  })

  it('has no download link beside a table when there is no package link', async () => {
    $page = $(page({ packageUrl: '' })).appendTo(document.body)
    view = new DataPreviewView({ el: $page.find('.data-preview')[0] })
    answer('/documents/abc/data-preview', json(manifest()))
    await vi.waitFor(() => expect(urls().some(url => url.includes('/data?'))).toBe(true))
    answer('/data?', json(dataTable()))
    await view.ready

    expect($page.find('.data-preview-data table').length).toBe(1)
    expect(note()).toContain('All 1 row')
    expect($page.find('.data-preview-data a').length).toBe(0)
  })

  it('links the full download when there are more CSV files than tabs', async () => {
    const dataFiles = Array.from({ length: 10 }, (i, n) => entry(`f${n}.csv`))
    await manifestThen(manifest({ dataFiles, dataCount: 54, totalFiles: 54 }))

    const $link = $page.find('.data-preview-data .data-preview-footer a')
    expect($link.text()).toBe('Showing 10 of 54 previewable files · 54 files in this dataset, all in the full download')
    expect($link.attr('href')).toBe(PACKAGE)
  })

  it('says at least when the count is a minimum', async () => {
    const dataFiles = Array.from({ length: 10 }, (i, n) => entry(`f${n}.csv`))
    await manifestThen(manifest({ dataFiles, dataCount: 54, dataCountIsMinimum: true, totalFiles: 54 }))

    expect(text('.data-preview-data .data-preview-footer a')).toContain('Showing 10 of at least 54 previewable files')
  })

  it('has no footer when every CSV file has a tab', async () => {
    await manifestThen(manifest())

    expect($page.find('.data-preview-data .data-preview-footer').length).toBe(0)
    expect(text('.data-preview-data')).not.toContain('Showing')
  })

  it('shows the footer as plain text when there is no package link', async () => {
    $page = $(page({ packageUrl: '' })).appendTo(document.body)
    view = new DataPreviewView({ el: $page.find('.data-preview')[0] })
    answer('/documents/abc/data-preview', json(manifest({ dataCount: 5, totalFiles: 5 })))
    await vi.waitFor(() => expect(urls().some(url => url.includes('/data?'))).toBe(true))
    answer('/data?', json(dataTable()))
    await view.ready

    expect(text('.data-preview-data')).toContain('Showing 2 of 5 previewable files')
    expect($page.find('.data-preview a').length).toBe(0)
  })

  it('says a file can\'t be previewed, with the package link, not an empty table', async () => {
    await manifestThen(manifest(), { kind: 'NOT_PREVIEWABLE', text: null, table: null, truncated: false })

    expect($page.find('.data-preview-data table').length).toBe(0)
    expect(text('.data-preview-data')).toContain('This file can\'t be previewed')
    expect($page.find('.data-preview-data a').attr('href')).toBe(PACKAGE)
  })

  it('shows a plain-text data file that is not a table as text, escaped', async () => {
    await manifestThen(manifest(), { kind: 'TEXT', text: 'Read me <b>first</b>', table: null, truncated: true })

    const $pre = $page.find('.data-preview-data pre')
    expect($pre.text()).toBe('Read me <b>first</b>')
    expect($pre.find('b').length).toBe(0)
    expect(text('.data-preview-data')).toContain('Showing the start of this file')
    expect($page.find('.data-preview-data table').length).toBe(0)
  })

  it('names the sheet shown and the others for a workbook', async () => {
    await manifestThen(manifest(), dataTable({ sheet: 'Data', sheets: ['Cover', 'Data', 'Notes'] }))

    expect(text('.data-preview-data .data-preview-sheets')).toBe('Sheet “Data” · also in this workbook: Cover, Notes')
    expect($page.find('.data-preview-data table').length).toBe(1)
  })

  it('has no sheet note for a workbook of one sheet, or a text table', async () => {
    await manifestThen(manifest(), dataTable({ sheet: 'Data', sheets: ['Data'] }))
    expect($page.find('.data-preview-sheets').length).toBe(0)
  })

  it('says the preview is busy on a 503 in the data pane', async () => {
    start()
    answer('/documents/abc/data-preview', json(manifest()))
    await vi.waitFor(() => expect(urls().some(url => url.includes('/data?'))).toBe(true))
    answer('/data?', json({ error: 'busy' }, 503))
    await vi.waitFor(() => expect(text('.data-preview-data')).toContain('The preview is busy, try again shortly'))

    expect($page.find('.data-preview-data a').attr('href')).toBe(PACKAGE)
  })

  it('shows a table result that is not previewable as can\'t be previewed', async () => {
    await manifestThen(manifest(), dataTable({ previewable: false, header: [], rows: [] }))

    expect(text('.data-preview-data')).toContain('This file can\'t be previewed')
  })

  describe('NetCDF summary', () => {
    const summary = (overrides = {}) => ({
      kind: 'SUMMARY',
      text: null,
      table: null,
      truncated: false,
      summary: {
        format: 'NetCDF-4 (HDF5)',
        attributes: [{ name: 'title', value: 'Daily <b>air</b> temperature' }, { name: 'Conventions', value: 'CF-1.8' }],
        attributesTruncated: false,
        dimensions: [{ name: 'time', length: 365, unlimited: true }, { name: 'lat', length: 2, unlimited: false }],
        variables: [
          { name: 'lat', longName: null, standardName: 'latitude', units: 'degrees_north', dimensions: 'lat', type: 'float', coordinate: true },
          { name: 'tas', longName: 'Air temperature', standardName: 'air_temperature', units: 'K', dimensions: 'time lat', type: 'float', coordinate: false }
        ],
        variablesTruncated: false,
        time: { variable: 'time', start: '2000-01-01T00:00:00Z', end: '2000-12-30T12:00:00Z', calendar: '360_day' },
        extents: [{ variable: 'lat', axis: 'latitude', units: 'degrees_north', min: 50.142877723, max: 52 }],
        ...overrides
      }
    })
    const ncManifest = (netcdfCount = 20) => manifest({ dataFiles: [{ ...entry('met/2000.nc', '2000.nc'), bytes: 1048576 }], dataCount: 1, totalFiles: 20, netcdfCount })

    it('says which of the NetCDF files it summarises, with format and size', async () => {
      await manifestThen(ncManifest(), summary())

      expect(text('.data-preview-data .data-preview-note')).toBe('Summary of the first of 20 NetCDF files · NetCDF-4 (HDF5) · 1 MB file · Download the data')
    })

    it('says the NetCDF file when there is only one', async () => {
      await manifestThen(ncManifest(1), summary())

      expect(text('.data-preview-data .data-preview-note')).toContain('Summary of the NetCDF file · ')
    })

    it('shows the title as text, and the time, extent and dimensions', async () => {
      await manifestThen(ncManifest(), summary())

      const $netcdf = $page.find('.data-preview-netcdf')
      expect($netcdf.find('p.fw-semibold').text()).toBe('Daily <b>air</b> temperature')
      expect($netcdf.find('b').length).toBe(0)
      const facts = $netcdf.find('ul li').map((i, li) => $(li).text()).get()
      expect(facts).toEqual([
        'Time: 2000-01-01 to 2000-12-30T12:00:00Z (360_day calendar)',
        'Latitude: 50.1429 to 52 degrees_north',
        'Dimensions: time 365 (unlimited) · lat 2'
      ])
    })

    it('lists data variables before coordinates', async () => {
      await manifestThen(ncManifest(), summary())

      const rows = $page.find('.data-preview-variables tbody tr').map((i, tr) => $(tr).find('td').map((j, td) => $(td).text()).get().join('|')).get()
      expect(rows).toEqual([
        'tas|Air temperature|K|time lat|float',
        'lat (coordinate)|latitude|degrees_north|lat|float'
      ])
    })

    it('keeps the global attributes in a collapsed list', async () => {
      await manifestThen(ncManifest(), summary({ attributesTruncated: true }))

      const $details = $page.find('.data-preview-attributes')
      expect($details.prop('open')).toBe(false)
      expect($details.find('summary').text()).toBe('Global attributes (2, first shown)')
      expect($details.find('dt').map((i, dt) => $(dt).text()).get()).toEqual(['title', 'Conventions'])
    })

    it('omits time, extent and attributes it does not have, and says when variables are cut', async () => {
      await manifestThen(ncManifest(), summary({ time: null, extents: [], attributes: [], variablesTruncated: true }))

      expect($page.find('.data-preview-netcdf ul li').map((i, li) => $(li).text()).get()).toEqual(['Dimensions: time 365 (unlimited) · lat 2'])
      expect($page.find('.data-preview-attributes').length).toBe(0)
      expect(text('.data-preview-netcdf')).toContain('Showing the first 2 variables')
    })

    it('gives a standard calendar no note', async () => {
      await manifestThen(ncManifest(), summary({ time: { variable: 't', start: '2000-01-01T00:00:00Z', end: '2001-01-01T00:00:00Z', calendar: 'standard' } }))

      expect(text('.data-preview-netcdf ul li:first')).toBe('Time: 2000-01-01 to 2001-01-01')
    })
  })

  it('omits the package link rather than render an empty href', async () => {
    $page = $(page({ packageUrl: '' })).appendTo(document.body)
    view = new DataPreviewView({ el: $page.find('.data-preview')[0] })
    answer('/documents/abc/data-preview', json({ ...manifest(), dataFiles: [entry('a.csv')] }))
    await vi.waitFor(() => expect(urls().some(url => url.includes('/data?'))).toBe(true))
    answer('/data?', json({}, 500))
    await view.ready

    expect(text('.data-preview-data')).toContain('Preview unavailable')
    expect($page.find('.data-preview a').length).toBe(0)
  })

  it('shows a CSV failure in the data pane, and retries when chosen again', async () => {
    await manifestThen(manifest())
    const second = () => $page.find('.data-preview-data [role=tab]').eq(1)
    second().trigger('click')
    answer('fish_biometry%20.csv', json({ error: 'x' }, 500))
    await vi.waitFor(() => expect(text('.data-preview-data')).toContain('Preview unavailable'))

    $page.find('.data-preview-data [role=tab]').eq(0).trigger('click')
    await vi.waitFor(() => expect($page.find('.data-preview-data table').length).toBe(1))
    second().trigger('click')

    expect(urls().filter(url => url.includes('fish_biometry%20.csv')).length).toBe(2)
  })

  it('shows Preview unavailable and the package link when the manifest fails', async () => {
    start()
    answer('/documents/abc/data-preview', json({ error: 'x' }, 500))
    await view.ready

    expect(text('.data-preview')).toContain('Preview unavailable')
    expect($page.find('.data-preview-data a').attr('href')).toBe(PACKAGE)
    expect(text('.data-preview')).not.toContain('Loading')
  })

  it('leaves no blank documents column when the manifest fails, widening the data pane', async () => {
    start()
    answer('/documents/abc/data-preview', json({ error: 'x' }, 500))
    await view.ready

    expect($page.find('.data-preview-documents').length).toBe(0)
    expect($page.find('.data-preview-data').hasClass('col-lg-12')).toBe(true)
    expect($page.find('.data-preview-data').hasClass('col-lg-7')).toBe(false)
  })

  describe('with no files', () => {
    const empty = { dataFiles: [], dataCount: 0, dataCountIsMinimum: false, totalFiles: 0, documents: [] }

    it('removes the section and its contents entry', async () => {
      start()
      answer('/documents/abc/data-preview', json(empty))
      await view.ready

      expect($('#segment-map').length).toBe(0)
      expect($('a[href="#segment-map"]').length).toBe(0)
    })

    // Supporting documents are shown beside the data, never on their own
    it('removes the section when there are supporting documents but no CSV files', async () => {
      start()
      answer('/documents/abc/data-preview', json({ ...empty, documents: [entry('dict.txt', 'Dictionary')] }))
      await view.ready

      expect($('#segment-map').length).toBe(0)
      expect($('a[href="#segment-map"]').length).toBe(0)
      expect(urls().some(url => url.includes('/document?'))).toBe(false)
    })

    it('removes only the preview, keeping the map, when there are documents but no CSV files', async () => {
      start(page({ map: true }))
      answer('/documents/abc/data-preview', json({ ...empty, documents: [entry('dict.txt', 'Dictionary')] }))
      await view.ready

      expect($('.data-preview').length).toBe(0)
      expect($('#segment-map .wms-map-preview').length).toBe(1)
    })

    it('removes only the preview when the section has a map', async () => {
      start(page({ map: true }))
      answer('/documents/abc/data-preview', json(empty))
      await view.ready

      expect($('.data-preview').length).toBe(0)
      expect($('#segment-map .wms-map-preview').length).toBe(1)
      expect($('a[href="#segment-map"]').length).toBe(1)
    })
  })

  describe('supporting documents', () => {
    const documents = [entry('dict.txt', 'Dictionary'), entry('t.xlsx', 'Table'), entry('big.pdf', 'Big'), entry('x.bin', 'Binary')]
    const withDocuments = async () => {
      await manifestThen(manifest({ documents }))
      await vi.waitFor(() => expect(urls().some(url => url.includes('/document?'))).toBe(true))
    }
    const documentTab = n => $page.find('.data-preview-documents [role=tab]').eq(n)

    it('makes a tab per document and fetches the first', async () => {
      await withDocuments()

      expect(tabs('.data-preview-documents')).toEqual(['Dictionary', 'Table', 'Big', 'Binary'])
      expect(urls()).toContain('/documents/abc/data-preview/document?path=dict.txt')
    })

    it('shows TEXT as escaped text, with tabs and newlines kept', async () => {
      await withDocuments()
      answer('/document?', json({ kind: 'TEXT', text: 'a\tb\n<img src=x onerror=alert(1)>', table: null, truncated: false }))
      await vi.waitFor(() => expect($page.find('.data-preview-documents pre').length).toBe(1))

      const $pre = $page.find('.data-preview-documents pre')
      expect($pre.text()).toBe('a\tb\n<img src=x onerror=alert(1)>')
      expect($pre.find('img').length).toBe(0)
      expect(text('.data-preview-documents')).not.toContain('Showing the start')
    })

    it('notes when TEXT is truncated', async () => {
      await withDocuments()
      answer('/document?', json({ kind: 'TEXT', text: 'abc', table: null, truncated: true }))
      await vi.waitFor(() => expect(text('.data-preview-documents')).toContain('Showing the start of this document'))
    })

    it('names the sheet for a workbook document', async () => {
      await withDocuments()
      documentTab(1).trigger('click')
      answer('path=t.xlsx', json({ kind: 'TABLE', text: null, table: { header: ['x'], rows: [['1']], moreRows: false, previewable: true, sheet: 'Units', sheets: ['Units', 'Codes'] }, truncated: false }))
      await vi.waitFor(() => expect($page.find('.data-preview-documents table').length).toBe(1))

      expect(text('.data-preview-documents .data-preview-sheets')).toBe('Sheet “Units” · also in this workbook: Codes')
    })

    it('shows TABLE with csvTable', async () => {
      await withDocuments()
      documentTab(1).trigger('click')
      answer('path=t.xlsx', json({ kind: 'TABLE', text: null, table: { header: ['x'], rows: [['1']], moreRows: false, previewable: true }, truncated: false }))
      await vi.waitFor(() => expect($page.find('.data-preview-documents table th').text()).toBe('x'))
    })

    it('shows the name and the supporting documents link for a document it cannot show', async () => {
      await withDocuments()
      documentTab(2).trigger('click')
      answer('path=big.pdf', json({ kind: 'TOO_LARGE', text: null, table: null, truncated: false }))
      await vi.waitFor(() => expect(text('.data-preview-documents')).toContain('In the supporting documents download'))

      expect(text('.data-preview-documents .data-preview-panel')).toContain('Big')
      expect($page.find('.data-preview-documents .data-preview-panel a').attr('href')).toBe(DOCUMENTS)
      documentTab(3).trigger('click')
      answer('path=x.bin', json({ kind: 'NOT_PREVIEWABLE', text: null, table: null, truncated: false }))
      await vi.waitFor(() => expect(text('.data-preview-documents .data-preview-panel')).toContain('Binary'))
    })

    it('shows the note without a link when there is no supporting documents URL', async () => {
      start(page({ documentsUrl: '' }))
      answer('/documents/abc/data-preview', json(manifest({ documents: [entry('x.bin', 'Binary')] })))
      await vi.waitFor(() => expect(urls().some(url => url.includes('/document?'))).toBe(true))
      answer('/data?', json(dataTable()))
      answer('/document?', json({ kind: 'NOT_PREVIEWABLE', text: null, table: null, truncated: false }))
      await view.ready

      expect(text('.data-preview-documents')).toContain('In the supporting documents download')
      expect($page.find('.data-preview-documents a').length).toBe(0)
    })

    it('says the preview is busy on a 503, in the documents pane only', async () => {
      await withDocuments()
      answer('/document?', json({ error: 'busy' }, 503))
      await vi.waitFor(() => expect(text('.data-preview-documents')).toContain('The preview is busy, try again shortly'))

      expect($page.find('.data-preview-documents a').attr('href')).toBe(DOCUMENTS)
      expect(text('.data-preview-data')).not.toContain('busy')
      expect($page.find('.data-preview-data table').length).toBe(1)
    })

    it('says the preview is unavailable on another error', async () => {
      await withDocuments()
      answer('/document?', json({ error: 'x' }, 500))
      await vi.waitFor(() => expect(text('.data-preview-documents')).toContain('Preview unavailable'))

      expect($page.find('.data-preview-documents a').attr('href')).toBe(DOCUMENTS)
    })

    it('leaves the documents pane out when there are none, widening the data pane', async () => {
      await manifestThen(manifest())

      expect($page.find('.data-preview-documents').length).toBe(0)
      expect($page.find('.data-preview-data').hasClass('col-lg-12')).toBe(true)
    })
  })
})
