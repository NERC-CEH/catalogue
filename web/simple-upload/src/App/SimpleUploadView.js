import $ from 'jquery'
import Backbone from 'backbone'
import { FileCollection, FileListView, UploadView } from '../File'
import { MessageListView } from '../Message'

export default Backbone.View.extend({

  initialize (options) {
    const files = new FileCollection({ url: options.url })

    const messages = new Backbone.Collection()

    const messageListView = new MessageListView({
      el: '#messages',
      messages
    })

    const uploadView = new UploadView({
      el: '#simple-upload-dropzone',
      files,
      messages,
      url: options.url
    })

    const fileListView = new FileListView({
      el: '#files',
      files,
      messages
    })

    if ($('#files-data').length) { files.reset(JSON.parse($('#files-data').text())) }
    if ($('#messages-data').length) { return messages.reset(JSON.parse($('#messages-data').text())) }
  }
})
