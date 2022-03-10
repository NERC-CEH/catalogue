import _ from 'underscore'
import template from '../templates/Relationship.tpl'
import ObjectInputView from './ObjectInputView'

export default ObjectInputView.extend({

  optionTemplate: _.template('<option value="<%= value %>"><%= label %></option>'),

  initialize (options) {
    this.template = _.template(template)
    this.options = options.options
    return ObjectInputView.prototype.initialize.call(this, options)
  },

  render () {
    ObjectInputView.prototype.render.apply(this)
    const $list = this.$('datalist')
    this.options.forEach(option => {
      return $list.append(this.optionTemplate(option))
    })
    return this
  }
})
