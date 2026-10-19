/*
 * Declares the global that the regenerator-runtime bundled in YASGUI assigns as it loads. The bundle now
 * runs as strict code, where assigning an undeclared global throws, so it falls back to
 * Function('r', 'regeneratorRuntime = r'): a string evaluated as script, which the
 * Content-Security-Policy refuses without 'unsafe-eval', and the page then never starts
 * (NERC-CEH/dri-one#454). Declared here, the assignment succeeds and the fallback never runs.
 *
 * A module of its own, imported before '@triply/yasgui', because imports are evaluated before the
 * importing module's own statements.
 */
globalThis.regeneratorRuntime ??= undefined
