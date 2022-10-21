import Backbone from 'backbone'
import { FileCollection, FileListView, UploadView } from '../File'
import { MessageListView } from '../Message'

export default Backbone.View.extend({

  initialize (options) {
    const files = new FileCollection({ url: options.url })

    const messages = new Backbone.Collection()

    // eslint-disable-next-line no-unused-vars
    const messageListView = new MessageListView({
      el: '#messages',
      messages
    })

    // eslint-disable-next-line no-unused-vars
    const uploadView = new UploadView({
      el: '#simple-upload-dropzone',
      files,
      messages,
      url: options.url
    })

    // eslint-disable-next-line no-unused-vars
    const fileListView = new FileListView({
      el: '#files',
      files,
      messages
    })

    if ($('#files-data').length) { files.reset(JSON.parse($('#files-data').text())) }
    if ($('#messages-data').length) { return messages.reset(JSON.parse($('#messages-data').text())) }
  }
})
