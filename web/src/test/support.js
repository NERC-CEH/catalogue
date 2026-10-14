/* global jasmine */
/*
 * The test-runner features the specs use beyond describe/it/expect/spyOn, in one place. They are
 * Jasmine's (with jasmine-ajax) today; replacing Karma (NERC-CEH/dri-one#468) means reimplementing
 * this module, not finding every call in the specs (dri-one #473). Each export offers only what the
 * specs actually use.
 */

// A request the fake server caught: what the specs read of it, and how they answer it
function caught (request) {
  return request && {
    get url () { return request.url },
    get method () { return request.method },
    get status () { return request.status },
    // response: { status, contentType, responseText, responseHeaders }
    respondWith (response) { request.respondWith(response) }
  }
}

// Fakes XMLHttpRequest, so the code under test sends nothing and the spec answers instead
export const fakeServer = {
  install () { jasmine.Ajax.install() },
  uninstall () { jasmine.Ajax.uninstall() },
  lastRequest () { return caught(jasmine.Ajax.requests.mostRecent()) },
  requestCount () { return jasmine.Ajax.requests.count() }
}

// Fakes setTimeout and friends, so a spec can move time on
export const fakeClock = {
  install () { jasmine.clock().install() },
  uninstall () { jasmine.clock().uninstall() },
  tick (milliseconds) { jasmine.clock().tick(milliseconds) }
}

// A stand-alone spy function, and an object of named spy methods
export const spy = name => jasmine.createSpy(name)
export const spyObject = (name, methods) => jasmine.createSpyObj(name, methods)
