import Backbone from 'backbone'
export default Backbone.Model.extend({

    defaults: {
        toDelete: false
    },

    idAttribute: 'name'
})
