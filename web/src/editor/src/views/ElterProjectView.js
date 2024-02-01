import ObjectInputView from './ObjectInputView'
import template from '../templates/ElterProject'

export default ObjectInputView.extend({

    render () {
        this.template = template
        ObjectInputView.prototype.render.apply(this)
        this.$('select').val(this.model.get('value'))
        return this
    }
})
