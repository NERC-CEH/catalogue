import $ from 'jquery'
import Backbone from 'backbone'
import { filesize } from 'filesize'
import { render as renderTable } from './csvTable'

const SECTION = '#segment-map'

const plural = (count, word) => `${count} ${word}${count === 1 ? '' : 's'}`

// "384 KB", "1.2 KB": binary multiples with the familiar unit names
const size = bytes => filesize(bytes, { standard: 'jedec' })

/**
 * The record page's Data Preview section: the first rows of the record's CSV files, one tab per file,
 * beside its supporting documents, one tab per document. Everything from the server is set as text.
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
    const csvFiles = this.manifest.csvFiles || []
    const documents = this.manifest.documents || []
    if (!csvFiles.length && !documents.length) return this.removeSection()
    this.$data.empty()
    this.$documents.empty()
    if (csvFiles.length) {
      this.csvPane = this.tabbedPane(this.$data, csvFiles, (entry) => this.csvContent(entry))
      this.$data.append(this.csvFooter(csvFiles))
    } else {
      this.$data.remove()
    }
    if (documents.length) {
      this.documentPane = this.tabbedPane(this.$documents, documents, (entry) => this.documentContent(entry))
    } else {
      this.$documents.remove()
    }
    if (!csvFiles.length) this.$documents.removeClass('col-lg-5').addClass('col-lg-12')
    if (!documents.length) this.$data.removeClass('col-lg-7').addClass('col-lg-12')
    await Promise.all([this.csvPane?.select(0), this.documentPane?.select(0)])
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
  async csvContent (entry) {
    let sample
    try {
      sample = await this.fetchJson(this.endpoint('csv', entry.path))
    } catch (status) {
      throw Object.assign(new Error(`CSV ${status}`), { content: this.unavailable() })
    }
    if (!sample.previewable) {
      return $('<p class="text-muted"></p>').text('This file can\'t be previewed. ').append(this.packageLink())
    }
    const rows = sample.rows || []
    const columns = Math.max((sample.header || []).length, ...rows.map(row => row.length))
    const of = entry.bytes > 0 ? ` of a ${size(entry.bytes)} file` : ''
    const note = `${sample.moreRows ? 'First' : 'All'} ${plural(rows.length, 'row')}${of} · ${plural(columns, 'column')}`
    const $note = $('<p class="data-preview-note small text-muted mb-1"></p>').text(note)
    if (this.packageUrl) $note.append(document.createTextNode(' · '), $('<a></a>').attr('href', this.packageUrl).text('Download the data'))
    return $('<div></div>').append(
      $note,
      $('<div class="data-preview-scroll"></div>').append(renderTable(sample)))
  },

  csvFooter (csvFiles) {
    const { csvCount, csvCountIsMinimum, totalFiles } = this.manifest
    if (!(csvCount > csvFiles.length || csvCountIsMinimum)) return $()
    const count = `${csvCountIsMinimum ? 'at least ' : ''}${csvCount} CSV files`
    const text = `Showing ${csvFiles.length} of ${count} · ${totalFiles} files in this dataset, all in the full download`
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
    if (result.kind === 'TEXT') {
      const $content = $('<div></div>').append($('<pre class="data-preview-text"></pre>').text(result.text || ''))
      if (result.truncated) $content.append($('<p class="small text-muted mb-0"></p>').text('Showing the start of this document'))
      return $content
    }
    if (result.kind === 'TABLE' && result.table) {
      return $('<div class="data-preview-scroll"></div>').append(renderTable(result.table))
    }
    const $note = $('<p class="text-muted"></p>').append($('<strong></strong>').text(entry.label), $('<br>'))
    return $note.append(this.documentsUrl ? this.documentsLink() : document.createTextNode('In the supporting documents download'))
  }
})
