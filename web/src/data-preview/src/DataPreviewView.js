import $ from 'jquery'
import Backbone from 'backbone'
import { filesize } from 'filesize'
import { render as renderTable } from './csvTable'

const SECTION = '#segment-map'

const plural = (count, word) => `${count} ${word}${count === 1 ? '' : 's'}`

// "384 KB", "1.2 KB": binary multiples with the familiar unit names
const size = bytes => filesize(bytes, { standard: 'jedec' })

// A CF time as the server gives it (ISO 8601), without a midnight time of day: "2000-01-01"
const when = iso => String(iso).replace(/T00:00:00(\.0+)?Z$/, '')

// A coordinate value to at most four decimal places: 50.5, -3, 51.1429
const number = value => String(Number(Number(value).toFixed(4)))

const capitalise = text => text.charAt(0).toUpperCase() + text.slice(1)

/**
 * The record page's Data Preview section: the first rows of the record's data files (delimited text
 * and Excel workbooks, or the start of a plain-text file that isn't a table), or the header summary of
 * its first NetCDF file, one tab per file, beside
 * its supporting documents, one tab per document. The documents are shown only beside data: a record
 * with no data files to show has no preview. Everything from the server is set as text.
 * A pane that fails shows why in itself and leaves the other alone.
 */
export default Backbone.View.extend({
  initialize () {
    this.recordId = this.$el.attr('data-record-id')
    this.packageUrl = this.$el.attr('data-package-url') || ''
    this.documentsUrl = this.$el.attr('data-supporting-documents-url') || ''
    this.$data = this.$('.data-preview-data')
    this.$documents = this.$('.data-preview-documents')
    this.$data.text('Loading preview…')
    this.$documents.text('Loading preview…')
    this.ready = this.loadManifest()
  },

  endpoint (suffix, path) {
    const base = `/documents/${encodeURIComponent(this.recordId)}/data-preview`
    return path === undefined ? base : `${base}/${suffix}?path=${encodeURIComponent(path)}`
  },

  // The parsed JSON, or a rejection with the HTTP status
  fetchJson (url) {
    return new Promise((resolve, reject) => {
      $.ajax({ url, dataType: 'json' }).then(resolve, xhr => reject(xhr.status))
    })
  },

  async loadManifest () {
    try {
      this.manifest = await this.fetchJson(this.endpoint())
    } catch {
      this.$data.empty().append(this.unavailable()).removeClass('col-lg-7').addClass('col-lg-12')
      this.$documents.remove()
      return
    }
    const dataFiles = this.manifest.dataFiles || []
    const documents = this.manifest.documents || []
    if (!dataFiles.length) return this.removeSection()
    this.$data.empty()
    this.$documents.empty()
    this.dataPane = this.tabbedPane(this.$data, dataFiles, (entry) => this.dataContent(entry))
    this.$data.append(this.dataFooter(dataFiles))
    if (documents.length) {
      this.documentPane = this.tabbedPane(this.$documents, documents, (entry) => this.documentContent(entry))
    } else {
      this.$documents.remove()
    }
    if (!documents.length) this.$data.removeClass('col-lg-7').addClass('col-lg-12')
    await Promise.all([this.dataPane.select(0), this.documentPane?.select(0)])
  },

  removeSection () {
    const $section = this.$el.closest(SECTION)
    if ($section.find('.wms-map-preview').length) {
      this.$el.remove()
    } else {
      $section.remove()
      $(`a[href="${SECTION}"]`).remove()
    }
  },

  // Tabs over `entries`, and a panel showing the selected one, as built by `build(entry)` (a promise of
  // content, or a rejection carrying `content`). Each entry is built once; a failure shows its content
  // but isn't kept, so choosing the entry again retries.
  tabbedPane ($container, entries, build) {
    const $tabs = $('<div class="nav nav-tabs flex-nowrap overflow-auto" role="tablist"></div>')
    const $panel = $('<div class="data-preview-panel pt-2" role="tabpanel"></div>')
    const cache = new Map()
    let current = -1
    const select = async index => {
      current = index
      $tabs.children().each((i, tab) => {
        $(tab).toggleClass('active', i === index).attr('aria-selected', String(i === index))
      })
      if (!cache.has(index)) {
        $panel.empty().text('Loading preview…')
        cache.set(index, build(entries[index]).catch(failure => { cache.delete(index); return failure.content }))
      }
      const content = await cache.get(index)
      if (current === index) $panel.empty().append(content)
    }
    entries.forEach((entry, i) => {
      $('<button type="button" class="nav-link text-nowrap" role="tab" aria-selected="false"></button>')
        .text(entry.label).on('click', () => select(i)).appendTo($tabs)
    })
    $container.append($tabs, $panel)
    return { select }
  },

  packageLink () {
    return this.packageUrl ? $('<a></a>').attr('href', this.packageUrl).text('Download the full dataset') : $()
  },

  documentsLink () {
    return this.documentsUrl
      ? $('<a></a>').attr('href', this.documentsUrl).text('In the supporting documents download')
      : $()
  },

  unavailable () {
    return $('<p class="text-muted"></p>').text('Preview unavailable. ').append(this.packageLink())
  },

  // Resolves to content for the panel, or rejects with an error whose `content` says what to show
  async dataContent (entry) {
    let result
    try {
      result = await this.fetchJson(this.endpoint('data', entry.path))
    } catch (status) {
      throw Object.assign(new Error(`Data ${status}`), {
        content: status === 503
          ? $('<p class="text-muted"></p>').text('The preview is busy, try again shortly. ').append(this.packageLink())
          : this.unavailable()
      })
    }
    if (result.kind === 'TABLE' && result.table?.previewable) return this.dataTable(entry, result.table)
    if (result.kind === 'TEXT') return this.text(result, 'file')
    if (result.kind === 'SUMMARY' && result.summary) return this.netcdfSummary(entry, result.summary)
    return $('<p class="text-muted"></p>').text('This file can\'t be previewed. ').append(this.packageLink())
  },

  dataTable (entry, sample) {
    const rows = sample.rows || []
    const columns = Math.max((sample.header || []).length, ...rows.map(row => row.length))
    const of = entry.bytes > 0 ? ` of a ${size(entry.bytes)} file` : ''
    const note = `${sample.moreRows ? 'First' : 'All'} ${plural(rows.length, 'row')}${of} · ${plural(columns, 'column')}`
    const $note = $('<p class="data-preview-note small text-muted mb-1"></p>').text(note)
    if (this.packageUrl) $note.append(document.createTextNode(' · '), $('<a></a>').attr('href', this.packageUrl).text('Download the data'))
    return $('<div></div>').append(
      $note,
      this.sheetNote(sample),
      $('<div class="data-preview-scroll"></div>').append(renderTable(sample)))
  },

  // A NetCDF file's header: what it holds, over what time and area, in which variables
  netcdfSummary (entry, summary) {
    const count = this.manifest.netcdfCount || 1
    const parts = [count > 1 ? `Summary of the first of ${count} NetCDF files` : 'Summary of the NetCDF file', summary.format]
    if (entry.bytes > 0) parts.push(`${size(entry.bytes)} file`)
    const $note = $('<p class="data-preview-note small text-muted mb-1"></p>').text(parts.join(' · '))
    if (this.packageUrl) $note.append(document.createTextNode(' · '), $('<a></a>').attr('href', this.packageUrl).text('Download the data'))

    const attributes = summary.attributes || []
    const title = attributes.find(a => a.name === 'title')
    const $content = $('<div class="data-preview-netcdf"></div>').append($note)
    if (title) $content.append($('<p class="fw-semibold mb-1"></p>').text(title.value))

    const $facts = $('<ul class="list-unstyled small mb-2"></ul>')
    if (summary.time) {
      const calendar = summary.time.calendar && summary.time.calendar !== 'standard' ? ` (${summary.time.calendar} calendar)` : ''
      $facts.append($('<li></li>').text(`Time: ${when(summary.time.start)} to ${when(summary.time.end)}${calendar}`))
    }
    ;(summary.extents || []).forEach(e => {
      const units = e.units ? ` ${e.units}` : ''
      $facts.append($('<li></li>').text(`${capitalise(e.axis)}: ${number(e.min)} to ${number(e.max)}${units}`))
    })
    const dimensions = (summary.dimensions || []).map(d => `${d.name} ${d.length}${d.unlimited ? ' (unlimited)' : ''}`)
    if (dimensions.length) $facts.append($('<li></li>').text(`Dimensions: ${dimensions.join(' · ')}`))
    $content.append($facts)

    // Data variables first: they are what the file is for
    const variables = [...(summary.variables || [])].sort((a, b) => Number(a.coordinate) - Number(b.coordinate))
    const $head = $('<tr></tr>')
    ;['Variable', 'Description', 'Units', 'Dimensions', 'Type'].forEach(h => $head.append($('<th scope="col"></th>').text(h)))
    const $body = $('<tbody></tbody>')
    variables.forEach(v => {
      const $name = $('<td></td>').text(v.name)
      if (v.coordinate) $name.append($('<span class="text-muted"></span>').text(' (coordinate)'))
      $body.append($('<tr></tr>').append(
        $name,
        $('<td></td>').text(v.longName || v.standardName || ''),
        $('<td></td>').text(v.units || ''),
        $('<td></td>').text(v.dimensions || ''),
        $('<td></td>').text(v.type || '')))
    })
    $content.append($('<div class="data-preview-scroll"></div>').append(
      $('<table class="table table-sm table-striped data-preview-table data-preview-variables"></table>')
        .append($('<thead></thead>').append($head), $body)))
    if (summary.variablesTruncated) {
      $content.append($('<p class="small text-muted mb-1"></p>').text(`Showing the first ${variables.length} variables`))
    }

    if (attributes.length) {
      const $list = $('<dl class="small mb-0"></dl>')
      attributes.forEach(a => $list.append($('<dt></dt>').text(a.name), $('<dd></dd>').text(a.value)))
      const more = summary.attributesTruncated ? ', first shown' : ''
      $content.append($('<details class="data-preview-attributes mt-2"></details>').append(
        $('<summary class="small"></summary>').text(`Global attributes (${attributes.length}${more})`), $list))
    }
    return $content
  },

  // For a workbook of several sheets, which one is shown and what the others are called
  sheetNote (sample) {
    const sheets = sample.sheets || []
    if (!sample.sheet || sheets.length < 2) return $()
    const others = sheets.filter(name => name !== sample.sheet)
    return $('<p class="data-preview-sheets small text-muted mb-1"></p>')
      .text(`Sheet “${sample.sheet}” · also in this workbook: ${others.join(', ')}`)
  },

  text (result, what) {
    const $content = $('<div></div>').append($('<pre class="data-preview-text"></pre>').text(result.text || ''))
    if (result.truncated) $content.append($('<p class="small text-muted mb-0"></p>').text(`Showing the start of this ${what}`))
    return $content
  },

  dataFooter (dataFiles) {
    const { dataCount, dataCountIsMinimum, totalFiles } = this.manifest
    if (!(dataCount > dataFiles.length || dataCountIsMinimum)) return $()
    const count = `${dataCountIsMinimum ? 'at least ' : ''}${dataCount} previewable files`
    const text = `Showing ${dataFiles.length} of ${count} · ${totalFiles} files in this dataset, all in the full download`
    const $note = $('<p class="data-preview-footer small text-muted mt-2 mb-0"></p>')
    return this.packageUrl ? $note.append($('<a></a>').attr('href', this.packageUrl).text(text)) : $note.text(text)
  },

  async documentContent (entry) {
    let result
    try {
      result = await this.fetchJson(this.endpoint('document', entry.path))
    } catch (status) {
      throw Object.assign(new Error(`Document ${status}`), {
        content: $('<p class="text-muted"></p>').text(status === 503
          ? 'The preview is busy, try again shortly. '
          : 'Preview unavailable. ').append(this.documentsLink())
      })
    }
    if (result.kind === 'TEXT') return this.text(result, 'document')
    if (result.kind === 'TABLE' && result.table) {
      return $('<div></div>').append(
        this.sheetNote(result.table),
        $('<div class="data-preview-scroll"></div>').append(renderTable(result.table)))
    }
    const $note = $('<p class="text-muted"></p>').append($('<strong></strong>').text(entry.label), $('<br>'))
    return $note.append(this.documentsUrl ? this.documentsLink() : document.createTextNode('In the supporting documents download'))
  }
})
