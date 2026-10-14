// StandardJS's rules, through neostandard on ESLint's flat config: one lint tool and one config,
// replacing the standard package and its EOL ESLint 8 (NERC-CEH/dri-one#366). `npm run lint` runs it;
// `npm run lint-fix` fixes what it can.
const neostandard = require('neostandard')
const globals = require('globals')

module.exports = [
  // Built and generated files (an object holding only ignores applies to every other one)
  { ignores: ['dist/**', 'build/**', 'coverage/**', 'css/**'] },
  ...neostandard({
    // The application and its specs run in the browser, the specs under Jasmine
    env: ['browser', 'jasmine']
  }),
  {
    // The build's own scripts run in Node
    files: ['*.js', 'scripts/**/*.js'],
    languageOptions: { globals: globals.node }
  }
]
