import _ from 'underscore'
import ObjectInputView from './ObjectInputView'
import ChildView from './ChildView'
import KeywordVocabularyView from './KeywordVocabularyView'
import template from '../templates/DescriptiveKeyword.tpl'

export default ObjectInputView.extend({

  events: function () {
    return _.extend({}, ObjectInputView.prototype.events, {
      'click .add' () { return this.add() },

      'click .predefined' (event) { return this.addPredefined(event) }
    })
  },

  initialize (options) {
    this.template = _.template(template)
    ObjectInputView.prototype.initialize.call(this, options)
    this.render()
    const keywordType = this.model.get('type')
    if (keywordType != null) {
      // IE only supports .startsWith() in MS Edge (> version 11)
      if (keywordType.lastIndexOf('Catalogue topic', 0) === 0) {
        this.$('#catalogueTopic').removeClass('hidden')
        this.$('.add').addClass('hidden')
        this.$('select.type').attr('disabled', 'disabled')
      }
    }

    this.keywords = this.model.getRelatedCollection('keywords')
    this.createList(this.keywords, '.keywords', this.addOne)
  },

  render () {
    ObjectInputView.prototype.render.apply(this)
    this.$('.type').val(this.model.get('type'))
    return this
  },

  addOne (model, keywordIndex) {
    this.data = _.omit(this.data, 'el')
    return new ChildView(_.extend({}, this.data, {
      model,
      keywordIndex,
      ObjectInputView: KeywordVocabularyView
    }))
  },

  add () { this.keywords.add({}) },

  addPredefined (event) {
    event.preventDefault()
    const $target = this.$(event.target)
    this.keywords.add({
      value: $target.text(),
      uri: $target.attr('href')
    })
  },

  modify (event) {
    const name = $(event.target).data('name')
    const value = $(event.target).val()

    if (value) {
      this.model.set(name, value)
    } else {
      this.model.unset(name)
    }
  }
})
