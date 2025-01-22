import _ from 'underscore'
import $ from 'jquery'
import Backbone from 'backbone'
import ObjectInputView from './ObjectInputView'
import template from '../templates/KeywordVocabulary'
import KeywordCheckboxView from './KeywordCheckboxView'

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
    const vocabsList = new Map([
      ['assist-topics', { id: 'assist-topics', name: 'Topics' }],
      ['assist-research-themes', { id: 'assist-research-themes', name: 'Research Themes' }],
      ['cast', { id: 'cast', name: 'CAST' }],
      ['envthes', { id: 'envThes', name: 'EnvThes' }],
      ['dukems-pollutant', { id: 'dukems-pollutant', name: 'Pollutants' }],
      ['dukems-sector', { id: 'dukems-sector', name: 'Sectors' }],
      ['elterCL', { id: 'elterCL', name: 'elterCL' }],
      ['gemet', { id: 'gemet', name: 'GEMET' }],
      ['inms', { id: 'inms', name: 'INMS' }],
      ['research-theme', { id: 'research-theme', name: 'Research themes' }],
      ['research-project', { id: 'research-project', name: 'Research projects' }],
      ['science-challenge', { id: 'science-challenge', name: 'Science challenges' }],
      ['service', { id: 'service', name: 'Services' }]
    ])
    if ('vocabs' in this.data) {
      this.vocabularies.reset(
        this.data.vocabs
          .filter(vocab => {
            if (vocab.indexOf(':') > 0) {
              if (vocab.split(':')[0] != catalogue) {
                return false
              }
            }
            return true
          })
          .map(vocab => vocabsList.get(vocab.replace(/^\w+:/, '')))
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

    if (this.vocabularies.length <= 0) {
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
