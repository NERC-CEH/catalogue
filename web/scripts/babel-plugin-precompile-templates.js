/*
 * Compiles underscore templates at build time (NERC-CEH/dri-one#464).
 *
 * `_.template(text)` compiles its text with `new Function` when the module loads. Every page loads
 * modules with templates, so the Content-Security-Policy had to allow 'unsafe-eval'. This plugin
 * replaces each `_.template` call on a literal with the function underscore would have compiled, so
 * the browser compiles nothing.
 *
 * Underscore's compiled source evaluates the template inside `with (obj) { ... }`, which isn't
 * allowed in the strict mode ES modules run in. So the `with` is taken out and each name it would
 * have resolved is rewritten to the same lookup: `('name' in obj ? obj.name : globalThis.name)`, the
 * data's property if it has one, otherwise the global. Names the template declares itself, and the
 * JavaScript built-ins, are left as they are. A template that assigns to such a name fails the
 * build, because `with` would have assigned to the data object and this can't know which.
 *
 * In application code (not specs), a `_.template` call on anything but a literal fails the build
 * too, as it would need 'unsafe-eval' again: compile the parts separately and concatenate what they
 * render, as LegiloBase.js does.
 */
const underscore = require('underscore')

// Globals a template may use that its data never shadows; left as plain references
const BUILTINS = new Set([
  'undefined', 'NaN', 'Infinity', 'arguments', 'globalThis', 'window', 'document',
  'Math', 'JSON', 'Array', 'Object', 'String', 'Number', 'Boolean', 'Date', 'RegExp',
  'encodeURIComponent', 'decodeURIComponent', 'encodeURI', 'decodeURI', 'parseInt', 'parseFloat',
  'isNaN', 'isFinite'
])

// The source underscore compiles, without its `with`, as a function of (obj, _)
function unwrap (text) {
  const source = underscore.template(text).source
  const open = 'with(obj||{}){\n'
  const close = '}\nreturn __p;\n}'
  const start = source.indexOf(open)
  if (!source.startsWith('function(obj){\n') || start < 0 || !source.endsWith(close)) {
    throw new Error(`underscore ${underscore.VERSION} compiled a template in an unexpected form:\n${source}`)
  }
  const declarations = source.slice('function(obj){\n'.length, start)
  const body = source.slice(start + open.length, source.length - close.length)
  return `(function (obj, _) {\nobj = obj || {};\n${declarations}${body}\nreturn __p;\n})`
}

// Rewrites the names `with (obj)` would have looked up on the data
function freeNames ({ types: t }) {
  const free = path => !BUILTINS.has(path.node.name) && !path.scope.hasBinding(path.node.name)
  return {
    visitor: {
      ReferencedIdentifier (path) {
        if (!path.isIdentifier() || !free(path)) return
        const name = path.node.name
        path.replaceWith(t.conditionalExpression(
          t.binaryExpression('in', t.stringLiteral(name), t.identifier('obj')),
          t.memberExpression(t.identifier('obj'), t.identifier(name)),
          t.memberExpression(t.identifier('globalThis'), t.identifier(name))
        ))
        path.skip()
      },
      'AssignmentExpression|UpdateExpression' (path) {
        const target = path.isAssignmentExpression() ? path.get('left') : path.get('argument')
        if (target.isIdentifier() && free(target)) {
          throw target.buildCodeFrameError(`a template assigns to ${target.node.name}, which it doesn't declare`)
        }
      }
    }
  }
}

function literalText (argument) {
  if (argument.isStringLiteral()) return argument.node.value
  if (argument.isTemplateLiteral() && argument.node.expressions.length === 0) return argument.node.quasis[0].value.cooked
  return null
}

module.exports = function precompileTemplates (babel) {
  const { types: t, template } = babel
  return {
    name: 'precompile-underscore-templates',
    visitor: {
      CallExpression (path, state) {
        const callee = path.get('callee')
        if (!callee.isMemberExpression() || !callee.get('property').isIdentifier({ name: 'template' })) return
        const object = callee.get('object')
        const binding = object.isIdentifier() && path.scope.getBinding(object.node.name)
        if (!binding || binding.kind !== 'module' || binding.path.parent.source.value !== 'underscore') return

        const args = path.get('arguments')
        const text = args.length === 1 ? literalText(args[0]) : null
        if (text === null) {
          if (/[\\/]test[\\/]/.test(state.filename || '')) return
          throw path.buildCodeFrameError('_.template needs a literal template (and no settings) to be compiled at build time')
        }

        let compiled
        try {
          const { ast } = babel.transformSync(unwrap(text), {
            configFile: false, babelrc: false, sourceType: 'script', plugins: [freeNames], ast: true, code: false
          })
          compiled = ast.program.body[0].expression
        } catch (error) {
          throw path.buildCodeFrameError(`could not compile this template: ${error.message}`)
        }
        // Called as underscore calls its compiled templates, with `this` passed through
        const call = t.callExpression(
          template.expression.ast`(function (render, _) {
            return function (data) { return render.call(this, data, _) }
          })`,
          [compiled, t.cloneNode(object.node)]
        )
        // The functions are underscore's, written by this build, not the catalogue's code: left out of
        // the specs' coverage, as they were when the coverage plugin saw the source before this one did.
        // Each is marked: printed, a comment on the call lands on its callee alone, not the render
        // function passed to it
        t.addComment(call, 'leading', ' istanbul ignore next ')
        t.addComment(compiled, 'leading', ' istanbul ignore next ')
        path.replaceWith(call)
      }
    }
  }
}
