import _ from 'underscore'
import { InputView } from './InputView'
import template from './Select.tpl'

export var SelectView = InputView.extend({

  optionTemplate: _.template('<option value="<%= value %>"><%= label %></option>'),

  initialize (options) {
    this.template = _.template(template)
    this.options = options.options
    return InputView.prototype.initialize.call(this, options)
  },

  render () {
    InputView.prototype.render.apply(this)
    const $select = this.$('select')
    this.options.forEach(option => {
      $select.append(this.optionTemplate(option))
    })
    $select.val(this.model.get(this.data.modelAttribute))
  }
})
