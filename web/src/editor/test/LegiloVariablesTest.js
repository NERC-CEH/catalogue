import $ from 'jquery'
import Backbone from 'backbone'
import { LegiloVariables, fetchVariablesFromLegilo } from '../src/views'
import { LegiloVariable, ObservedProperty } from '../src/models'

describe('Test LegiloVariables View', () => {
  let view = null
  const mockVariables = [
    new LegiloVariable({ name: 'test1', longName: 'longName1', units: 'unit1', meaning: 'meaning1', confidence: 0.7 }),
    new LegiloVariable({ name: 'test2', longName: 'longName2', units: 'unit2', meaning: 'meaning1', confidence: 0.8 })
  ]

  beforeEach(() => {
    const model = new Backbone.Model({ id: '123' })
    const collection = new Backbone.Collection()
    view = new LegiloVariables({
      model,
      collection,
      modelType: ObservedProperty
    })

    view.render()
  })

  afterEach(() => {
    view.remove()
  })

  it('View should be defined', () => {
    expect(view).toBeDefined()
  })

  it('should render variables and make table visible', () => {
    view.renderVariables(mockVariables)

    expect(view.$('.variable-table-header').css('display')).not.toBe('none')
    expect(view.$('.variables-table').css('display')).not.toBe('none')
    expect(view.$('.variables-buttons').css('display')).not.toBe('none')

    expect(view.$('.variables-table-body').children().length).toBe(2)
  })

  it('showNoVariablesMessage should display the message when no variables are available', () => {
    view.renderVariables([])

    expect(view.$('.no-variables-message').text()).toContain('No suggested variables available.')
    expect(view.$('.variables-table').is(':visible')).toBeFalse()
  })

  it('close should hide the table and buttons', () => {
    view.renderVariables(mockVariables)
    view.showTableAndButtons()

    view.close()
    expect(view.$('.variables-table').is(':visible')).toBeFalse()
    expect(view.$('.variables-buttons').is(':visible')).toBeFalse()
    expect(view.$('.variable-table-header').is(':visible')).toBeFalse()
  })

  it('toggleVariableSelection should add and remove variables from selectedVariables', () => {
    view.renderVariables(mockVariables)

    view.$('.variable-checkbox[data-value="test1"]').prop('checked', true).trigger('change')
    expect(view.selectedVariables.length).toBe(1)

    view.$('.variable-checkbox[data-value="test1"]').prop('checked', false).trigger('change')
    expect(view.selectedVariables.length).toBe(0)
  })

  it('addSelectedVariables should add selected variables to the collection', () => {
    view.renderVariables(mockVariables)

    view.$('.variable-checkbox[data-value="test1"]').prop('checked', true).trigger('change')

    view.addSelectedVariables()

    expect(view.collection.length).toBe(1)
    expect(view.collection.at(0).get('value')).toBe('test1')
    expect(view.selectedVariables.length).toBe(0)
  })

  it('fetchVariablesFromLegilo should fetch variables from the API', (done) => {
    const mockApiResponse = [
      { name: 'test1', longName: 'longName1', units: 'unit1', meaning: 'meaning1', confidence: 0.7 },
      { name: 'test2', longName: 'longName2', units: 'unit2', meaning: 'meaning2', confidence: 0.8 }
    ]

    spyOn($, 'getJSON').and.returnValue(Promise.resolve(mockApiResponse))

    fetchVariablesFromLegilo(view.model).then((variables) => {
      expect($.getJSON).toHaveBeenCalledWith('/documents/123/suggestVariables')

      expect(variables.length).toBe(2)
      expect(variables[0].get('name')).toBe('test1')
      expect(variables[0].get('longName')).toBe('longName1')
      expect(variables[0].get('units')).toBe('unit1')
      expect(variables[0].get('meaning')).toBe('meaning1')
      expect(variables[0].get('confidence')).toBe(0.7)

      done()
    })
  })
})
