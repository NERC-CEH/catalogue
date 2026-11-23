import $ from 'jquery'
import Backbone from 'backbone'
import _ from 'underscore'

export default Backbone.View.extend({
  template: _.template(`
    <tr>
      <td><%- document %></td>
      <td><%- docTitle %></td>
      <td><%- recordType %></td>
      <td><%- views %></td>
      <td><%- downloads %></td>
    </tr>
  `),

  initialize () {
    this.model.set({
      metricsReport: Array.from(this.$('tbody tr')).filter(tr => tr.querySelectorAll('td').length === 5).map(tr => {
        const [document, docTitle, recordType, views, downloads] = Array.from(tr.querySelectorAll('td'), td => td.innerText)
        return { document, docTitle, recordType, views, downloads }
      })
    }, { silent: true })

    this.listenTo(this.model, 'sync', this.render)
    // A failed report replaces the old rows, which no longer match the filters (dri-one #461)
    this.listenTo(this.model, 'error', this.renderError)
  },

  renderError (model, xhr) {
    const reason = (xhr && (xhr.responseJSON?.detail || xhr.responseJSON?.message || xhr.responseText)) || 'the server did not respond'
    this.$('tbody').empty().append($('<tr>').append($('<td colspan="5" class="text-danger">').text(`The report could not be produced: ${reason}`)))
  },

  render () {
    const report = this.model.get('metricsReport')
    if (!report) {
      return this
    }
    const body = this.$('tbody')
    body.empty()
    if (report.length === 0) {
      body.append($('<tr>').append($('<td colspan="5" class="text-body-secondary metrics-no-records">').text('No records match these filters.')))
      return this
    }
    report.forEach(record => body.append(this.template(record)))
    return this
  }
})
