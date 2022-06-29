import $ from 'jquery'
import _ from 'underscore'
import Backbone from 'backbone'
import template from './Editor.tpl'

export default Backbone.View.extend({

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
    if (typeof this.template === 'undefined') {
      this.template = _.template(template)
    }
    this.currentStep = 1
    this.saveRequired = false
    this.catalogue = $('html').data('catalogue')

    const that = this
    this.listenTo(this.model, 'error', function (model, response) {
      that.$('#editorAjax').toggleClass('visible')

      alert('There was a problem communicating with the server.' + '\n' + 'Server response:' + '\n' +
          +`${response.status}` + '\n' + `${response.statusText}` + '\n' +
          'Please save this record locally by copying the text below to a file.' + '\n' + JSON.stringify(model.toJSON()))
    })
    this.listenTo(this.model, 'sync', function () {
      that.$('#editorAjax').toggleClass('visible')
      that.saveRequired = false
    })
    this.listenTo(this.model, 'change save:required', function () {
      that.saveRequired = true
    })
    this.listenTo(this.model, 'request', function () {
      that.$('#editorAjax').toggleClass('visible')
    })
    this.listenTo(this.model, 'invalid', function (model, errors) {
      const errorString = ''
      _.each(errors, function (error) {
        errorString.concat(`<p>${error}</p>`)
      })
      alert('Validation Errors' + '\n' + errorString)
    })

    this.render()
    _.invoke(this.sections[0].views, 'show')
    this.sections.forEach(section => {
      this.$('#editorNav').append($(`<li title='${section.title}'>${section.label}</li>`))
    })

    this.$('#editorNav').find('li').first().addClass('active')
  },

  attemptDelete () {
    if (confirm('Are you sure you want to delete this record?')) {
      this.delete()
    }
  },

  delete () {
    this.model.destroy({
      success: () => {
        _.invoke(this.sections, 'remove')
        this.remove()
        Backbone.history.location.replace(`/${this.catalogue}/documents`)
        alert('Record deleted')
      }
    })
  },

  save () {
    this.model.save()
  },

  attemptExit () {
    if (this.saveRequired) {
      if (confirm('There are unsaved changes to this record' + '\n' + 'Do you want to exit without saving?')) {
        this.exit()
      }
    } else {
      this.exit()
    }
  },

  exit () {
    _.invoke(this.sections, 'remove')
    this.remove()

    if ((Backbone.history.location.pathname === `/${this.catalogue}/documents`) && !this.model.isNew()) {
      Backbone.history.location.replace(`/documents/${this.model.get('id')}`)
    } else {
      Backbone.history.location.reload()
    }
  },

  back () {
    this.navigate(this.currentStep - 1)
  },

  next () {
    this.navigate(this.currentStep + 1)
  },

  direct (event) {
    let node = event.currentTarget
    let step = 0
    while (node !== null) {
      step++
      node = node.previousElementSibling
    }

    this.navigate(step)
  },

  navigate (newStep) {
    const $nav = this.$('#editorNav li')
    const maxStep = $nav.length
    this.currentStep = newStep
    if (this.currentStep < 1) { this.currentStep = 1 }
    if (this.currentStep > maxStep) { this.currentStep = maxStep }

    if (this.currentStep === 1) {
      this.$('#editorBack').prop('disabled', true)
    } else {
      this.$('#editorBack').prop('disabled', false)
    }

    if (this.currentStep === maxStep) {
      this.$('#editorNext').prop('disabled', true)
    } else {
      this.$('#editorNext').prop('disabled', false)
    }

    $nav.filter('.active').toggleClass('active')
    this.$($nav[this.currentStep - 1]).toggleClass('active')

    _.each(this.sections, (section, index) => {
      let method = 'hide'
      if ((this.currentStep - 1) === index) {
        method = 'show'
      }
      _.invoke(section.views, method)
    })
  },

  render () {
    this.$el.html(this.template(this.model.attributes))
    this.sections.forEach(section => {
      section.views.forEach(view => {
        this.$('#editor').append(view.render().el)
      })
    })
    return this
  }
})
