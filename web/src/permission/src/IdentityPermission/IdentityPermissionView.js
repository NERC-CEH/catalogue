import _ from 'underscore'
import $ from 'jquery'
import Backbone from 'backbone'

const template = _.template(`
<td><%= identity %></td>
<td><input data-permission="canView" type="checkbox" <% if(canView) { %>checked<% } %>></td>
<td><input data-permission="canEdit" type="checkbox" <% if(canEdit) { %>checked<% } %>></td>
<td><input data-permission="canDelete" type="checkbox" <% if(canDelete) { %>checked<% } %>></td>
<td><input data-permission="canUpload" type="checkbox" <% if(canUpload) { %>checked<% } %>></td>
<td><button class="editor-button-xs"><i class="fa-solid fa-times"></i></button></td>
`)

export default Backbone.View.extend({
    tagName: 'tr',

    events: {
        'click button': 'removePermission',
        'click [type="checkbox"]': 'update'
    },

    removePermission () {
        this.model.parent.removePermission(this.model)
    },

    update (event) {
        const permission = $(event.target).data('permission')
        this.model.set(permission, !this.model.get(permission))
    },

    render () {
        this.$el.html(template(this.model.attributes))
        return this
    }
})
