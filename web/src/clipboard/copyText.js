/**
 * Copy an input's value to the clipboard. The text is selected first (and stays selected, so the
 * user can copy by hand if this fails), then the async Clipboard API is tried, falling back to
 * document.execCommand('copy') where it is missing or refuses.
 *
 * @param {HTMLInputElement|HTMLTextAreaElement} input
 * @returns {Promise<boolean>} true if either route succeeded
 */
export function copyText (input) {
  input.focus()
  input.select()

  const legacyCopy = () => {
    try {
      return document.execCommand('copy') === true
    } catch (e) {
      return false
    }
  }

  if (navigator.clipboard && navigator.clipboard.writeText) {
    return navigator.clipboard.writeText(input.value).then(() => true, legacyCopy)
  }
  return Promise.resolve(legacyCopy())
}
