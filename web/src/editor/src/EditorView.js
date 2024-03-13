import Swal from 'sweetalert2'
import _ from 'underscore'
import $ from 'jquery'
import Backbone from 'backbone'
import template from './editorTemplate'

export default Backbone.View.extend({

  events: {
    'click #editorDelete': 'attemptDelete',
    'click #confirmDeleteYes': 'delete',
    'click #editorExit': 'attemptExit',
    'click #editorSave': 'save',
    'click #editorBack': 'back',
    'click #editorNext': 'next',
    'click #editorNav li': 'direct'
  },

  initialize () {
    this.saveRequired = false
    this.template = template
    this.currentStep = 1
    this.catalogue = $('html').data('catalogue')

    const that = this
    this.listenTo(this.model, 'error', function (model, response) {
      that.$('#editorAjax').toggleClass('visible')

      Swal.fire({
        title: `Server response: ${response.status} ${response.statusText}`,
        text: 'There was a problem communicating with the server! \n Please save this record locally by copying the text below to a file.',
        html: `<textarea readonly style="resize:none; height:auto;" rows="20">${JSON.stringify(model.toJSON())}</textarea>`,
        icon: 'error',
        confirmButtonText: 'Close'
      })
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
      let errorString = ''
      _.each(errors, function (error) {
        errorString = errorString.concat(`<p>${error}</p>`)
      })
      Swal.fire({
        title: 'Validation Errors',
        html: errorString,
        icon: 'error',
        confirmButtonText: 'Close'
      })
    })

    this.render()
    _.invoke(this.sections[0].views, 'show')
    this.sections.forEach(section => {
      this.$('#editorNav').append($(`<li title='${section.title}'>${section.label}</li>`))
    })

    this.$('#editorNav').find('li').first().addClass('active')
  },

  attemptDelete () {
    Swal.fire({
      title: 'Are you sure?',
      confirmButtonText: 'Yes, delete this record',
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#3085d6',
      cancelButtonColor: '#d33'
    }).then((result) => {
      if (result.isConfirmed) {
        this.delete()
      }
    })
  },

  delete () {
    this.model.destroy({
      success: () => {
        _.invoke(this.sections, 'remove')
        this.remove()
        Backbone.history.location.replace(`/${this.catalogue}/documents`)
        Swal.fire({
          title: 'Deleted!',
          html: 'Your file has been deleted.',
          icon: 'success'
        })
      }
    })
  },

  save () {
    if (this.model.save()) {
      Swal.fire({
        title: 'Saved!',
        icon: 'success'
      })
      this.saveRequired = false
    }
  },

  confirmExit () {
    const that = this
    Swal.fire({
      title: 'There are unsaved changes.',
      showCancelButton: true,
      icon: 'warning',
      confirmButtonText: 'Exit without saving'
    }).then((result) => {
      if (result.isConfirmed) {
        that.exit()
      }
    })
  },

  attemptExit () {
    if (this.saveRequired === true) {
      this.confirmExit()
    } else {
      this.exit()
    }
  },

  exit () {
    _.invoke(this.sections, 'remove')
    $('body').toggleClass('edit-mode')
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
    $('body').toggleClass('edit-mode')
    $('#search').toggleClass('search')
    this.sections.forEach(section => {
      section.views.forEach(view => {
        this.$('#editor').append(view.render().el)
      })
    })
    return this
  }
})
