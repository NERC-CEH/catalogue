import template from '../templates/SaTissue'
import ObjectInputView from './ObjectInputView'

export default ObjectInputView.extend({
    initialize (options) {
        this.template = template
        ObjectInputView.prototype.initialize.call(this, options)
    },
    render () {
        ObjectInputView.prototype.render.apply(this)
        this.$('select').val(this.model.get('value'))
        return this
    }
})
