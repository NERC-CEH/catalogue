import Backbone from 'backbone'
import $ from 'jquery'
import template from '../templates/LegiloKeywords'
import { LegiloKeyword } from '../models'

export default Backbone.View.extend({

  initialize (options) {
    this.model = options.model
    this.collection = options.collection
    this.template = template
    this.selectedKeywords = []
    this.keywords = []
    this.keywordsToShow = 10
  },

  events: {
    'click .legilo-close-btn': 'close',
    'click .legilo-add-btn': 'addSelectedKeywords',
    'change .keyword-checkbox': 'toggleKeywordSelection',
    'click .legilo-load-more-btn': 'loadAllKeywords'
  },

  render () {
    this.$el.html(this.template())
    return this
  },

  renderKeywords (keywords) {
    this.keywords = keywords

    const filteredKeywords = this.keywords.filter(keyword => {
      return !this.collection.findWhere({ value: keyword.get('name') })
    })

    if (this.keywords.length > 0 && filteredKeywords.length === 0) {
      this.showNoKeywordsMessage('All suggested keywords are already present or have been added.')
      return
    }

    if (keywords.length === 0) {
      this.showNoKeywordsMessage('No suggested keywords available.')
      return
    }

    const keywordsToDisplay = filteredKeywords.slice(0, this.keywordsToShow)

    const rowsHTML = keywordsToDisplay.map(keyword => {
      const uri = keyword.get('uri') || ''
      const isChecked = this.selectedKeywords.some(selected => selected.value === keyword.get('name'))
      return `
      <tr>
        <td><input type="checkbox" class="keyword-checkbox" data-term="${keyword.get('name')}" data-uri="${uri}" ${isChecked ? 'checked' : ''}></td>
        <td>${keyword.get('name')}</td>
        <td>${uri}</td>
        <td>${keyword.get('confidence')}</td>
      </tr>
    `
    }).join('')

    this.$('.keywords-table-body').html(rowsHTML)

    if (filteredKeywords.length > this.keywordsToShow) {
      this.$('.legilo-load-more-btn').show()
    } else {
      this.$('.legilo-load-more-btn').hide()
    }

    this.showTableAndButtons()
    this.$('.no-keywords-message').hide()
  },

  toggleKeywordSelection (event) {
    const keywordName = $(event.target).data('term')
    const keywordUri = $(event.target).data('uri') || ''
    if (event.target.checked) {
      this.selectedKeywords.push({ value: keywordName, uri: keywordUri })
    } else {
      this.selectedKeywords = this.selectedKeywords.filter(kw => kw.value !== keywordName)
    }
  },

  addSelectedKeywords () {
    this.selectedKeywords.forEach(keyword => {
      const newKeywordModel = new LegiloKeyword({
        value: keyword.value,
        uri: keyword.uri || null
      })

      this.collection.add(newKeywordModel)
    })

    this.selectedKeywords = []
    this.renderKeywords(this.keywords)
  },

  showTableAndButtons () {
    this.$('.keyword-table-header').show()
    this.$('.keywords-table').show()
    this.$('.keywords-buttons').show()
  },

  close () {
    this.$('.keywords-table').hide()
    this.$('.keywords-buttons').hide()
    this.$('.legilo-load-more-btn').hide()
    this.$('.keyword-table-header').hide()
    this.selectedKeywords = []
  },

  loadAllKeywords () {
    this.keywordsToShow = this.keywords.length
    this.renderKeywords(this.keywords)
  },

  showNoKeywordsMessage (message) {
    this.$('.no-keywords-message').text(message).show()
    this.$('.keywords-table').hide()
    this.$('.keywords-buttons').hide()
    this.$('.keyword-table-header').hide()
  }
})
