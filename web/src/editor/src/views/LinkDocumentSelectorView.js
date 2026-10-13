import _ from 'underscore'
import $ from 'jquery'
import Backbone from 'backbone'
import InputView from '../InputView'
import LinkDocumentView from './LinkDocumentView'
import template from '../templates/LinkDocumentSelector'

export default InputView.extend({

  // The template has a search box and a catalogue picker rather than one input-<modelAttribute>
  labelsControl: false,

  events: function () {
    return _.extend({}, InputView.prototype.events, {
      'keyup #term': 'searchSoon',
      'change #term': 'search',
      'change #catalogue': 'search',
      'click button': 'search'
    })
  },

  optionTemplate: _.template('<option value="<%- id %>" <% if (id === data.catalogue) { %>selected<% } %>><%- title %></option>'),

  initialize (options) {
    this.template = template
    this.data = options
    let params
    if (this.model.isNew()) {
      params = `catalogue=${Backbone.history.location.pathname.split('/')[1]}`
    } else {
      params = `identifier=${this.model.get('id')}`
    }

    options.catalogue = 'eidc'

    this.results = new Backbone.Collection()

    $.getJSON(`/catalogues?${params}`, catalogues => {
      this.catalogues = catalogues
      InputView.prototype.initialize.call(this, options)
    })

    this.listenTo(this.results, 'selected', this.setSelected)
    this.listenTo(this.results, 'reset', this.addAll)
  },

  render () {
    InputView.prototype.render.apply(this)
    _.each(this.catalogues, catalogue => {
      this.$('#catalogue').append(this.optionTemplate(_.extend({}, catalogue, { data: this.data })))
    })
    this.search()
    return this
  },

  // Typing searches once it pauses. Created here rather than in initialize, because Backbone binds
  // the events (by method name) before initialize runs
  searchSoon () {
    this.debouncedSearch = this.debouncedSearch || _.debounce(() => this.search(), 500)
    this.debouncedSearch()
  },

  search () {
    if (this.debouncedSearch) {
      this.debouncedSearch.cancel()
    }
    this.data.catalogue = this.$('#catalogue').val()
    this.data.term = this.$('#term').val()

    const clauses = ['state:published', 'view:public', 'NOT documentType:LINK_DOCUMENT']
    if (this.data.term.length > 0 && this.data.catalogue) {
      clauses.push(`(${this.data.term})`)
    }

    // Only the latest search may show its results; an earlier one can answer after it
    const search = this.latestSearch = (this.latestSearch || 0) + 1
    $.getJSON(`/${this.data.catalogue}/documents?term=${encodeURIComponent(clauses.join(' AND '))}`)
      .done(data => {
        if (search === this.latestSearch) {
          this.results.reset(data.results)
        }
      })
  },

  addOne (result) {
    const view = new LinkDocumentView({ model: result })
    this.$('#results').append(view.render().el)
  },

  addAll () {
    this.$('#results').html('')
    this.results.each(this.addOne, this)
  },

  setSelected (identifier) {
    this.model.set(this.data.modelAttribute, identifier)
  }
})
