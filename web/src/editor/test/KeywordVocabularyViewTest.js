import $ from 'jquery'
import Backbone from 'backbone'
import { KeywordVocabularyView } from '../src/views'

describe('Test KeywordVocabularyView', () => {
  let view = null
  let model = null
  beforeEach(() => {
    model = new Backbone.Model({ title: 'test' })
  })

  it('view should be defined', () => {
    view = new KeywordVocabularyView({ model })
    expect(view).toBeDefined()
  })

  it('keyword option should be shown if vocabs is defined', () => {
    $('html').data('catalogue', 'eidc')
    view = new KeywordVocabularyView({
      model,
      vocabs: { eidc: ['cast'] }
    })
    expect(view.$('.keywordPicker').css('display')).not.toBe('none')
    expect(view.$('.vocabularyPicker').children().length).toBe(1)
    expect(view.$('.vocabularyPicker').html()).toContain('CAST')
  })

  it('no keyword option should be shown if vocabs is defined empty', () => {
    $('html').data('catalogue', 'eidc')
    view = new KeywordVocabularyView({
      model,
      vocabs: { eidc: [] }
    })
    expect(view.$('.keywordPicker').is(':visible')).toBeFalse()
    expect(view.$('.vocabularyPicker').children().length).toBe(0)
  })

  it('no keyword option should be shown if catalogue vocabs not configured', () => {
    $('html').data('catalogue', 'not_exist')
    view = new KeywordVocabularyView({
      model
    })
    expect(view.$('.keywordPicker').is(':visible')).toBeFalse()
    expect(view.$('.vocabularyPicker').children().length).toBe(0)
  })

  it('predefined keyword option should be shown if vocabs is not defined', () => {
    $('html').data('catalogue', 'eidc')
    view = new KeywordVocabularyView({
      model
    })
    expect(view.$('.keywordPicker').css('display')).not.toBe('none')
    expect(view.$('.vocabularyPicker').children().length).toBe(3)
    expect(view.$('.vocabularyPicker').html()).toContain('CAST')
    expect(view.$('.vocabularyPicker').html()).toContain('EnvThes')
    expect(view.$('.vocabularyPicker').html()).toContain('GEMET')
  })

  it('predefined keyword option should be shown if vocabs is not defined for running catalogue', () => {
    $('html').data('catalogue', 'eidc')
    view = new KeywordVocabularyView({
      model,
      vocabs: { ukeof: ['gemet'] }
    })
    expect(view.$('.keywordPicker').css('display')).not.toBe('none')
    expect(view.$('.vocabularyPicker').children().length).toBe(3)
    expect(view.$('.vocabularyPicker').html()).toContain('CAST')
    expect(view.$('.vocabularyPicker').html()).toContain('EnvThes')
    expect(view.$('.vocabularyPicker').html()).toContain('GEMET')
  })
})
