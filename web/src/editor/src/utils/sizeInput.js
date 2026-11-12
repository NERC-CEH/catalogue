import $ from 'jquery'

const SIZE_NOT_RECOGNISED = 'Size not recognised, e.g. 32 MB or 2-2.5 MB'

/** Shows, or clears, the message beside a size box that could not be read. */
export function showSizeRecognised (input, recognised) {
  $(input).siblings('.size-error').text(recognised ? '' : SIZE_NOT_RECOGNISED).prop('hidden', recognised)
}
