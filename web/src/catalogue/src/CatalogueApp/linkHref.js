import $ from 'jquery'

/**
 * The address a catalogue control was clicked for. The control is either the link itself or a span
 * around it, and the click can land on the icon inside the link, where event.target has no href
 * (NERC-CEH/dri-one#461).
 */
export default function linkHref (event) {
  return $(event.target).closest('a').attr('href') || $(event.currentTarget).find('a').attr('href')
}
