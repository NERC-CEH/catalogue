import _ from 'underscore'
import $ from 'jquery'
import Backbone from 'backbone'
import ObjectInputView from './ObjectInputView'
import template from '../templates/KeywordVocabulary.tpl'
import KeywordCheckboxView from './KeywordCheckboxView'
import 'jquery-ui/ui/widgets/autocomplete'

export default ObjectInputView.extend({

  initialize () {
    this.template = _.template(template)
    ObjectInputView.prototype.initialize.apply(this)
    this.vocabularies = new Backbone.Collection()
    const catalogue = $('html').data('catalogue')
    this.$vocabularies = this.$('.vocabularyPicker')
    this.listenTo(this.vocabularies, 'add', this.addOne)
    this.listenTo(this.vocabularies, 'reset', this.addAll)
    $.getJSON(`/catalogues/${catalogue}`, data => {
      return this.vocabularies.reset(data.vocabularies)
    })

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
        return $.getJSON(query, data => response(_.map(data, d => ({
          value: d.label,
          label: `${d.label} (${d.vocabId})`,
          url: d.url
        }))))
      }
    })

    return this.$('.autocomplete').on('autocompleteselect', (event, ui) => {
      this.model.set('value', ui.item.value)
      this.$('.value').val(ui.item.value)
      this.model.set('uri', ui.item.url)
      return this.$('.uri').val(ui.item.url)
    })
  },

  addAll () {
    return this.vocabularies.each(this.addOne, this)
  },

  addOne (vocabulary) {
    vocabulary.set({ toSearch: true })
    const view = new KeywordCheckboxView({ model: vocabulary })
    return this.$vocabularies.append(view.render().el)
  }
})
