import _ from 'underscore'
import SingleObjectView from './SingleObjectView'
import predefinedTemplate from '../templates/PredefinedParent'
import dropdownTemplate from '../templates/PredefinedParentDropdown'
import $ from 'jquery'

export default SingleObjectView.extend({

  events: {
    'click .dropdown-menu': 'setPredefined'
  },

  initialize (options) {
    SingleObjectView.prototype.initialize.call(this, options)
  },

  render () {
    this.predefinedTemplate = predefinedTemplate
    this.dropdownTemplate = dropdownTemplate
    SingleObjectView.prototype.render.apply(this)
    this.$('#template-add-on').replaceWith(this.predefinedTemplate({ data: this.data }))
    this.$('.editor-button').html('Set <span class="caret"></span>')
    _.chain(this.data.predefined)
      .keys()
      .each(item => this.$('ul.dropdown-menu').append(this.dropdownTemplate({ predefined: item })))
    return this
  },

  setPredefined (event) {
    event.preventDefault()
    const value = $(event.target).text()

    if (value !== 'Custom') {
      this.inputModel.set(this.data.predefined[value])
    } else {
      this.inputModel.clear().set(this.inputModel.defaults)
    }
    this.objectInputView.render()
  }
})
