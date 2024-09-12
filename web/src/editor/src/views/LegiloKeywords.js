import Backbone from 'backbone'
import $ from 'jquery'
import template from '../templates/LegiloKeywords'
import { KeywordModel } from '../models'

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
    'click .legilo-fetch-btn': 'fetchKeywordsFromLegilo',
    'click .legilo-cancel-btn': 'cancel',
    'click .legilo-add-btn': 'addSelectedKeywords',
    'change .keyword-checkbox': 'toggleKeywordSelection',
    'click .legilo-load-more-btn': 'loadAllKeywords'
  },

  render () {
    this.$el.html(this.template())

    return this
  },

  fetchKeywordsFromLegilo () {
    const datasetId = this.model.get('id')
    if (!datasetId) {
      console.error('No dataset ID found')
      return
    }

    // const apiUrl = `/api/datasets/${datasetId}`

    this.showLoader()

    const mockData = {
      summary: [
        { name: 'temperature', uri: 'custom-uri1', confidence: 0.63 },
        { name: 'trophic', uri: '', confidence: 0.35 },
        { name: 'transect', uri: 'custom-uri2', confidence: 0.25 },
        { name: 'pressure', uri: 'custom-uri3', confidence: 0.45 },
        { name: 'salinity', uri: '', confidence: 0.50 },
        { name: 'depth', uri: 'custom-uri4', confidence: 0.30 },
        { name: 'oxygen', uri: '', confidence: 0.55 },
        { name: 'chlorophyll', uri: 'custom-uri5', confidence: 0.60 },
        { name: 'phosphate', uri: '', confidence: 0.20 },
        { name: 'silicate', uri: 'custom-uri6', confidence: 0.15 },
        { name: 'nitrate', uri: 'custom-uri7', confidence: 0.40 }
      ]
    }

    return new Promise((resolve) => {
      setTimeout(() => {
        this.keywords = mockData.summary.map(keywordData => new KeywordModel(keywordData))
        this.renderKeywords(true)
        this.showTableAndButtons()
        this.hideFetchButton()
        this.hideLoader()
        resolve(mockData)
      }, 1000)
    }).catch(error => {
      console.error('Error fetching keywords:', error)
      this.hideLoader()
    })
  },

  renderKeywords (initialLoad = false) {
    const filteredKeywords = this.keywords.filter(keyword => {
      return !this.collection.findWhere({ value: keyword.get('name') })
    })

    if (filteredKeywords.length === 0) {
      this.showNoKeywordsMessage()
      return
    }

    let keywordsToDisplay = filteredKeywords

    if (initialLoad) {
      keywordsToDisplay = filteredKeywords.slice(0, this.keywordsToShow)
    }

    const rowsHTML = keywordsToDisplay.map(keyword => {
      const uri = keyword.get('uri') || ''
      return `
          <tr>
            <td><input type="checkbox" class="keyword-checkbox" data-term="${keyword.get('name')}" data-uri="${uri}""></td>
            <td>${keyword.get('name')}</td>
            <td>${uri}</td>
            <td>${keyword.get('confidence')}</td>
          </tr>
        `
    }).join('')

    this.$('.keywords-table-body').html(rowsHTML)

    if (initialLoad && filteredKeywords.length > this.keywordsToShow) {
      this.$('.load-more-btn').show()
    } else {
      this.$('.load-more-btn').hide()
    }
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
      const newKeywordModel = new KeywordModel({
        value: keyword.value,
        uri: keyword.uri || null
      })

      this.collection.add(newKeywordModel)
    })

    this.selectedKeywords = []
    this.renderKeywords(true)

    if (this.keywords.length === 0) {
      this.showNoKeywordsMessage()
    }
  },

  showTableAndButtons () {
    this.$('.keywords-table').show()
    this.$('.keywords-buttons').show()
  },

  cancel () {
    this.$('.keywords-table').hide()
    this.$('.keywords-buttons').hide()
    this.selectedKeywords = []
    this.showFetchButton()
  },

  loadAllKeywords () {
    this.renderKeywords(false)
  },

  hideFetchButton () {
    this.$('.legilo-fetch-btn').hide()
  },

  showFetchButton () {
    this.$('.legilo-fetch-btn').show()
  },

  showLoader () {
    this.$('.loader').show()
  },

  hideLoader () {
    this.$('.loader').hide()
  },

  showNoKeywordsMessage () {
    this.$('.keywords-table-body').html('<tr><td colspan="4">No keywords available</td></tr>')
  }
})
