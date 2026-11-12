import $ from 'jquery'
import { FORMATS, findFormat } from '../formats'
import { cleanText } from './textUtils'

export const FORMAT_DATALIST_ID = 'catalogue-formats'

/** Adds the shared format names to the page once, for inputs to offer through `list`. */
export function ensureFormatDatalist () {
  if (document.getElementById(FORMAT_DATALIST_ID)) return
  const list = $('<datalist>').attr('id', FORMAT_DATALIST_ID)
  FORMATS.forEach(f => list.append($('<option>').val(f.name)))
  $(document.body).append(list)
}

/** The canonical name and type of a listed format; any other text is kept, trimmed, with no type. */
export function pickFormat (text) {
  const format = findFormat(text)
  return format ? { name: format.name, type: format.type } : { name: String(text ?? '').trim(), type: undefined }
}

/**
 * Sets `format` and `formatType` from a format input: a listed format, or an extension of one, brings
 * its media type; anything else has none to offer. The input is left showing the name kept.
 */
export function setFormat (model, input) {
  const { name, type } = pickFormat(cleanText($(input).val().trim()))
  $(input).val(name)
  if (name) model.set('format', name)
  else model.unset('format')
  if (type) model.set('formatType', type)
  else model.unset('formatType')
}
