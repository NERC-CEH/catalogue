import EditorMetadata from './EditorMetadata'

export default EditorMetadata.extend({

  initialize (data, options, title) {
    EditorMetadata.prototype.initialize.call(this, data, options, title)
    if (this.isNew()) {
      this.set('hasOnlineServiceAgreement', true)
    }
  }
})
