import _ from 'underscore'
import ParentLargeView from './ParentLargeView'
import template from '../templates/PredefinedParent.tpl'
import dropdownTemplate from '../templates/PredefinedParentDropdown.tpl'
import $ from 'jquery'

export default ParentLargeView.extend({

  events: {
    'click .dropdown-menu': 'setPredefined'
  },

  render () {
    ParentLargeView.prototype.render.apply(this)
    this.$('button.add').replaceWith(template({ data: this.data }))
    this.$('button').prop(this.data.disabled, this.data.disabled)
    const $dropdown = this.$('ul.dropdown-menu')
    _.chain(this.data.predefined)
      .keys()
      .each(item => $dropdown.append(dropdownTemplate({ predefined: item })))
    return this
  },

  setPredefined (event) {
    (event.preventDefault)()
    const value = $(event.target).text()
    let selected = {}

    if (value !== 'Custom') {
      selected = this.data.predefined[value]
    }

    return this.collection.add(selected)
  }
})
