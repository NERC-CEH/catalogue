# web/

## Testing
- The specs run on Vitest in a real browser (Chromium via Playwright, `vitest.config.mjs`).
- CI runs `npm run test-coverage`, which fails below the coverage floor in `vitest.config.mjs`; every source file counts, so an untested one lowers the figures. Run it before shipping web changes.
- Specs import what they use from `vitest` (`vi` for spies, mocks and the clock) and use `fakeServer` from `src/test/support.js` for jQuery's XHR.
