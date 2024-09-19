import Backbone from 'backbone'

export default Backbone.View.extend({
  events: { submit: 'onSubmit' },

  initialize () {
    this.updateDisplayedInputs(this.model)
    this.listenTo(this.model, 'change', this.updateDisplayedInputs)
  },

  onSubmit (event) {
    event.preventDefault()
    const recordType = this.$('input[name=recordType]:checked').get().map(el => el.value)
    this.model.set({
      ...Object.fromEntries(
        this.$el.serializeArray()
          .filter(({ name, value }) => value && this.model.searchFields.includes(name))
          .map(({ name, value }) => [name, value])
      ),
      recordType
    })
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
