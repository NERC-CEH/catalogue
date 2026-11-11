import { describe, expect, it } from 'vitest'
import { parseSearchQuery } from '../src/searchQuery'

// A search's state, read back from the query string the page, a link or the router wrote
describe('Reading a search from its query string', () => {
  it('reads a term, as a GET form writes it with + for spaces (dri-one #532)', () => {
    expect(parseSearchQuery('term=soil+carbon')).toEqual({ term: 'soil carbon' })
  })

  it('reads a term the router encoded, + and all', () => {
    expect(parseSearchQuery(`term=${encodeURIComponent('C++ & R')}`)).toEqual({ term: 'C++ & R' })
  })

  it('keeps a term that looks like a number as text (dri-one #532)', () => {
    expect(parseSearchQuery('term=007')).toEqual({ term: '007' })
    expect(parseSearchQuery('term=true')).toEqual({ term: 'true' })
  })

  it('reads the page and rows as numbers', () => {
    expect(parseSearchQuery('page=2&rows=50')).toEqual({ page: 2, rows: 50 })
  })

  it('leaves a page that is not a number as it is, rather than NaN', () => {
    expect(parseSearchQuery('page=last')).toEqual({ page: 'last' })
  })

  it('reads semantic as a boolean', () => {
    expect(parseSearchQuery('semantic=true')).toEqual({ semantic: true })
    expect(parseSearchQuery('semantic=false')).toEqual({ semantic: false })
  })

  it('reads one facet as a value and repeated facets as a list', () => {
    expect(parseSearchQuery('facet=resourceType%7Cdataset')).toEqual({ facet: 'resourceType|dataset' })
    expect(parseSearchQuery('facet=a%7Cb&facet=c%7Cd')).toEqual({ facet: ['a|b', 'c|d'] })
  })

  it('reads a bounding box and spatial operation', () => {
    expect(parseSearchQuery('bbox=POLYGON((1%202))&op=within')).toEqual({ bbox: 'POLYGON((1 2))', op: 'within' })
  })

  it('takes a query string with or without its ?', () => {
    expect(parseSearchQuery('?term=soil')).toEqual({ term: 'soil' })
  })

  it('reads a __proto__ key as a key, not as the object\'s prototype', () => {
    const state = parseSearchQuery('__proto__=a&__proto__=b')
    expect(Object.getPrototypeOf(state)).toBe(Object.prototype)
    expect(Object.hasOwn(state, '__proto__')).toBe(true)
  })

  it('reads an empty or missing query string as no state', () => {
    expect(parseSearchQuery('')).toEqual({})
    expect(parseSearchQuery(undefined)).toEqual({})
  })
})
