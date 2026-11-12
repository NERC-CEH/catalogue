import _ from 'underscore'
import Backbone from 'backbone'
import { matchExpected } from './matchExpected'
import { formatBytes } from '../../../file-size/formatBytes'

const FILE_STATUS = { uploaded: 'uploaded', 'not-uploaded': 'not yet uploaded', unknown: '' }

// Depositors enter these, so every value is escaped; a missing one shows as blank
const text = value => _.escape(value ?? '')

const plural = (count, word) => `${count} ${word}${count === 1 ? '' : 's'}`

const hasBytes = value => typeof value === 'number' && Number.isFinite(value)

// From bytes when the agreement has them, else the size as typed (an older agreement)
const fileSize = file => hasBytes(file.bytes) ? formatBytes(file.bytes) : text(file.size)

const groupSize = group => {
  const min = hasBytes(group.minBytes) ? group.minBytes : group.maxBytes
  const max = hasBytes(group.maxBytes) ? group.maxBytes : group.minBytes
  const each = hasBytes(min)
    ? `<span class="expected-each">${min === max ? formatBytes(min) : `${formatBytes(min)} – ${formatBytes(max)}`}</span>`
    : text(group.size)
  const total = hasBytes(group.totalBytes)
    ? `<div class="expected-total-size text-body-secondary">${formatBytes(group.totalBytes)} in total</div>`
    : ''
  return each + total
}

// Neutral wording (dri-one #537): staff decide whether a difference matters
const sizeFlags = flags => flags.filter(Boolean).map(flag => `; <span class="expected-size-flag">${flag}</span>`).join('')

/**
 * The agreement's last agreed file listing, ticked off against the uploads (dri-one #536). Warnings
 * only, and worded neutrally: staff resolve mismatches.
 *
 * `sources` returns `{ uploads, complete, hasListings }`: the uploaded data files as
 * `[{ path, status, bytes? }]`, whether every listing that could hold a file is fully loaded, and whether
 * the page has any listing. Without one nothing is said about arrivals.
 *
 * An agreement with a prose naming convention (`hasNamingConvention`) may describe files it doesn't
 * list, so then no upload is called "not in the agreement" and the total isn't compared.
 */
export default Backbone.View.extend({

  initialize ({ expected, sources }) {
    this.expected = expected
    this.sources = sources
  },

  render () {
    const { uploads, complete, hasListings } = this.sources()
    const result = matchExpected(this.expected, hasListings ? uploads : [], { complete: hasListings && complete })
    const convention = Boolean(this.expected.hasNamingConvention)
    this.$el.html([
      this.expected.revising
        ? '<p class="expected-revising alert alert-info">The service agreement is being revised; this list is the last agreed version.</p>'
        : '',
      hasListings && !complete
        ? '<p class="expected-partial text-body-secondary">The counts cover the files loaded so far.</p>'
        : '',
      '<p class="expected-naming small text-body-secondary">Files uploaded through this page are stored with names in lower case and hyphens in place of spaces, and names are compared with the agreement ignoring case and treating spaces as hyphens. A zip file\'s contents are unpacked into a folder named after it: <code>data.zip</code> containing <code>site1/x.csv</code> gives <code>data/site1/x.csv</code>.</p>',
      convention
        ? '<p class="expected-convention text-body-secondary">Some files are described by the agreement\'s naming convention, so files not listed here may still be expected.</p>'
        : '',
      this.table(result, hasListings),
      hasListings && !convention ? this.unmatched(result.unmatched) : '',
      hasListings && !convention && result.total.differs
        ? `<p class="expected-total alert alert-warning">The agreement says ${result.total.expected} files will be deposited; ${result.total.found} ${result.total.found === 1 ? 'has' : 'have'} been uploaded.</p>`
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
      <tr class="expected-file${file.sizeDiffers ? ' expected-flagged table-warning' : ''}">
        <td class="expected-path">${text(file.name)}</td>
        <td></td>
        <td>${text(file.format)}</td>
        <td>${fileSize(file)}</td>
        ${status(FILE_STATUS[file.status] + sizeFlags([file.sizeDiffers && 'size differs from the agreement']))}
      </tr>`)
    const groups = result.groups.map(group => {
      const count = /^\d+$/.test(String(group.count ?? '').trim()) ? ` of ${text(String(group.count).trim())}` : ''
      const flag = group.flag
        ? ' <i class="fa-solid fa-triangle-exclamation" aria-hidden="true"></i><span class="visually-hidden">(differs from the agreement)</span>'
        : ''
      const sizes = sizeFlags([
        group.sizesDiffer && 'some files\' sizes differ from the agreement',
        group.totalDiffers && 'total size differs from the agreement'
      ])
      const flagged = group.flag || group.sizesDiffer || group.totalDiffers
      return `
      <tr class="expected-group${flagged ? ' expected-flagged table-warning' : ''}">
        <td class="expected-path"><code>${text(group.pattern)}</code></td>
        <td>${text(group.description)}</td>
        <td>${text(group.format)}</td>
        <td>${groupSize(group)}</td>
        ${status(`${group.found}${count} uploaded${flag}${sizes}`)}
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
