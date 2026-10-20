import $ from 'jquery'

const MISSING_VALUES = ['NA', 'NaN', 'NULL', 'null', '-999', '-9999']

/**
 * The style class for a cell's value: 'cell-empty' for blank, 'cell-missing' for a conventional
 * missing-value marker (matched exactly, so NANH1 isn't one), else ''.
 */
export const cellClass = value => {
  if (String(value).trim() === '') return 'cell-empty'
  return MISSING_VALUES.includes(value) ? 'cell-missing' : ''
}

/**
 * A <table> for a CSV sample ({ header, rows }). Every cell is set as text, never as HTML. A row can
 * be longer than the header, so the header gets blank cells to keep the extra columns lined up.
 */
export const render = sample => {
  const header = sample.header || []
  const rows = sample.rows || []
  const width = Math.max(header.length, ...rows.map(row => row.length))
  const $headRow = $('<tr></tr>')
  for (let i = 0; i < width; i++) $headRow.append($('<th scope="col"></th>').text(i < header.length ? header[i] : ''))
  const $body = $('<tbody></tbody>')
  rows.forEach(row => {
    const $row = $('<tr></tr>')
    row.forEach(value => $row.append($('<td></td>').addClass(cellClass(value)).text(value)))
    $body.append($row)
  })
  return $('<table class="table table-sm table-striped data-preview-table"></table>')
    .append($('<thead></thead>').append($headRow), $body)[0]
}

export default { render, cellClass }
