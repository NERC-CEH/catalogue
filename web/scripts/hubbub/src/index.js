
import { UploadView, UploadModel } from './Upload'

export default function init () {
  const id = $('#document-upload').data('guid')
  return new UploadView({
    el: '.document-upload',
    model: new UploadModel({ id })
  })
}

$(document).ready(init)
