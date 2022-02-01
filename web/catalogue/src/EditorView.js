/* eslint-disable
    no-return-assign,
    no-undef,
*/
// TODO: This file was created by bulk-decaffeinate.
// Fix any style issues and re-enable lint.
/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
define([
  'underscore',
  'jquery',
  'backbone',
  'tpl!templates/Editor.tpl'
], function (_, $, Backbone, template) {
  return Backbone.View.extend({

    events: {
      'click #editorDelete': 'attemptDelete',
      'click #confirmDeleteYes': 'delete',
      'click #editorExit': 'attemptExit',
      'click #exitWithoutSaving': 'exit',
      'click #editorSave': 'save',
      'click #editorBack': 'back',
      'click #editorNext': 'next',
      'click #editorNav li': 'direct'
    },

    initialize () {
      this.currentStep = 1
      this.saveRequired = false
      this.catalogue = $('html').data('catalogue')

      this.listenTo(this.model, 'error', function (model, response) {
        this.$('#editorAjax').toggleClass('visible')
        return this.$('#editorErrorMessage')
          .find('#editorErrorMessageResponse').text(`${response.status} ${response.statusText}`)
          .end()
          .find('#editorErrorMessageJson').text(JSON.stringify(model.toJSON()))
          .end()
          .modal('show')
      })
      this.listenTo(this.model, 'sync', function () {
        this.$('#editorAjax').toggleClass('visible')
        return this.saveRequired = false
      })
      this.listenTo(this.model, 'change save:required', function () {
        return this.saveRequired = true
      })
      this.listenTo(this.model, 'request', function () {
        return this.$('#editorAjax').toggleClass('visible')
      })
      this.listenTo(this.model, 'invalid', function (model, errors) {
        const $modalBody = this.$('#editorValidationMessage .modal-body')
        $modalBody.html('')
        _.each(errors, function (error) {
          return $modalBody.append(this.$(`<p>${error}</p>`))
        })
        return this.$('#editorValidationMessage').modal('show')
      });

      (this.render)()
      _.invoke(this.sections[0].views, 'show')
      const $editorNav = this.$('#editorNav')
      _.each(this.sections, function (section) {
        return $editorNav.append(this.$(`<li title='${section.title}'>${section.label}</li>`))
      })

      return $editorNav.find('li').first().addClass('active')
    },

    attemptDelete () {
      return this.$('#confirmDelete').modal('show')
    },

    delete () {
      this.$('#confirmDelete').modal('hide')
      return this.model.destroy({
        success: () => {
          _.invoke(this.sections, 'remove');
          (this.remove)()
          return Backbone.history.location.replace(`/${this.catalogue}/documents`)
        }
      })
    },

    save () {
      return (this.model.save)()
    },

    attemptExit () {
      if (this.saveRequired) {
        return this.$('#confirmExit').modal('show')
      } else {
        return (this.exit)()
      }
    },

    exit () {
      this.$('#confirmExit').modal('hide')
      _.invoke(this.sections, 'remove');
      (this.remove)()

      if ((Backbone.history.location.pathname === `/${this.catalogue}/documents`) && !this.model.isNew()) {
        return Backbone.history.location.replace(`/documents/${this.model.get('id')}`)
      } else {
        return (Backbone.history.location.reload)()
      }
    },

    back () {
      return this.navigate(this.currentStep - 1)
    },

    next () {
      return this.navigate(this.currentStep + 1)
    },

    direct (event) {
      let node = event.currentTarget
      let step = 0
      while (node !== null) {
        step++
        node = node.previousElementSibling
      }

      return this.navigate(step)
    },

    navigate (newStep) {
      const $nav = this.$('#editorNav li')
      const maxStep = $nav.length
      this.currentStep = newStep
      if (this.currentStep < 1) { this.currentStep = 1 }
      if (this.currentStep > maxStep) { this.currentStep = maxStep }

      const $back = this.$('#editorBack')
      if (this.currentStep === 1) {
        $back.prop('disabled', true)
      } else {
        $back.prop('disabled', false)
      }

      const $next = this.$('#editorNext')
      if (this.currentStep === maxStep) {
        $next.prop('disabled', true)
      } else {
        $next.prop('disabled', false)
      }

      $nav.filter('.active').toggleClass('active')
      this.$($nav[this.currentStep - 1]).toggleClass('active')

      return _.each(this.sections, (section, index) => {
        let method = 'hide'
        if ((this.currentStep - 1) === index) {
          method = 'show'
        }
        return _.invoke(section.views, method)
      })
    },

    render () {
      this.$el.html(template)
      _.each(this.sections, section => _.each(section.views, function (view) {
        return this.$('#editor').append(view.el)
      }))
      return this
    }
  })
})
