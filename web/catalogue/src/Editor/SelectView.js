import _ from 'underscore'
import { InputView } from './InputView'
import template from './Select.tpl'

export var SelectView = InputView.extend({

  initialize (options) {
    console.log('initialize select view')
    this.template = _.template(template)
    this.optionTemplate = _.template('<option value="<%= value %>"><%= label %></option>')
    console.log('options.options')
    console.log(options.options)
    this.options = options.options
    InputView.prototype.initialize.call(this, options)
  },

  render () {
    console.log('selectview render')
    InputView.prototype.render.apply(this)
    this.options.forEach(option => {
      this.$('select').append(this.optionTemplate(option))
      console.log('option')
      console.log(option)
    })
    this.$('select').val(this.model.get(this.data.modelAttribute))
  }
})
