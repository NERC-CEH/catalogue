import { describe, expect, it } from 'vitest'
import $ from 'jquery'
import { cellClass, render } from '../src/csvTable'

const texts = (table, selector) => $(table).find(selector).map((i, el) => $(el).text()).get()

describe('csvTable', () => {
  describe('cellClass', () => {
    it('is empty for a name that merely starts like a marker', () => expect(cellClass('NANH1')).toBe(''))
    it('marks conventional missing values', () => {
      ;['NA', 'NaN', 'NULL', 'null', '-999', '-9999'].forEach(each => expect(cellClass(each), each).toBe('cell-missing'))
    })
    it('marks blank cells', () => {
      expect(cellClass(' ')).toBe('cell-empty')
      expect(cellClass('')).toBe('cell-empty')
    })
  })

  describe('render', () => {
    it('renders the header and cells', () => {
      const table = render({ header: ['a', 'b'], rows: [['1', 'NA'], ['', '3']] })

      expect(table.tagName).toBe('TABLE')
      expect(texts(table, 'th')).toEqual(['a', 'b'])
      expect(texts(table, 'td')).toEqual(['1', 'NA', '', '3'])
      expect($(table).find('td.cell-missing').length).toBe(1)
      expect($(table).find('td.cell-empty').length).toBe(1)
    })

    it('marks a blank cell as empty and leaves a real value unmarked', () => {
      const cells = $(render({ header: ['a', 'b', 'c'], rows: [[' ', 'NA', 'x']] })).find('td')

      expect(cells.eq(0).hasClass('cell-empty')).toBe(true)
      expect(cells.eq(0).text()).toBe(' ')
      expect(cells.eq(1).hasClass('cell-missing')).toBe(true)
      expect(cells.eq(1).hasClass('cell-empty')).toBe(false)
      expect(cells.eq(2).hasClass('cell-empty') || cells.eq(2).hasClass('cell-missing')).toBe(false)
    })

    it('renders markup as literal text', () => {
      const table = render({ header: ['<b>h</b>'], rows: [['<img src=x onerror=alert(1)>']] })

      expect($(table).find('img, b').length).toBe(0)
      expect(texts(table, 'td')).toEqual(['<img src=x onerror=alert(1)>'])
      expect(texts(table, 'th')).toEqual(['<b>h</b>'])
    })

    it('rendersRowsWiderThanHeader', () => {
      const table = render({ header: ['a'], rows: [['1', '2', '3'], ['4']] })

      expect(texts(table, 'th')).toEqual(['a', '', ''])
      expect(texts(table, 'td')).toEqual(['1', '2', '3', '4'])
    })

    it('renders rows shorter than the header', () => {
      const table = render({ header: ['a', 'b', 'c'], rows: [['1']] })

      expect(texts(table, 'th')).toEqual(['a', 'b', 'c'])
      expect(texts(table, 'td')).toEqual(['1'])
    })

    it('renders a sample with no rows', () => {
      expect(texts(render({ header: ['a'], rows: [] }), 'th')).toEqual(['a'])
      expect(texts(render({}), 'th')).toEqual([])
    })
  })
})
