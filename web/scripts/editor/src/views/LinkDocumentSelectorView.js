import _ from 'underscore'
import Backbone from 'backbone'
import InputView from '../InputView'
import LinkDocumentView from './LinkDocumentView'
import template from '../templates/LinkDocumentSelector.tpl'

export default InputView.extend({

  events: function () {
    return _.extend({}, InputView.prototype.events, {
      'keyup #term': 'search',
      'change #term': 'search',
      'change #catalogue': 'search',
      'click button': 'search'
    })
  },

  optionTemplate: _.template('<option value="<%= id %>" <% if (id === data.catalogue) { %>selected<% } %>><%= title %></option>'),

  initialize (options) {
    this.template = _.template(template)
    this.data = options
    let params
    if (this.model.isNew()) {
      params = `catalogue=${Backbone.history.location.pathname.split('/')[1]}`
    } else {
      params = `identifier=${this.model.get('id')}`
    }

    options.catalogue = 'eidc'

    // this.searchOnceComplete = _.debounce(this.search, 500)
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

  search () {
    let searchUrl
    this.data.catalogue = this.$('#catalogue').val()
    this.data.term = this.$('#term').val()

    if (this.data.term.length > 0 && this.data.catalogue) {
      searchUrl = `/${this.data.catalogue}/documents?term=state:published AND view:public AND NOT documentType:LINK_DOCUMENT AND ${this.data.term}`
    } else {
      searchUrl = `/${this.data.catalogue}/documents?term=state:published AND view:public AND NOT documentType:LINK_DOCUMENT`
    }

    $.getJSON(searchUrl, data => {
      this.results.reset(data.results)
    })
  },

  addOne (result) {
    const view = new LinkDocumentView({ model: result })
    const that = this
    $(document).ready(function () {
      that.$('#results').append(view.render().el)
    })
  },

  addAll () {
    this.$('#results').html('')
    this.results.each(this.addOne, this)
  },

  setSelected (identifier) {
    this.model.set(this.data.modelAttribute, identifier)
  }
})
