import Backbone from 'backbone'
import $ from 'jquery'
import template from '../templates/LegiloVariables'

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
    'click .legilo-variable-close-btn': 'close',
    'click .legilo-variable-add-btn': 'addSelectedVariables',
    'change .variable-checkbox': 'toggleVariableSelection',
    'click .legilo-variable-load-more-btn': 'loadAllVariables'
  },

  render () {
    this.$el.html(this.template())
    return this
  },

  renderVariables (variables) {
    this.variables = variables

    const filteredVariables = this.variables.filter(variable => {
      return !this.collection.findWhere({ value: variable.get('name') })
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

    const rowsHTML = variablesToDisplay.map(variable => {
      const name = variable.get('name')
      const title = variable.get('longName')
      const units = variable.get('units')
      const description = variable.get('meaning')
      const confidence = variable.get('confidence')

      const isChecked = this.selectedVariables.some(selected => selected.value === name)
      return `
      <tr>
        <td><input type="checkbox" class="variable-checkbox" data-value="${name}" data-title="${title}" data-units="${units}" data-description="${description}" ${isChecked ? 'checked' : ''}></td>
        <td>${name}</td>
        <td>${title}</td>
        <td>${units}</td>
        <td>${description}</td>
        <td>${confidence}</td>
      </tr>
    `
    }).join('')

    this.$('.variables-table-body').html(rowsHTML)

    if (filteredVariables.length > this.variablesToShow) {
      this.$('.legilo-variable-load-more-btn').show()
    } else {
      this.$('.legilo-variable-load-more-btn').hide()
    }

    this.showTableAndButtons()
    this.$('.no-variables-message').hide()
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
    this.selectedVariables.forEach(variable => this.collection.add(new this.ModelType(variable)))

    this.selectedVariables = []
    this.renderVariables(this.variables)
  },

  showTableAndButtons () {
    this.$('.variable-table-header').show()
    this.$('.variables-table').show()
    this.$('.variables-buttons').show()
  },

  close () {
    this.$('.variables-table').hide()
    this.$('.variables-buttons').hide()
    this.$('.legilo-variable-load-more-btn').hide()
    this.$('.variable-table-header').hide()
    this.selectedVariables = []
  },

  loadAllVariables () {
    this.variablesToShow = this.variables.length
    this.renderVariables(this.variables)
  },

  showNoVariablesMessage (message) {
    this.$('.no-variables-message').text(message).show()
    this.$('.variables-table').hide()
    this.$('.variables-buttons').hide()
    this.$('.variable-table-header').hide()
  }
})
