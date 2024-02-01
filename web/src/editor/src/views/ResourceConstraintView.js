import ObjectInputView from './ObjectInputView'
import template from '../templates/ResourceConstraint'

export default ObjectInputView.extend({
    initialize (options) {
        this.template = template
        ObjectInputView.prototype.initialize.call(this, options)
    },
    render () {
        ObjectInputView.prototype.render.apply(this)
        this.$('select').val(this.model.get('code'))
        return this
    }
})
