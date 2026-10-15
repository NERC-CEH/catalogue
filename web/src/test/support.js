/*
 * A fake server for the specs, which Vitest doesn't provide: the jQuery the code under test sends its
 * requests with uses XMLHttpRequest, which this replaces with Sinon's fake (nise), so nothing is sent
 * and the spec answers each request itself. Spies, mocks and the clock are Vitest's own (`vi`).
 * It kept the interface the specs used under Karma and jasmine-ajax (NERC-CEH/dri-one#473, #468).
 */
import { fakeXhr } from 'nise'

let fake = null
let caughtRequests = []

// A request the fake server caught: what the specs read of it, and how they answer it
function caught (request) {
  return request && {
    get url () { return request.url },
    get method () { return request.method },
    get status () { return request.status },
    // What was sent: the body as a string, and the headers by name
    get body () { return request.requestBody },
    get headers () { return request.requestHeaders },
    // response: { status, contentType, responseText, responseHeaders }
    // JSON unless said otherwise, as jasmine-ajax assumed: jQuery parses a response by its type
    respondWith ({ status = 200, contentType = 'application/json', responseText = '', responseHeaders = {} }) {
      const headers = { 'Content-Type': contentType, ...responseHeaders }
      request.respond(status, headers, responseText)
    }
  }
}

export const fakeServer = {
  install () {
    caughtRequests = []
    fake = fakeXhr.useFakeXMLHttpRequest()
    fake.onCreate = request => caughtRequests.push(request)
  },
  uninstall () {
    fake?.restore()
    fake = null
  },
  lastRequest () { return caught(caughtRequests.at(-1)) },
  // Every request caught, oldest first, for answering one that isn't the latest
  requests () { return caughtRequests.map(caught) },
  requestCount () { return caughtRequests.length }
}
