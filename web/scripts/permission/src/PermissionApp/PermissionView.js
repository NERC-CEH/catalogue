import Backbone from 'backbone'
import _ from 'underscore'
import template from './Permissions.tpl'
import { IdentityPermission, IdentityPermissionView } from '../IdentityPermission'

export default Backbone.View.extend({
  el: '.permission',

  events: {
    'click #permissionSave': 'save',
    'click #permissionAdd': 'add'
  },

  initialize () {
    this.template = _.template(template)
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
    const identity = $('#identity').val()
    if (identity) {
      const permission = new IdentityPermission({
        identity,
        canView: $('#canView').prop('checked'),
        canEdit: $('#canEdit').prop('checked'),
        canDelete: $('#canDelete').prop('checked'),
        canUpload: $('#canUpload').prop('checked')
      })
      this.model.addPermission(permission)
    }

    $('#identity').val('')
    $('#canView').prop('checked', false)
    $('#canEdit').prop('checked', false)
    $('#canDelete').prop('checked', false)
    $('#canUpload').prop('checked', false)
  },

  leave () {
    window.location.assign(`/documents/${this.model.get('id')}/permission`)
  }
})
