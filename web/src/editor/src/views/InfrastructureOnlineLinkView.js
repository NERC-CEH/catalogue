import template from '../templates/InfrastructureOnlineLink'
import ObjectInputView from './ObjectInputView'

export default ObjectInputView.extend({

    render () {
        this.template = template
        ObjectInputView.prototype.render.apply(this)
        this.$('select.function').val(this.model.get('function'))
        return this
    }
})
