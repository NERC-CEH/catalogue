import { ObjectInputView } from '../index'
import template from '../../templates/service-agreement/rightsHolder'

export default ObjectInputView.extend({

    initialize (options) {
        this.template = template
        ObjectInputView.prototype.initialize.call(this, options)
    },

    modify (event) {
        ObjectInputView.prototype.modify.call(this, event)
        this.model.set('role', 'rightsHolder')
    }
})
