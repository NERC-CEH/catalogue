import { afterEach, beforeEach, describe, expect, it } from 'vitest'
import $ from 'jquery'
import { ExpectedFilesView } from '../../src/Expected'

describe('ExpectedFilesView', function () {
  const expected = {
    files: [
      { name: 'site1/2024.csv', format: 'CSV', size: '2 MB' },
      { name: 'readme.txt', format: 'Text', size: '1 KB' }
    ],
    fileGroups: [
      { pattern: 'site*/cp_*.csv', count: 2, format: 'CSV', size: '10 MB', description: 'One per site' }
    ],
    fileNumber: '4',
    revising: false
  }

  let view

  function render (agreement, sources) {
    view = new ExpectedFilesView({ el: '.expected-files', expected: agreement, sources: () => sources })
    view.render()
    return view
  }

  function row (text) {
    return view.$('tbody tr').filter((index, element) => $(element).find('td').first().text().trim() === text)
  }

  beforeEach(function () {
    $(document.body).html('<section class="expected-files"></section>')
  })

  afterEach(function () {
    view?.remove()
    $(document.body).empty()
  })

  it('shows each file and group with how much has arrived', function () {
    render(expected, {
      uploads: [
        { path: 'site1/2024.csv', status: 'VALID' },
        { path: 'site1/cp_a.csv', status: 'VALID' }
      ],
      complete: true,
      hasListings: true
    })

    const file = row('site1/2024.csv')
    expect(file.find('td').eq(2).text()).toBe('CSV')
    expect(file.find('td').eq(3).text()).toBe('2 MB')
    expect(file.find('.expected-status').text()).toBe('uploaded')
    expect(row('readme.txt').find('.expected-status').text()).toBe('not yet uploaded')

    const group = row('site*/cp_*.csv')
    expect(group.find('td').eq(1).text()).toBe('One per site')
    expect(group.find('.expected-status').text()).toContain('1 of 2 uploaded')
    expect(group.hasClass('expected-flagged')).toBe(true)
    expect(view.$('.expected-revising').length).toBe(0)
  })

  it('does not flag a group that has what was agreed', function () {
    render(expected, {
      uploads: [{ path: 'site1/cp_a.csv', status: 'VALID' }, { path: 'site2/cp_b.csv', status: 'VALID' }],
      complete: true,
      hasListings: true
    })

    const group = row('site*/cp_*.csv')
    expect(group.find('.expected-status').text()).toBe('2 of 2 uploaded')
    expect(group.hasClass('expected-flagged')).toBe(false)
  })

  it('says the list is the last agreed version while the agreement is revised', function () {
    render({ ...expected, revising: true }, { uploads: [], complete: true, hasListings: true })

    expect(view.$('.expected-revising').text().trim())
      .toBe('The service agreement is being revised; this list is the last agreed version.')
  })

  it('shows no statuses and no warnings without a listing', function () {
    render(expected, { uploads: [], complete: false, hasListings: false })

    expect(view.$('tbody tr').length).toBe(3)
    expect(view.$('.expected-status').length).toBe(0)
    expect(view.$('th:contains("Status")').length).toBe(0)
    expect(view.$('.expected-flagged, .expected-unmatched, .expected-total, .expected-partial').length).toBe(0)
  })

  it('makes no claim that a file is missing or a group short while a listing is partial', function () {
    render(expected, {
      uploads: [{ path: 'site1/cp_a.csv', status: 'VALID' }],
      complete: false,
      hasListings: true
    })

    expect(view.$el.text()).not.toContain('not yet uploaded')
    expect(row('readme.txt').find('.expected-status').text()).toBe('')
    expect(row('site*/cp_*.csv').hasClass('expected-flagged')).toBe(false)
    expect(view.$('.expected-total').length).toBe(0)
    expect(view.$('.expected-partial').text()).toContain('files loaded so far')
  })

  it('lists uploads the agreement does not mention', function () {
    render(expected, {
      uploads: [{ path: 'extra/notes.csv', status: 'VALID' }, { path: 'other.csv', status: 'VALID' }],
      complete: true,
      hasListings: true
    })

    const items = view.$('.expected-unmatched li')
    expect(items.length).toBe(2)
    expect(items.first().text()).toContain('extra/notes.csv')
    expect(items.first().find('.expected-status').text()).toBe('not in the agreement')
    expect(view.$('.expected-unmatched summary').text()).toContain('2 files not in the agreement')
  })

  it('says when the total differs from the agreement, and only then', function () {
    render(expected, { uploads: [{ path: 'site1/2024.csv', status: 'VALID' }], complete: true, hasListings: true })
    expect(view.$('.expected-total').text())
      .toBe('The agreement says 4 files will be deposited; 1 has been uploaded.')

    render(expected, {
      uploads: ['site1/2024.csv', 'other.csv'].map(path => ({ path, status: 'VALID' })),
      complete: true,
      hasListings: true
    })
    expect(view.$('.expected-total').text())
      .toBe('The agreement says 4 files will be deposited; 2 have been uploaded.')

    // Uploads the agreement doesn't mention still count towards the total
    render(expected, {
      uploads: ['site1/2024.csv', 'readme.txt', 'extra/a.csv', 'extra/b.csv'].map(path => ({ path, status: 'VALID' })),
      complete: true,
      hasListings: true
    })
    expect(view.$('.expected-total').length).toBe(0)

    render(expected, {
      uploads: ['site1/2024.csv', 'readme.txt', 'site1/cp_a.csv', 'site2/cp_b.csv'].map(path => ({ path, status: 'VALID' })),
      complete: true,
      hasListings: true
    })
    expect(view.$('.expected-total').length).toBe(0)
  })

  it('copes with an agreement whose entries leave fields out', function () {
    render({ files: [{}], fileGroups: [{ pattern: 'a*.csv' }] }, {
      uploads: [{ path: 'a1.csv', status: 'VALID' }],
      complete: true,
      hasListings: true
    })

    expect(view.$('tbody tr').length).toBe(2)
    expect(row('a*.csv').find('.expected-status').text()).toBe('1 uploaded')
    expect(view.$el.text()).not.toContain('undefined')
  })

  it('explains how uploaded names are stored', function () {
    render(expected, { uploads: [], complete: false, hasListings: false })

    const note = view.$('.expected-naming').text()
    expect(note).toContain('stored with names in lower case and hyphens in place of spaces')
    expect(note).toContain('compared with the agreement ignoring case and treating spaces as hyphens')
    expect(note).toContain('data.zip containing site1/x.csv gives data/site1/x.csv')
  })

  describe('with a prose naming convention', function () {
    const withConvention = { ...expected, hasNamingConvention: true }
    const uploads = [{ path: 'extra/notes.csv', status: 'VALID' }]

    it('does not call unlisted uploads unexpected, nor compare the total', function () {
      render(withConvention, { uploads, complete: true, hasListings: true })

      expect(view.$('.expected-unmatched').length).toBe(0)
      expect(view.$('.expected-total').length).toBe(0)
      expect(view.$el.text()).not.toContain('not in the agreement')
    })

    it('says files not listed may still be expected', function () {
      render(withConvention, { uploads, complete: true, hasListings: true })

      expect(view.$('.expected-convention').text()).toBe(
        'Some files are described by the agreement\'s naming convention, so files not listed here may still be expected.')
    })

    it('still ticks off what is listed', function () {
      render(withConvention, { uploads: [{ path: 'site1/2024.csv', status: 'VALID' }], complete: true, hasListings: true })

      expect(row('site1/2024.csv').find('.expected-status').text()).toBe('uploaded')
      expect(row('readme.txt').find('.expected-status').text()).toBe('not yet uploaded')
    })
  })

  it('says nothing of a naming convention without one', function () {
    render(expected, { uploads: [{ path: 'extra/notes.csv', status: 'VALID' }], complete: true, hasListings: true })

    expect(view.$('.expected-convention').length).toBe(0)
    expect(view.$('.expected-unmatched').length).toBe(1)
  })

  it('ticks off a name typed with capitals once it arrives in lower case', function () {
    render({ files: [{ name: 'Site1/Data.csv' }], fileGroups: [] }, {
      uploads: [{ path: 'site1/data.csv', status: 'VALID' }],
      complete: true,
      hasListings: true
    })

    expect(row('Site1/Data.csv').find('.expected-status').text()).toBe('uploaded')
  })

  it('says when the agreement lists no files', function () {
    render({ files: [], fileGroups: [] }, { uploads: [], complete: true, hasListings: true })

    expect(view.$('table').length).toBe(0)
    expect(view.$('.expected-none').text()).toBe('The service agreement lists no files.')
  })

  it('shows depositor text as text (dri-one #454)', function () {
    const attack = '<img src=x onerror="window.__xss536=1">'
    render({
      files: [{ name: `${attack}.csv`, format: attack, size: attack }],
      fileGroups: [{ pattern: `${attack}*`, description: attack, format: attack, size: attack, count: 1 }]
    }, {
      uploads: [{ path: `x/${attack}`, status: 'VALID' }],
      complete: true,
      hasListings: true
    })

    expect(view.$('img').length).toBe(0)
    expect(view.$el.html()).toContain('&lt;img src=x onerror="window.__xss536=1"&gt;')
    expect(row(`${attack}.csv`).length).toBe(1)
    expect(view.$('.expected-unmatched li').text()).toContain(`x/${attack}`)
  })
})
