/* global regeneratorRuntime:writable */
import { describe, expect, it } from 'vitest'
import '../regenerator-global'
import source from '../index.js?raw'

// YASGUI's bundled regenerator-runtime assigns regeneratorRuntime as strict code; if that throws, its
// fallback evaluates a string, which the Content-Security-Policy refuses (dri-one #454). This file
// imports no YASGUI, so nothing else has declared the global.
describe('The SPARQL page\'s regeneratorRuntime global', () => {
  it('can be assigned from strict code', () => {
    const runtime = {}

    expect(() => { regeneratorRuntime = runtime }).not.toThrow()
    expect(regeneratorRuntime).toBe(runtime)
    expect(globalThis.regeneratorRuntime).toBe(runtime)
  })

  it('is declared before the page imports YASGUI', () => {
    const imports = source.match(/^import .*$/gm)

    expect(imports.indexOf('import \'./regenerator-global\'')).toBeGreaterThan(-1)
    expect(imports.indexOf('import \'./regenerator-global\''))
      .toBeLessThan(imports.indexOf('import Yasgui from \'@triply/yasgui\''))
  })
})
