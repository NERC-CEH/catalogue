import { afterEach, describe, expect, it } from 'vitest'
import _ from 'underscore'

// The build compiles each `_.template` literal ahead of time (scripts/babel-plugin-precompile-templates.js,
// dri-one #464). Each template here is compiled that way and, from the same text, by underscore in the
// browser (`_['template']`, which the plugin leaves alone); the two must render alike.
const runtime = _['template']

describe('A template compiled at build time', () => {
  afterEach(() => { delete window.precompiledTemplateGlobal })

  it('escapes, interpolates and evaluates as underscore does', () => {
    const precompiled = _.template('<%- data.text %>|<%= data.html %>|<% if (data.flag) { %>yes<% } %>')
    const compiled = runtime('<%- data.text %>|<%= data.html %>|<% if (data.flag) { %>yes<% } %>')
    const data = { text: '<b>"Tom & Jerry"</b>', html: '<i>x</i>', flag: true }

    expect(precompiled({ data })).toEqual(compiled({ data }))
    expect(precompiled({ data })).toEqual('&lt;b&gt;&quot;Tom &amp; Jerry&quot;&lt;/b&gt;|<i>x</i>|yes')
  })

  it('loops, declares its own variables and prints', () => {
    const precompiled = _.template('<% _.each(items, function (item, i) { var n = i + 1 %><%- n %>:<%- item %>;<% }) %><% print(items.length) %>')
    const compiled = runtime('<% _.each(items, function (item, i) { var n = i + 1 %><%- n %>:<%- item %>;<% }) %><% print(items.length) %>')

    expect(precompiled({ items: ['a', 'b'] })).toEqual(compiled({ items: ['a', 'b'] }))
    expect(precompiled({ items: ['a', 'b'] })).toEqual('1:a;2:b;2')
  })

  it('falls back to a global for a name the data lacks, as `with` did', () => {
    window.precompiledTemplateGlobal = 'global'
    const precompiled = _.template('<%- precompiledTemplateGlobal %>')
    const compiled = runtime('<%- precompiledTemplateGlobal %>')

    expect(precompiled({})).toEqual(compiled({}))
    expect(precompiled({ precompiledTemplateGlobal: 'data' })).toEqual('data')
  })

  it('treats a missing name, or no data at all, as underscore does', () => {
    const precompiled = _.template('<% if (typeof absent === "undefined") { %>none<% } %><%= Math.max(1, 2) %>')
    const compiled = runtime('<% if (typeof absent === "undefined") { %>none<% } %><%= Math.max(1, 2) %>')

    expect(precompiled()).toEqual(compiled())
    expect(precompiled(null)).toEqual('none2')
  })

  it('passes `this` through, as underscore does', () => {
    const view = { name: 'view', precompiled: _.template('<%- this.name %>'), compiled: runtime('<%- this.name %>') }

    expect(view.precompiled({})).toEqual(view.compiled({}))
    expect(view.precompiled({})).toEqual('view')
  })

  it('is a function the build wrote, not one compiled in the browser', () => {
    // underscore's own compiled templates carry their source
    expect(runtime('x').source).toBeDefined()
    expect(_.template('x').source).toBeUndefined()
  })
})
