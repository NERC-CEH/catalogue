import Backbone from 'backbone'

export default Backbone.View.extend({

  initialize (options) {
    this.model = options.model
    this.ModelType = options.modelType
    this.collection = options.collection
    this.fetcher = options.fetcher
    this.fetchButton = options.fetchButton
    this.template = options.template
    this.onSelect = options.onSelect
    this.suggestionsToShow = options.suggestionsToShow || 10
    this.result = options.result
    this.selectedSuggestions = []
    this.suggestions = []
    this.suggestionsToDisplay = []

    this.fetchButton.on('click', () => this.fetch())

    this.result.append(`
      <div class="d-flex justify-content-center w-100">
        <span class="legilo-fetch-loader spinner-border text-secondary loader mx-3"
              role="status"
              style="display: none;"></span>
        <span class="legilo-fetch-loader-msg"></span>
      </div>
      <div class="legilo-result w-100"></div>
    `)
    this.fetchLoader = this.result.find('.legilo-fetch-loader')
    this.fetchLoaderMsg = this.result.find('.legilo-fetch-loader-msg')
    this.fetchResult = this.result.find('.legilo-result')
    this.setElement(this.fetchResult)
  },

  events: {
    'click .suggestions-close-btn': 'close',
    'click .suggestions-add-btn': 'addSelectedSuggestions',
    'change .suggestions-checkbox': 'toggleSuggestionsSelection',
    'click .suggestions-load-more-btn': 'loadAllSuggestions'
  },

  fetch () {
    this.fetchButton.prop('disabled', true)
    this.fetchLoader.show()
    this.fetchLoaderMsg
      .removeClass('text-primary')
      .text('It may take a while.')

    this.fetcher(this.model.id)
      .then(suggestions => {
        this.model.set(this.fetcher.name, suggestions)
        this.render()

        this.fetchLoader.hide()
        this.fetchLoaderMsg.text('')
        this.fetchButton.prop('disabled', false)
      })
      .catch(error => {
        console.error('Error fetching suggestions:', error)

        let errText = 'Something went wrong while fetching suggestions.'
        if (error.status === 500) {
          errText = 'Service not reachable, please try again later.'
        } else if (error.status === 422) {
          errText = 'No documents available for this dataset extraction.'
        } else if (error.status === 404) {
          errText = 'Suggestion service not available.'
        } else {
          if (error.responseJSON) {
            const response = error.responseJSON
            if (response.error) {
              errText = response.error
            } else if (response.message) {
              errText = response.message
            }
          }
        }
        this.fetchLoader.hide()
        this.fetchLoaderMsg
          .addClass('text-primary')
          .text(errText)
        this.fetchButton.prop('disabled', false)
      })
  },

  render () {
    this.suggestions = this.model.get(this.fetcher.name) || []
    const filteredSuggestions = this.suggestions.filter(suggestion => {
      return !this.collection.findWhere({ value: suggestion.get('name') })
    })
    this.suggestionsToDisplay = filteredSuggestions.slice(0, this.suggestionsToShow).map(suggestion => {
      return {
        isChecked: this.selectedSuggestions.some(selected => selected.value === suggestion.get('name')),
        ...suggestion.attributes
      }
    })
    this.$el.html(this.template({ suggestions: this.suggestionsToDisplay }))

    if (this.suggestions.length > 0 && filteredSuggestions.length === 0) {
      this.showNoSuggestedMessage('All suggestions are already present or have been added.')
      return
    }

    if (this.suggestions.length === 0) {
      this.showNoSuggestedMessage('No suggestions available.')
      return
    }

    if (filteredSuggestions.length > this.suggestionsToShow) {
      this.$('.suggestions-load-more-btn').show()
    } else {
      this.$('.suggestions-load-more-btn').hide()
    }

    this.$('.no-suggestions-message').hide()
    this.showTableAndButtons()

    return this
  },

  toggleSuggestionsSelection (event) {
    this.onSelect(event, this.selectedSuggestions)
  },

  addSelectedSuggestions () {
    this.selectedSuggestions.forEach(suggestion => this.collection.add(new this.ModelType(suggestion)))

    this.selectedSuggestions = []
    this.render()
  },

  showTableAndButtons () {
    this.$('.suggestions-table-header').show()
    this.$('.suggestions-table').show()
    this.$('.suggestions-buttons').show()
  },

  close () {
    this.$('.suggestions-table').hide()
    this.$('.suggestions-buttons').hide()
    this.$('.suggestions-load-more-btn').hide()
    this.$('.suggestions-table-header').hide()
    this.selectedSuggestions = []
  },

  loadAllSuggestions () {
    this.suggestionsToShow = this.suggestions.length
    this.render()
  },

  showNoSuggestedMessage (message) {
    this.$('.no-suggestions-message').text(message).show()
    this.$('.suggestions-table').hide()
    this.$('.suggestions-buttons').hide()
    this.$('.suggestions-table-header').hide()
  }
})
