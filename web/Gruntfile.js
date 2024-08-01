const sass = require('sass')

module.exports = function (grunt) {
  grunt.initConfig({
    clean: {
      build: ['css/*']
    },
    sass: {
      options: {
        implementation: sass,
        outputStyle: 'compressed',
        sourceMap: true,
        quietDeps: true
      },
      build: {
        files: [
          { src: 'scss/style-assist.scss', dest: 'css/style-assist.css' },
          { src: 'scss/style-ukceh.scss', dest: 'css/style-ukceh.css' },
          { src: 'scss/style-cmp.scss', dest: 'css/style-cmp.css' },
          { src: 'scss/style-datalabs.scss', dest: 'css/style-datalabs.css' },
          { src: 'scss/style-edge.scss', dest: 'css/style-edge.css' },
          { src: 'scss/style-eidc.scss', dest: 'css/style-eidc.css' },
          { src: 'scss/style-elter.scss', dest: 'css/style-elter.css' },
          { src: 'scss/style-erammp.scss', dest: 'css/style-erammp.css' },
          { src: 'scss/style-inlicensed.scss', dest: 'css/style-inlicensed.css' },
          { src: 'scss/style-inms.scss', dest: 'css/style-inms.css' },
          { src: 'scss/style-nc.scss', dest: 'css/style-nc.css' },
          { src: 'scss/style-nm.scss', dest: 'css/style-nm.css' },
          { src: 'scss/style-m.scss', dest: 'css/style-m.css' },
          { src: 'scss/style-osdp.scss', dest: 'css/style-osdp.css' },
          { src: 'scss/style-pimfe.scss', dest: 'css/style-pimfe.css' },
          { src: 'scss/style-infrastructure.scss', dest: 'css/style-infrastructure.css' },
          { src: 'scss/style-sa.scss', dest: 'css/style-sa.css' },
          { src: 'scss/style-ukeof.scss', dest: 'css/style-ukeof.css' },
          { src: 'scss/style-ukscape.scss', dest: 'css/style-ukscape.css' }
        ]
      }
    },
    watch: {
      sass: {
        files: 'scss/*',
        tasks: ['sass']
      }
    },
    concurrent: {
      watch: {
        tasks: ['watch:sass'],
        options: {
          logConcurrentOutput: true
        }
      }
    }
  })
  grunt.loadNpmTasks('grunt-concurrent')
  grunt.loadNpmTasks('grunt-contrib-clean')
  grunt.loadNpmTasks('grunt-sass')
  grunt.loadNpmTasks('grunt-contrib-watch')
  grunt.registerTask('develop', ['sass', 'concurrent'])
  grunt.registerTask('build', ['clean', 'sass'])
  grunt.registerTask('default', ['build'])
}
