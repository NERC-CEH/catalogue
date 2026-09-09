import $ from 'jquery'
import { SearchFormView } from '../src/views'
import SearchApp from '../src/SearchApp'

describe('Test SearchFormView', () => {
  let view = null
  let model = null

  // Mirrors templates/html/search.ftlh: both term controls are present in the
  // markup, the textarea starting out disabled and hidden.
  const formHtml = `
    <form id='searchForm'>
      <div class='input-group'>
        <input name='term' type='text' value='rivers' class='form-control'>
        <textarea name='term' rows='4' class='form-control d-none' disabled></textarea>
        <button type='submit' class='btn'></button>
      </div>
      <input type='checkbox' name='semantic' value='true'>
    </form>`

  const build = (attributes = {}) => {
    $(document.body).html(formHtml)
    model = new SearchApp(attributes)
    view = new SearchFormView({
      el: '#searchForm',
      model
    })
  }

  const input = () => $("input[name='term']")
  const textarea = () => $("textarea[name='term']")

  beforeEach(() => build())

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

  describe('keyword mode display', () => {
    it('leaves the input active and the textarea disabled', () => {
      expect(input().prop('disabled')).toBe(false)
      expect(input().hasClass('d-none')).toBe(false)
      expect(textarea().prop('disabled')).toBe(true)
      expect(textarea().hasClass('d-none')).toBe(true)
    })

    it('does not mark the form as being in semantic mode', () => {
      expect(view.$el.hasClass('semantic-mode')).toBe(false)
    })
  })

  describe('semantic mode display', () => {
    beforeEach(() => model.set('semantic', true))

    it('swaps the single line input for the multi row textarea', () => {
      expect(input().prop('disabled')).toBe(true)
      expect(input().hasClass('d-none')).toBe(true)
      expect(textarea().prop('disabled')).toBe(false)
      expect(textarea().hasClass('d-none')).toBe(false)
    })

    it('marks the form as being in semantic mode', () => {
      expect(view.$el.hasClass('semantic-mode')).toBe(true)
    })

    it('carries the typed term across to the textarea', () => {
      expect(textarea().val()).toBe('rivers')
      expect(view.getDisplayedTerm()).toBe('rivers')
    })

    it('carries the term back to the input when semantic mode is turned off', () => {
      textarea().val('rivers in the uplands of Wales')

      model.set('semantic', false)

      expect(input().val()).toBe('rivers in the uplands of Wales')
      expect(view.getDisplayedTerm()).toBe('rivers in the uplands of Wales')
    })

    it('reflects the mode when the page loads already in semantic mode', () => {
      build({ semantic: true })

      expect(textarea().prop('disabled')).toBe(false)
      expect(input().prop('disabled')).toBe(true)
      expect($("[name='semantic']").is(':checked')).toBe(true)
    })
  })

  describe('semantic mode query submission', () => {
    beforeEach(() => {
      model.set({ semantic: true, term: 'rivers' })
      textarea().val('rivers in the uplands of Wales')
    })

    it('does not put the typed term on the model while typing', () => {
      view.handleTyping()

      expect(model.get('term')).toBe('rivers')
    })

    it('leaves the already rendered results alone while typing', () => {
      spyOn(model, 'clearResults')

      view.handleTyping()

      expect(model.clearResults).not.toHaveBeenCalled()
    })

    it('flags the query as pending while it is unsubmitted', () => {
      view.handleTyping()

      expect(view.$el.hasClass('term-pending')).toBe(true)
    })

    it('clears the pending flag once the query is submitted', () => {
      view.handleTyping()

      view.handleSubmit(jasmine.createSpyObj('event', ['preventDefault']))

      expect(view.$el.hasClass('term-pending')).toBe(false)
    })

    it('clears the pending flag when the typing is reverted', () => {
      view.handleTyping()

      textarea().val('rivers')
      view.handleTyping()

      expect(view.$el.hasClass('term-pending')).toBe(false)
    })

    it('puts the typed term on the model when the form is submitted', () => {
      const e = jasmine.createSpyObj('event', ['preventDefault'])

      view.handleSubmit(e)

      expect(model.get('term')).toBe('rivers in the uplands of Wales')
    })

    it('submits the term when the search button is clicked', () => {
      $('#searchForm button[type=submit]').trigger('click')

      expect(model.get('term')).toBe('rivers in the uplands of Wales')
    })
  })

  describe('semantic checkbox', () => {
    it('puts the checkbox state onto the model', () => {
      $("[name='semantic']").prop('checked', true).trigger('change')

      expect(model.get('semantic')).toBe(true)
    })

    it('commits a term typed but not yet submitted when semantic mode is left', () => {
      model.set({ semantic: true, term: 'rivers' })
      textarea().val('rivers in the uplands of Wales')

      $("[name='semantic']").prop('checked', false).trigger('change')

      expect(model.get('semantic')).toBe(false)
      expect(model.get('term')).toBe('rivers in the uplands of Wales')
    })

    it('follows the model when semantic mode is changed elsewhere', () => {
      model.set('semantic', true)

      expect($("[name='semantic']").is(':checked')).toBe(true)
    })
  })

  describe('term changes made on the model', () => {
    it('updates both term controls so neither goes stale', () => {
      model.set('term', 'soil carbon')

      expect(input().val()).toBe('soil carbon')
      expect(textarea().val()).toBe('soil carbon')
    })

    it('clears both term controls when the term is removed', () => {
      model.set('term', 'soil carbon')

      model.unset('term')

      expect(input().val()).toBe('')
      expect(textarea().val()).toBe('')
    })
  })

  describe('focusTerm', () => {
    it('focuses the input in keyword mode', () => {
      view.focusTerm()

      expect(document.activeElement).toBe(input()[0])
    })

    it('focuses the textarea in semantic mode', () => {
      model.set('semantic', true)

      view.focusTerm()

      expect(document.activeElement).toBe(textarea()[0])
    })
  })
})
