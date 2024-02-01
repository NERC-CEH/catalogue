import Backbone from 'backbone'
import _ from 'underscore'
import Permission from './Permission'

export default Backbone.Model.extend({

    loadPermission (identifier) {
        const permission = new Permission({
            id: identifier
        })

        return permission.fetch({
            success: model => {
                model.loadCollection()
                this.set('permission', model)
                this.trigger('loaded')
            },

            error: model => {
                this.trigger('error', `Unable to load permission for: ${model.id}`)
            }
        })
    },

    getPermission () {
        return _.clone(this.get('permission'))
    }
})
