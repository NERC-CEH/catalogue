import Backbone from 'backbone'

export const DOC_ID_PATTERN = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i
export const DOC_ID_ERROR = 'Enter a valid document ID, e.g. dcfc2081-f42c-4238-93dd-2ce64438b3ea'

export default Backbone.View.extend({
  events: {
    submit: 'onSubmit',
    'input input[name=docId]': 'clearDocIdError'
  },

  initialize () {
    this.updateDisplayedInputs(this.model)
    this.listenTo(this.model, 'change', this.updateDisplayedInputs)
  },

  onSubmit (event) {
    event.preventDefault()
    const docIdInput = this.$('input[name=docId]')
    const docId = (docIdInput.val() || '').trim()
    // An empty document ID means "any document"; anything else must be a well-formed ID
    if (docId && !DOC_ID_PATTERN.test(docId)) {
      this.showDocIdError(docIdInput)
      return
    }
    this.clearDocIdError()
    const recordType = this.$('input[name=recordType]:checked').get().map(el => el.value)
    // Every search field on the form is set, emptied ones to '': dropping them left the previous
    // value in place, so a filter could never be cleared (dri-one #461)
    this.model.set({
      ...Object.fromEntries(
        this.$el.serializeArray()
          .filter(({ name }) => this.model.searchFields.includes(name))
          .map(({ name, value }) => [name, value])
      ),
      ...(docIdInput.length ? { docId } : {}),
      recordType
    })
  },

  showDocIdError (docIdInput) {
    let feedback = docIdInput.siblings('.invalid-feedback')
    if (!feedback.length) {
      feedback = Backbone.$('<div class="invalid-feedback"></div>').insertAfter(docIdInput)
    }
    feedback.text(DOC_ID_ERROR)
    docIdInput.addClass('is-invalid').attr('aria-invalid', 'true')
    docIdInput.trigger('focus')
  },

  clearDocIdError () {
    const docIdInput = this.$('input[name=docId]')
    docIdInput.removeClass('is-invalid').removeAttr('aria-invalid')
    docIdInput.siblings('.invalid-feedback').text('')
  },

  updateDisplayedInputs (model) {
    for (const param in model.changed) {
      if (param === 'recordType') {
        const active = model.changed.recordType
        this.$('input[name=recordType]').each((_index, el) => {
          el.checked = active.includes(el.value)
        })
      } else {
        this.$(`input[name=${param}]`).attr('value', model.changed[param])
      }
    }
  }
})
