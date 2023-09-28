import _ from 'underscore'
import ParentView from './ParentView'
import template from '../templates/PredefinedParent'
import dropdownTemplate from '../templates/PredefinedParentDropdown'
import $ from 'jquery'
import 'jquery-ui'
export default ParentView.extend({

  events: {
    'click .dropdown-menu': 'setPredefined'
  },

  render () {
    this.predefinedTemplate = template
    this.dropdownTemplate = dropdownTemplate
    ParentView.prototype.render.apply(this)
    this.$('button.add').replaceWith(this.predefinedTemplate({ data: this.data }))
    this.$('button').prop(this.data.disabled, this.data.disabled)
    _.chain(this.data.predefined)
      .keys()
      .each(item => this.$('ul.dropdown-menu').append(this.dropdownTemplate({ predefined: item })))
    return this
  },

  setPredefined (event) {
    event.preventDefault()
    const value = $(event.target).text()
    let selected = {}

    if (value !== 'Custom') {
      selected = this.data.predefined[value]
    }

    this.collection.add(selected)
  }
})
