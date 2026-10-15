import { describe, expect, it } from 'vitest'
import template from '../src/templates/ObservedProperty'

describe('ObservedProperty template', () => {
  // Browsers keep only the first of a repeated attribute, so the rendered DOM hides these. Check the
  // markup itself: a second name="…" moved first would put every row's radios in one group.
  it('gives each input one name and one type', () => {
    const html = template({ data: { index: 'c1', constraints: {} } })

    const repeated = (html.match(/<input\b[^>]*>/g) || [])
      .filter(tag => (tag.match(/\sname=/g) || []).length > 1 || (tag.match(/\stype=/g) || []).length > 1)
    expect(repeated).toEqual([])
  })
})
