import $ from 'jquery'
import Backbone from 'backbone'
import { LegiloKeywords, fetchKeywordsFromLegilo } from '../src/views'
import { LegiloKeyword } from '../src/models'

describe('Test LegiloKeywords View', () => {
  let view = null

  beforeEach(() => {
    const model = new Backbone.Model({ id: '123' })
    const collection = new Backbone.Collection()

    view = new LegiloKeywords({
      model,
      collection
    })

    view.render()
  })

  afterEach(() => {
    view.remove()
  })

  it('View should be defined', () => {
    expect(view).toBeDefined()
  })

  it('should render keywords and make table visible', () => {
    const mockKeywords = [
      new LegiloKeyword({ name: 'temperature', confidence: 0.63, uri: 'custom-uri1' }),
      new LegiloKeyword({ name: 'pressure', confidence: 0.45, uri: 'custom-uri2' })
    ]

    view.renderKeywords(mockKeywords)

    expect(view.$('.keyword-table-header').css('display')).not.toBe('none')
    expect(view.$('.keywords-table').css('display')).not.toBe('none')
    expect(view.$('.keywords-buttons').css('display')).not.toBe('none')

    expect(view.$('.keywords-table-body').children().length).toBe(2)
  })

  it('showNoKeywordsMessage should display the message when no keywords are available', () => {
    view.renderKeywords([])

    expect(view.$('.no-keywords-message').text()).toContain('No suggested keywords available.')
    expect(view.$('.keywords-table').is(':visible')).toBeFalse()
  })

  it('close should hide the table and buttons', () => {
    view.renderKeywords([new LegiloKeyword({ name: 'temperature', confidence: 0.63, uri: 'custom-uri1' })])
    view.showTableAndButtons()

    view.close()
    expect(view.$('.keywords-table').is(':visible')).toBeFalse()
    expect(view.$('.keywords-buttons').is(':visible')).toBeFalse()
    expect(view.$('.keyword-table-header').is(':visible')).toBeFalse()
  })

  it('toggleKeywordSelection should add and remove keywords from selectedKeywords', () => {
    const mockKeywords = [
      new LegiloKeyword({ name: 'temperature', confidence: 0.63, uri: 'custom-uri1' })
    ]

    view.renderKeywords(mockKeywords)

    view.$('.keyword-checkbox').prop('checked', true).trigger('change')
    expect(view.selectedKeywords.length).toBe(1)

    view.$('.keyword-checkbox').prop('checked', false).trigger('change')
    expect(view.selectedKeywords.length).toBe(0)
  })

  it('addSelectedKeywords should add selected keywords to the collection', () => {
    const mockKeywords = [
      new LegiloKeyword({ name: 'temperature', confidence: 0.63, uri: 'custom-uri1' })
    ]

    view.renderKeywords(mockKeywords)

    view.$('.keyword-checkbox').prop('checked', true).trigger('change')

    view.addSelectedKeywords()

    expect(view.collection.length).toBe(1)
    expect(view.collection.at(0).get('value')).toBe('temperature')
    expect(view.selectedKeywords.length).toBe(0)
  })

  it('fetchKeywordsFromLegilo should fetch keywords from the API', (done) => {
    const mockApiResponse = [
      { name: 'temperature', confidence: 0.9, matched_url: 'custom-url1' },
      { name: 'pressure', confidence: 0.8, matched_url: 'custom-url2' }
    ]

    spyOn($, 'getJSON').and.returnValue(Promise.resolve(mockApiResponse))

    fetchKeywordsFromLegilo(view.model).then((keywords) => {
      expect($.getJSON).toHaveBeenCalledWith('/documents/123/suggestKeywords')

      expect(keywords.length).toBe(2)
      expect(keywords[0].get('name')).toBe('temperature')
      expect(keywords[0].get('confidence')).toBe(0.9)
      expect(keywords[0].get('uri')).toBe('custom-url1')

      done()
    })
  })
})
