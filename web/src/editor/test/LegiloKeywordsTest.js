import $ from 'jquery'
import Backbone from 'backbone'
import { fetchKeywordsFromLegilo } from '../src/views/LegiloFetcher'
import keywordsTemplate from '../src/templates/LegiloKeywords'
import LegiloView from '../src/views/LegiloView'
import { keywordOnSelect } from '../src/views/LegiloEventHandler'
import { LegiloKeyword } from '../src/models'

describe('Test LegiloKeywords View', () => {
  let view = null
  const mockSingleSuggestion = [
    new LegiloKeyword({ name: 'temperature', confidence: 0.63, uri: 'custom-uri1' })
  ]
  const mockMultipleSuggestions = [
    new LegiloKeyword({ name: 'temperature', confidence: 0.63, uri: 'custom-uri1' }),
    new LegiloKeyword({ name: 'pressure', confidence: 0.45, uri: 'custom-uri2' })
  ]

  $('<div class="legilo-keywords-view"></div>').appendTo('body')
  $('<button class="legilo-keywords-btn"></button>').appendTo('body')
  const legiloButton = $('.legilo-keywords-btn')
  const parentView = $('.legilo-keywords-view')

  beforeEach(() => {
    const model = new Backbone.Model({ id: '123' })
    const collection = new Backbone.Collection()

    view = new LegiloView({
      collection,
      model,
      modelType: LegiloKeyword,
      template: keywordsTemplate,
      fetcher: fetchKeywordsFromLegilo,
      fetchButton: legiloButton,
      result: parentView,
      onSelect: keywordOnSelect
    })
  })

  afterEach(() => {
    view.remove()
  })

  it('View should be defined', () => {
    expect(view).toBeDefined()
  })

  it('should render suggestions and make table visible', () => {
    view.model.set(view.fetcher.name, mockMultipleSuggestions)
    view.render()

    expect(parentView.find('.suggestions-table-header').css('display')).not.toBe('none')
    expect(parentView.find('.suggestions-table').css('display')).not.toBe('none')
    expect(parentView.find('.suggestions-buttons').css('display')).not.toBe('none')

    expect(parentView.find('.suggestions-table-body').children().length).toBe(2)
  })

  it('showNoSuggestionsMessage should display the message when no keywords are available', () => {
    view.model.set(view.fetcher.name, [])
    view.render()

    expect(parentView.find('.no-suggestions-message').text()).toContain('No suggestions available.')
    expect(parentView.find('.suggestions-table').is(':visible')).toBeFalse()
  })

  it('close should hide the table and buttons', () => {
    view.model.set(view.fetcher.name, mockMultipleSuggestions)
    view.render()

    view.close()
    expect(parentView.find('.suggestions-table').is(':visible')).toBeFalse()
    expect(parentView.find('.suggestions-buttons').is(':visible')).toBeFalse()
    expect(parentView.find('.suggestions-table-header').is(':visible')).toBeFalse()
  })

  it('toggleSuggestionsSelection should add and remove suggesgtion from selectedKeywords', () => {
    view.model.set(view.fetcher.name, mockSingleSuggestion)
    view.render()

    parentView.find('.suggestions-checkbox').prop('checked', true).trigger('change')
    expect(view.selectedSuggestions.length).toBe(1)

    parentView.find('.suggestions-checkbox').prop('checked', false).trigger('change')
    expect(view.selectedSuggestions.length).toBe(0)
  })

  it('addSelectedSuggestions should add selected suggestions to the collection', () => {
    view.model.set(view.fetcher.name, mockSingleSuggestion)
    view.render()

    parentView.find('.suggestions-checkbox').prop('checked', true).trigger('change')

    view.addSelectedSuggestions()

    expect(view.collection.length).toBe(1)
    expect(view.collection.at(0).get('value')).toBe('temperature')
    expect(view.selectedSuggestions.length).toBe(0)
  })

  it('fetch should fetch suggestions from the API', async () => {
    const mockApiResponse = [
      { name: 'temperature', confidence: 0.9, matched_url: 'custom-url1' },
      { name: 'pressure', confidence: 0.8, matched_url: 'custom-url2' }
    ]
    spyOn($, 'getJSON').and.returnValue(Promise.resolve(mockApiResponse))

    legiloButton.trigger('click')
    await new Promise(resolve => setTimeout(resolve, 0))

    expect($.getJSON).toHaveBeenCalledWith('/documents/123/suggestKeywords')
    expect(view.suggestions.length).toBe(2)
    expect(view.suggestions[0].get('name')).toBe('temperature')
    expect(view.suggestions[0].get('confidence')).toBe(0.9)
    expect(view.suggestions[0].get('uri')).toBe('custom-url1')
  })
})
