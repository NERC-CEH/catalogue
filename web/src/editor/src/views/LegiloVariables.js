import Backbone from 'backbone'
import $ from 'jquery'
import template from '../templates/LegiloKeywords'

export default Backbone.View.extend({

  initialize (options) {
    this.model = options.model
    this.ModelType = options.modelType
    this.collection = options.collection
    this.template = template
    this.selectedVariables = []
    this.variables = []
    this.variablesToShow = 10
  },

  events: {
    'click .legilo-close-btn': 'close',
    'click .legilo-add-btn': 'addSelectedVariables',
    'change .variable-checkbox': 'toggleVariableSelection',
    'click .legilo-load-more-btn': 'loadAllVariables'
  },

  render () {
    this.$el.html(this.template())
    return this
  },

  renderVariables (variables) {
    this.variables = variables

    const filteredVariables = this.variables.filter(keyword => {
      return !this.collection.findWhere({ value: keyword.get('name') })
    })

    if (this.variables.length > 0 && filteredVariables.length === 0) {
      this.showNoVariablesMessage('All suggested variables are already present or have been added.')
      return
    }

    if (variables.length === 0) {
      this.showNoVariablesMessage('No suggested variables available.')
      return
    }

    const variablesToDisplay = filteredVariables.slice(0, this.variablesToShow)

    const rowsHTML = variablesToDisplay.map(keyword => {
      const name = keyword.get('name')
      const title = keyword.get('standardName')
      const units = keyword.get('units')
      const description = keyword.get('longName')
      const isChecked = this.selectedVariables.some(selected => selected.value === name)
      return `
      <tr>
        <td><input type="checkbox" class="variable-checkbox" data-value="${name}" data-title="${title}" data-units="${units}" data-description="${description}" ${isChecked ? 'checked' : ''}></td>
        <td>${name}</td>
        <td>${title}</td>
        <td>${units}</td>
        <td>${description}</td>
      </tr>
    `
    }).join('')

    const tableHead = `
      <tr>
        <th scope="col" class="col-1">Select</th>
        <th scope="col">Name</th>
        <th scope="col">Title</th>
        <th scope="col">Unit</th>
        <th scope="col">Description</th>
      </tr>
    `
    this.$('.keywords-table-head').html(tableHead)
    this.$('.keywords-table-body').html(rowsHTML)

    if (filteredVariables.length > this.variablesToShow) {
      this.$('.legilo-load-more-btn').show()
    } else {
      this.$('.legilo-load-more-btn').hide()
    }

    this.showTableAndButtons()
    this.$('.no-keywords-message').hide()
  },

  toggleVariableSelection (event) {
    const value = $(event.target).data('value')
    const title = $(event.target).data('title')
    const units = $(event.target).data('units')
    const description = $(event.target).data('description')
    if (event.target.checked) {
      this.selectedVariables.push({ value, title, units, description })
    } else {
      this.selectedVariables = this.selectedVariables.filter(kw => kw.value !== value)
    }
  },

  addSelectedVariables () {
    this.selectedVariables.forEach(keyword => this.collection.add(new this.ModelType(keyword)))

    this.selectedVariables = []
    this.renderVariables(this.variables)
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
    this.selectedVariables = []
  },

  loadAllVariables () {
    this.variablesToShow = this.variables.length
    this.renderVariables(this.variables)
  },

  showNoVariablesMessage (message) {
    this.$('.no-keywords-message').text(message).show()
    this.$('.keywords-table').hide()
    this.$('.keywords-buttons').hide()
    this.$('.keyword-table-header').hide()
  }
})
