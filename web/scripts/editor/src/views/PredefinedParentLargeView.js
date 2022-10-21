import _ from 'underscore'
import ParentLargeView from './ParentLargeView'
import template from '../templates/PredefinedParent.tpl'
import dropdownTemplate from '../templates/PredefinedParentDropdown.tpl'
import 'jquery-ui'

export default ParentLargeView.extend({

  events: {
    'click .dropdown-menu': 'setPredefined'
  },

  render () {
    this.predefinedTemplate = _.template(template)
    this.dropdownTemplate = _.template(dropdownTemplate)
    ParentLargeView.prototype.render.apply(this)
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
