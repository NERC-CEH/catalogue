import Backbone from 'backbone'
import _ from 'underscore'
import template from './PermissionsTemplate'
import { IdentityPermission, IdentityPermissionView } from '../IdentityPermission'

export default Backbone.View.extend({
  el: '.permission',

  events: {
    'click #permissionSave': 'save',
    'click #permissionAdd': 'add'
  },

  initialize () {
    this.template = template
    this.listenTo(this.model, 'sync', this.reload)
    this.listenTo(this.model, 'permission:add', this.addAll)
    this.listenTo(this.model, 'permission:remove', this.addAll)
    this.listenTo(this.model, 'save:success', this.leave)
  },

  save () {
    this.model.save({}, {
      success: () => {
        this.model.trigger('save:success', 'Save successful')
      },
      error: (model, response) => {
        this.model.trigger('save:error', `Error saving permission: ${response.status} (${response.statusText})`)
      }
    }
    )
  },

  addOne (permission) {
    _.extend(permission, { parent: this.model })
    const view = new IdentityPermissionView({ model: permission })
    this.$('tbody').append(view.render().el)
  },

  addAll () {
    this.$('tbody').html('')
    this.model.get('permissions').each(this.addOne, this)
  },

  render () {
    this.$el.html(this.template(this.model.attributes))
    this.addAll()
    return this
  },

  reload () {
    this.model.loadCollection()
    this.addAll()
  },

  add () {
    const identity = this.$('#identity').val()
    if (identity) {
      const permission = new IdentityPermission({
        identity,
        canView: this.$('#canView').prop('checked'),
        canEdit: this.$('#canEdit').prop('checked'),
        canDelete: this.$('#canDelete').prop('checked'),
        canUpload: this.$('#canUpload').prop('checked')
      })
      this.model.addPermission(permission)
    }

    this.$('#identity').val('')
    this.$('#canView').prop('checked', false)
    this.$('#canEdit').prop('checked', false)
    this.$('#canDelete').prop('checked', false)
    this.$('#canUpload').prop('checked', false)
  },

  leave () {
    window.location.assign(`/documents/${this.model.get('id')}/permission`)
  }
})
