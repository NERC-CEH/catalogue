import $ from 'jquery'
import Backbone from 'backbone'
import { fetchVariablesFromLegilo } from '../src/views/LegiloFetcher'
import variablesTemplate from '../src/templates/LegiloVariables'
import LegiloView from '../src/views/LegiloView'
import { variableOnSelect } from '../src/views/LegiloEventHandler'
import { LegiloVariable } from '../src/models'

describe('Test LegiloVariables View', () => {
  let view = null
  const mockSingleSuggestion = [
    new LegiloVariable({ name: 'test1', longName: 'longName1', units: 'unit1', meaning: 'meaning1', confidence: 0.7 })
  ]
  const mockMultipleSuggestions = [
    new LegiloVariable({ name: 'test1', longName: 'longName1', units: 'unit1', meaning: 'meaning1', confidence: 0.7 }),
    new LegiloVariable({ name: 'test2', longName: 'longName2', units: 'unit2', meaning: 'meaning2', confidence: 0.8 })
  ]

  $('<div class="legilo-variables-view"></div>').appendTo('body')
  $('<button class="legilo-variables-btn"></button>').appendTo('body')
  const legiloButton = $('.legilo-variables-btn')
  const parentView = $('.legilo-variables-view')

  beforeEach(() => {
    const model = new Backbone.Model({ id: '123' })
    const collection = new Backbone.Collection()

    view = new LegiloView({
      collection,
      model,
      modelType: LegiloVariable,
      template: variablesTemplate,
      fetcher: fetchVariablesFromLegilo,
      fetchButton: legiloButton,
      result: parentView,
      onSelect: variableOnSelect
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
    expect(view.collection.at(0).get('value')).toBe('test1')
    expect(view.selectedSuggestions.length).toBe(0)
  })

  it('fetch should fetch suggestions from the API', async () => {
    const mockApiResponse = [
      { name: 'test1', longName: 'longName1', units: 'unit1', meaning: 'meaning1', confidence: 0.7 },
      { name: 'test2', longName: 'longName2', units: 'unit2', meaning: 'meaning2', confidence: 0.8 }
    ]
    spyOn($, 'getJSON').and.returnValue(Promise.resolve(mockApiResponse))

    legiloButton.trigger('click')
    await new Promise(resolve => setTimeout(resolve, 0))

    expect($.getJSON).toHaveBeenCalledWith('/documents/123/suggestVariables')
    expect(view.suggestions.length).toBe(2)
    expect(view.suggestions[0].get('name')).toBe('test1')
    expect(view.suggestions[0].get('longName')).toBe('longName1')
    expect(view.suggestions[0].get('units')).toBe('unit1')
    expect(view.suggestions[0].get('meaning')).toBe('meaning1')
    expect(view.suggestions[0].get('confidence')).toBe(0.7)
  })
})
