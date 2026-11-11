// The search state's fields that aren't text. Every other value, a term of 007 or true among them,
// stays the text it was typed as (deparam.js turned anything that looked like a number into one)
const NUMBERS = ['page', 'rows']
const BOOLEANS = ['semantic']

const typed = (key, value) => {
  if (NUMBERS.includes(key) && value.trim() !== '' && Number.isFinite(Number(value))) return Number(value)
  if (BOOLEANS.includes(key) && (value === 'true' || value === 'false')) return value === 'true'
  return value
}

/**
 * Reads a search's state from a query string (with or without its ?), as the page's form, a link,
 * or SearchRouter wrote it. A key given more than once, as facet can be, becomes a list.
 * URLSearchParams reads + as a space, as the server does: the search form's GET sends one (dri-one #532)
 * Collected in a Map, so a key of __proto__ in a crafted link is just a key, not the object's prototype.
 */
export const parseSearchQuery = (query = '') => {
  const state = new Map()
  for (const [key, raw] of new URLSearchParams(query)) {
    const value = typed(key, raw)
    state.set(key, state.has(key) ? [].concat(state.get(key), value) : value)
  }
  return Object.fromEntries(state)
}
