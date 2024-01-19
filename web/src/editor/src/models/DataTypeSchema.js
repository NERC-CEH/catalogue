import Backbone from 'backbone'
import _ from 'underscore'

export default Backbone.Model.extend({

    defaults: {
        constraints: {}
    },

    toJSON () {
        if (_.isEmpty(this.get('constraints'))) {
            return _.omit(this.attributes, 'constraints')
        } else {
            return _.clone(this.attributes)
        }
    }
})
