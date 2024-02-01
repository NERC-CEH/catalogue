import Backbone from 'backbone'

export default Backbone.Model.extend({

    url () {
        return this.urlRoot()
    },

    urlRoot () {
        return `/documents/${this.id}/catalogue`
    }
})
