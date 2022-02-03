import $ from 'jquery'
import Backbone from 'backbone'
import { FileCollection, FileListView, UploadView } from '../File'
import { MessageListView } from '../Message'

export var SimpleUploadView = Backbone.View.extend({

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

    const $filesData = $('#files-data')
    if ($filesData.length) { files.reset(JSON.parse($filesData.text())) }
    const $messageData = $('#messages-data')
    if ($messageData.length) { return messages.reset(JSON.parse($messageData.text())) }
  }
})
