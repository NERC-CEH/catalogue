import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import Yasgui from '@triply/yasgui'
import { examples, startSparql } from '../index'
import prefixes from '../prefixes.json'
import { fakeServer } from '../../test/support'

// The public SPARQL page: YASGUI on the catalogue's endpoint, our own prefix list, and example queries
// loaded into the editor on click (dri-one #473)
describe('The SPARQL page', () => {
  let element, list

  beforeEach(() => {
    element = document.createElement('div')
    element.dataset.endpoint = 'https://catalogue.ceh.ac.uk/fuseki/sparql'
    element.dataset.catalogueGraph = 'https://catalogue.ceh.ac.uk/graph/eidc'
    list = document.createElement('ul')
    document.body.append(element, list)
  })

  afterEach(() => {
    element.remove()
    list.remove()
  })

  describe('with YASGUI watched', () => {
    let yasqe, made

    function FakeYasgui (el, options) {
      made = { el, options }
      this.getTab = () => ({ yasqe })
    }

    beforeEach(() => {
      yasqe = { addPrefixes: vi.fn(), setValue: vi.fn() }
      startSparql(element, list, FakeYasgui)
    })

    it('queries the page\'s endpoint, in the page\'s element', () => {
      expect(made.el).toBe(element)
      expect(made.options.requestConfig).toEqual({ endpoint: 'https://catalogue.ceh.ac.uk/fuseki/sparql' })
    })

    it('suggests prefixes from our own list rather than prefix.cc, which the CSP refuses', () => {
      const prefixList = new URL(made.options.yasqe.prefixCcApi)

      expect(prefixList.origin).toBe(window.location.origin)
      expect(prefixList.pathname).toMatch(/\.json$/)
    })

    it('declares the namespaces the examples use', () => {
      expect(yasqe.addPrefixes).toHaveBeenCalledWith(expect.objectContaining({
        dcterms: 'http://purl.org/dc/terms/',
        skos: 'http://www.w3.org/2004/02/skos/core#',
        prov: 'http://www.w3.org/ns/prov#',
        frapo: 'http://purl.org/cerif/frapo/'
      }))
    })

    it('lists every example as a button, without writing one into the editor', () => {
      const buttons = [...list.querySelectorAll('li > button[type=button]')]

      expect(buttons.map(button => button.textContent)).toEqual(examples('').map(example => example.name))
      expect(yasqe.setValue).not.toHaveBeenCalled()
    })

    it('loads an example into the editor when it is clicked, on the page\'s own graph', () => {
      list.querySelector('button').click()

      const query = yasqe.setValue.mock.lastCall[0]
      expect(query).toBe(examples('https://catalogue.ceh.ac.uk/graph/eidc')[0].query.join('\n'))
      expect(query).toContain('GRAPH <https://catalogue.ceh.ac.uk/graph/eidc>')
    })
  })

  it('works with YASGUI itself, which fetches our prefix list', () => {
    const tabs = Object.keys(window.localStorage)
    fakeServer.install()
    const yasgui = startSparql(element, list, Yasgui)
    try {
      const request = fakeServer.lastRequest()
      expect(new URL(request.url, window.location.href).origin).toBe(window.location.origin)
      request.respondWith({ status: 200, contentType: 'application/json', responseText: JSON.stringify(prefixes) })
      expect(yasgui.getTab().yasqe.getPrefixesFromQuery()).toEqual(expect.objectContaining({ dcat: 'http://www.w3.org/ns/dcat#' }))

      list.querySelectorAll('button')[1].click()

      expect(yasgui.getTab().yasqe.getValue()).toBe(examples('')[1].query.join('\n'))
    } finally {
      fakeServer.uninstall()
      yasgui.destroy()
      Object.keys(window.localStorage).filter(key => !tabs.includes(key)).forEach(key => window.localStorage.removeItem(key))
    }
  })
})
