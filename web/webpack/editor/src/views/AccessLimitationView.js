import ObjectInputView from './ObjectInputView'
import template from '../templates/AccessLimitation.tpl'
import _ from 'underscore'

export default ObjectInputView.extend({

    initialize () {
      this.template = _.template(template)
      ObjectInputView.prototype.initialize.apply(this)
      return this.listenTo(this.model, 'change:accessLimitation', function (model, value) {
        return this.model.set('type', value.value)
      })
    },

    render () {
      ObjectInputView.prototype.render.apply(this)
      this.$('select').val(this.model.get('value'))
      return this
    }
})
