import _ from 'underscore'
import Backbone from 'backbone'
export default Backbone.Model.extend({

    validate (attrs) {
        const errors = []

        const {
            equivalentScale
        } = attrs
        const {
            distance
        } = attrs

        if (equivalentScale && distance) {
            errors.push({ message: 'You can <b>EITHER</b> enter an Equivalent scale <b>OR</b> a Distance but not both.' })
        }

        if (_.isEmpty(errors)) {
            // return nothing from Backbone.Model.validate
            // because returning something signals a validation error.

        } else {
            return errors
        }
    }
})
