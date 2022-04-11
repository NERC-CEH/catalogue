import _ from 'underscore'
import $ from 'jquery'
import Backbone from 'backbone'
import InputView from '../InputView'
import LinkDocumentView from './LinkDocumentView'
import template from '../templates/LinkDocumentSelector.tpl'

export default InputView.extend({

  events () {
    return _.extend({}, InputView.prototype.events, {
      'keyup #term' () { return this.searchOnceComplete() },
      'change #term' () { return this.search() },
      'change #catalogue' () { return this.search() },
      'click button' () { return this.search() }
    }
    )
  },

  template,

  optionTemplate: _.template('<option value="<%= id %>" <% if (id === data.catalogue) { %>selected<% } %>><%= title %></option>'),

  initialize (options) {
    this.template = _.template(template)
    let params
    if (this.model.isNew()) {
      params = `catalogue=${Backbone.history.location.pathname.split('/')[1]}`
    } else {
      params = `identifier=${this.model.get('id')}`
    }

    options.catalogue = 'eidc'

    this.searchOnceComplete = _.debounce(this.search, 500)
    this.results = new Backbone.Collection()

    $.getJSON(`/catalogues?${params}`, catalogues => {
      this.catalogues = catalogues
      return InputView.prototype.initialize.call(this, options)
    })

    this.listenTo(this.results, 'selected', this.setSelected)
    return this.listenTo(this.results, 'reset', this.addAll)
  },

  render () {
    InputView.prototype.render.apply(this)
    const $select = this.$('#catalogue')
    _.each(this.catalogues, catalogue => {
      return $select.append(this.optionTemplate(_.extend({}, catalogue, { data: this.data })))
    })
    this.search()
    return this
  },

  search () {
    let searchUrl
    this.data.catalogue = this.$('#catalogue').val()
    this.data.term = this.$('#term').val()

    if (this.data.term.length > 0) {
      searchUrl = `/${this.data.catalogue}/documents?term=state:published AND view:public AND NOT documentType:LINK_DOCUMENT AND ${this.data.term}`
    } else {
      searchUrl = `/${this.data.catalogue}/documents?term=state:published AND view:public AND NOT documentType:LINK_DOCUMENT`
    }

    return $.getJSON(searchUrl, data => {
      return this.results.reset(data.results)
    })
  },

  addOne (result) {
    const view = new LinkDocumentView({ model: result })
    return this.$('#results').append(view.render().el)
  },

  addAll () {
    this.$('#results').html('')
    return this.results.each(this.addOne, this)
  },

  setSelected (identifier) {
    return this.model.set(this.data.modelAttribute, identifier)
  }
})
