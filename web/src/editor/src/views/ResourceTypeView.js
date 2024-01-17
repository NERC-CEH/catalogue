import ObjectInputView from './ObjectInputView'
import template from '../templates/ResourceType'

export default ObjectInputView.extend({

    initialize (options) {
        this.template = template
        ObjectInputView.prototype.initialize.call(this, options)
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
