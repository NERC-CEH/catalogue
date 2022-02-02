import _ from 'underscore'
import { InputView } from './InputView'
import template from './Select.tpl'

export var SelectView = InputView.extend({

  optionTemplate: _.template('<option value="<%= value %>"><%= label %></option>'),

  initialize (options) {
    console.log('initialize select view')
    this.template = _.template(template)
    this.options = options.options
    return InputView.prototype.initialize.call(this, options)
  },

  render () {
    console.log('selectview render')
    InputView.prototype.render.apply(this)
    Array.prototype.forEach.call(this.options, option => {
      this.$('select').append(this.optionTemplate(option))
    })
    this.$('select').val(this.model.get(this.data.modelAttribute))
  }
})
