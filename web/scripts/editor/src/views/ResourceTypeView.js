import ObjectInputView from './ObjectInputView'
import template from '../templates/ResourceType.tpl'
import _ from 'underscore'

export default ObjectInputView.extend({

  initialize () {
    this.template = _.template(template)
    ObjectInputView.prototype.initialize.apply(this)
    this.listenTo(this.model, 'change:resourceType', function (model, value) {
      this.model.set('type', value.value)
    })
  },

  render () {
    ObjectInputView.prototype.render.apply(this)
    this.$('select').val(this.model.get('value'))
    return this
  }
})
