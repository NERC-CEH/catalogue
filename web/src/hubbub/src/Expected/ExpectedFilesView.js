import _ from 'underscore'
import Backbone from 'backbone'
import { matchExpected } from './matchExpected'

const FILE_STATUS = { uploaded: 'uploaded', 'not-uploaded': 'not yet uploaded', unknown: '' }

// Depositors enter these, so every value is escaped; a missing one shows as blank
const text = value => _.escape(value ?? '')

const plural = (count, word) => `${count} ${word}${count === 1 ? '' : 's'}`

/**
 * The agreement's last agreed file listing, ticked off against the uploads (dri-one #536). Warnings
 * only, and worded neutrally: staff resolve mismatches.
 *
 * `sources` returns `{ uploads, complete, hasListings }`: the uploaded data files as
 * `[{ path, status }]`, whether every listing that could hold a file is fully loaded, and whether
 * the page has any listing. Without one nothing is said about arrivals.
 */
export default Backbone.View.extend({

  initialize ({ expected, sources }) {
    this.expected = expected
    this.sources = sources
  },

  render () {
    const { uploads, complete, hasListings } = this.sources()
    const result = matchExpected(this.expected, hasListings ? uploads : [], { complete: hasListings && complete })
    this.$el.html([
      this.expected.revising
        ? '<p class="expected-revising alert alert-info">The service agreement is being revised; this list is the last agreed version.</p>'
        : '',
      hasListings && !complete
        ? '<p class="expected-partial text-body-secondary">The counts cover the files loaded so far.</p>'
        : '',
      this.table(result, hasListings),
      hasListings ? this.unmatched(result.unmatched) : '',
      hasListings && result.total.differs
        ? `<p class="expected-total alert alert-warning">The agreement says ${result.total.expected} files will be deposited; ${plural(result.total.found, 'uploaded file')} ${result.total.found === 1 ? 'matches' : 'match'} it.</p>`
        : ''
    ].join(''))
    return this
  },

  table (result, withStatus) {
    if (result.files.length === 0 && result.groups.length === 0) {
      return '<p class="expected-none">The service agreement lists no files.</p>'
    }
    const status = content => withStatus ? `<td class="expected-status">${content}</td>` : ''
    const files = result.files.map(file => `
      <tr class="expected-file">
        <td class="expected-path">${text(file.name)}</td>
        <td></td>
        <td>${text(file.format)}</td>
        <td>${text(file.size)}</td>
        ${status(FILE_STATUS[file.status])}
      </tr>`)
    const groups = result.groups.map(group => {
      const count = /^\d+$/.test(String(group.count ?? '').trim()) ? ` of ${text(String(group.count).trim())}` : ''
      const flag = group.flag
        ? ' <i class="fa-solid fa-triangle-exclamation" aria-hidden="true"></i><span class="visually-hidden">(differs from the agreement)</span>'
        : ''
      return `
      <tr class="expected-group${group.flag ? ' expected-flagged table-warning' : ''}">
        <td class="expected-path"><code>${text(group.pattern)}</code></td>
        <td>${text(group.description)}</td>
        <td>${text(group.format)}</td>
        <td>${text(group.size)}</td>
        ${status(`${group.found}${count} uploaded${flag}`)}
      </tr>`
    })
    return `
      <table class="table expected-files-table">
        <thead><tr>
          <th scope="col">File or pattern</th><th scope="col">Description</th>
          <th scope="col">Format</th><th scope="col">Size</th>
          ${withStatus ? '<th scope="col">Status</th>' : ''}
        </tr></thead>
        <tbody>${files.join('')}${groups.join('')}</tbody>
      </table>`
  },

  // Folded away: a deposit described loosely could otherwise fill the page with them
  unmatched (paths) {
    if (paths.length === 0) return ''
    const items = paths.map(path =>
      `<li><span class="expected-path">${text(path)}</span> <span class="expected-status">not in the agreement</span></li>`)
    return `
      <details class="expected-unmatched">
        <summary>${plural(paths.length, 'file')} not in the agreement</summary>
        <ul>${items.join('')}</ul>
      </details>`
  }
})
