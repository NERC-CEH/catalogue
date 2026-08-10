import $ from 'jquery'
import { SearchFormView } from '../src/views'
import SearchApp from '../src/SearchApp'

describe('Test SearchFormView', () => {
  let view = null
  let model = null

  beforeEach(() => {
    $(document.body).html("<form id='searchForm'><input name='term' value='rivers'></form>")
    model = new SearchApp({})
    view = new SearchFormView({
      el: '#searchForm',
      model
    })
  })

  afterEach(() => {
    $(document.body).empty()
  })

  it('View should be defined', () => {
    expect(view).toBeDefined()
  })

  it('reads the displayed term from the term input', () => {
    expect(view.getDisplayedTerm()).toBe('rivers')
  })

  it('prevents the default form submission', () => {
    const e = jasmine.createSpyObj('event', ['preventDefault'])
    view.handleSubmit(e)
    expect(e.preventDefault).toHaveBeenCalled()
  })

  it('clears results and pushes the new term to the model when typing changes it', () => {
    spyOn(model, 'clearResults')

    view.handleTyping()

    expect(model.clearResults).toHaveBeenCalled()
    expect(model.get('term')).toBe('rivers')
  })

  it('does nothing when the displayed term already matches the model', () => {
    model.set('term', 'rivers')
    spyOn(model, 'clearResults')

    view.handleTyping()

    expect(model.clearResults).not.toHaveBeenCalled()
  })
})
