import _ from 'underscore'
import Backbone from 'backbone'

export default Backbone.Model.extend({

    validate (attrs) {
        const errors = []

        const {
            name
        } = attrs
        const {
            type
        } = attrs
        const {
            version
        } = attrs

        if (!version && (name || type)) {
            errors.push({ message: "The version is mandatory - if it's not applicable, enter 'unknown'" })
        }

        if (_.isEmpty(errors)) {
            // return nothing from Backbone.Model.validate
            // because returning something signals a validation error.

        } else {
            return errors
        }
    }
})
