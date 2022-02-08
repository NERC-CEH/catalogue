import Jasmine from 'jasmine'

const jasmine = new Jasmine()
// modify this line to point to your jasmine.json
jasmine.loadConfigFile('./spec/support/jasmine.json')
jasmine.execute().then(passed => {
  if (passed) {
    console.log('All specs have passed')
  } else {
    console.log('At least one spec has failed')
  }
})
