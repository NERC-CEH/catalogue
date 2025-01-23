import _ from 'underscore'
import $ from 'jquery'
import Backbone from 'backbone'
import ObjectInputView from './ObjectInputView'
import template from '../templates/KeywordVocabulary'
import KeywordCheckboxView from './KeywordCheckboxView'
import { vocabsPredefined, vocabsList } from './KeywordVocabularyConfig'

export default ObjectInputView.extend({

  initialize (options) {
    this.template = template
    ObjectInputView.prototype.initialize.apply(this)
    this.vocabularies = new Backbone.Collection()
    const catalogue = $('html').data('catalogue')
    this.$vocabularies = this.$('.vocabularyPicker')
    this.listenTo(this.vocabularies, 'add', this.addOne)
    this.listenTo(this.vocabularies, 'reset', this.addAll)
    this.data = options

    if (('vocabs' in this.data) && (catalogue in this.data.vocabs)) {
      this.vocabularies.reset(
        this.data.vocabs[catalogue]
        .filter(vocab => vocabsList.has(vocab))
        .map(vocab => vocabsList.get(vocab))
      )
    }
    if ((this.vocabularies.length == 0) && (catalogue in vocabsPredefined)) {
      const vocabs = vocabsPredefined[catalogue]
      this.vocabularies.reset(
        Object.keys(vocabs).map(vocab => vocabs[vocab])
      )
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

    if (this.vocabularies.length == 0) {
      this.$('.keywordPicker').addClass('d-none')
    } else {
      this.$('.autocomplete').autocomplete({
        minLength: 2,
        source: (request, response) => {
          let query
          const vocab = _.pluck(this.vocabularies.where({ toSearch: true }), 'id')
          const term = request.term.trim()
          if (_.isEmpty(term)) {
            query = `/vocabulary/keywords?vocab=${vocab}`
          } else {
            query = `/vocabulary/keywords?query=${request.term}&vocab=${vocab}`
          }
          $.getJSON(query, data => response(_.map(data, d => ({
            value: d.label,
            label: `${d.label} (${d.vocabId})`,
            url: d.url
          }))))
        },
        select: (event, ui) => {
          this.model.set('value', ui.item.value)
          this.$('.value').val(ui.item.value)
          this.model.set('uri', ui.item.url)
          this.$('.uri').val(ui.item.url)
          this.$('.keywordPicker').addClass('d-none')
          this.$('.uri').attr('disabled', true)
          this.$('.value').attr('disabled', true)
        }
      })
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
