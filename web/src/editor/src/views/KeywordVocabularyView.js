import _ from 'underscore'
import $ from 'jquery'
import Backbone from 'backbone'
import ObjectInputView from './ObjectInputView'
import template from '../templates/KeywordVocabulary'
import KeywordCheckboxView from './KeywordCheckboxView'
import { vocabsList } from './KeywordVocabularyConfig'
import { autocomplete } from '../utils/autocomplete'

export default ObjectInputView.extend({

  initialize (options) {
    this.template = template
    ObjectInputView.prototype.initialize.apply(this, arguments)
    this.vocabularies = new Backbone.Collection()
    const catalogue = $('html').data('catalogue')
    this.$vocabularies = this.$('.vocabularyPicker')
    this.listenTo(this.vocabularies, 'add', this.addOne)
    this.listenTo(this.vocabularies, 'reset', this.addAll)
    this.data = options
    if ('vocabs' in this.data && catalogue in this.data.vocabs) {
      this.vocabularies.reset(
        this.data.vocabs[catalogue]
          .filter(vocab => vocabsList.has(vocab))
          .map(vocab => vocabsList.get(vocab))
      )
    } else {
      this.vocabularies.reset([])
    }

    const kwurl = this.model.get('uri')
    const kwvalue = this.model.get('value')
    if (kwurl != null && kwvalue != null) {
      this.$('.keywordPicker').addClass('d-none')
      this.$('.uri').attr('disabled', true)
      this.$('.value').attr('disabled', true)
    } else if (kwvalue != null) {
      this.$('.keywordPicker').addClass('d-none')
    }

    if (this.vocabularies.length === 0) {
      this.$('.keywordPicker').addClass('d-none')
    } else {
      this.$('.autocomplete').each((i, input) => autocomplete(input, {
        minLength: 2,
        source: async term => {
          const vocab = _.pluck(this.vocabularies.where({ toSearch: true }), 'id').join(',')
          term = term.trim()
          const params = _.isEmpty(term) ? { vocab } : { query: term, vocab }
          try {
            const data = await $.getJSON(`/vocabulary/keywords?${$.param(params)}`)
            return _.map(data, d => ({
              value: d.label,
              label: `${d.label} (${d.vocabId})`,
              url: d.url
            }))
          } catch {
            return []
          }
        },
        select: item => {
          this.model.set('value', item.value)
          this.$('.value').val(item.value)
          this.model.set('uri', item.url)
          this.$('.uri').val(item.url)
          this.$('.keywordPicker').addClass('d-none')
          this.$('.uri').attr('disabled', true)
          this.$('.value').attr('disabled', true)
        }
      }))
    }
  },

  addAll () {
    this.vocabularies.each(this.addOne, this)
  },

  addOne (vocabulary) {
    vocabulary.set({ toSearch: true })
    const view = new KeywordCheckboxView({ model: vocabulary })
    this.$vocabularies.append(view.render().el)
  }
})
