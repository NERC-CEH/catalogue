module.exports = function (grunt) {
  grunt.loadNpmTasks('grunt-concurrent')
  grunt.loadNpmTasks('grunt-contrib-clean')
  grunt.loadNpmTasks('grunt-contrib-less')
  grunt.loadNpmTasks('grunt-contrib-cssmin')
  grunt.loadNpmTasks('grunt-contrib-watch')

  grunt.initConfig({
    less: {
      options: {
        compress: false,
        paths: ['css']
      },
      assist: {
        files: {
          'css/style-assist.css': 'less/style-assist.less'
        }
      },
      ceh: {
        files: {
          'css/style-ceh.css': 'less/style-ceh.less'
        }
      },
      cmp: {
        files: {
          'css/style-cmp.css': 'less/style-cmp.less'
        }
      },
      datalabs: {
        files: {
          'css/style-datalabs.css': 'less/style-datalabs.less'
        }
      },
      edge: {
        files: {
          'css/style-edge.css': 'less/style-edge.less'
        }
      },
      eidc: {
        files: {
          'css/style-eidc.css': 'less/style-eidc.less'
        }
      },
      elter: {
        files: {
          'css/style-elter.css': 'less/style-elter.less'
        }
      },
      erammp: {
        files: {
          'css/style-erammp.css': 'less/style-erammp.less'
        }
      },
      inlicensed: {
        files: {
          'css/style-inlicensed.css': 'less/style-inlicensed.less'
        }
      },
      inms: {
        files: {
          'css/style-inms.css': 'less/style-inms.less'
        }
      },
      nc: {
        files: {
          'css/style-nc.css': 'less/style-nc.less'
        }
      },
      nm: {
        files: {
          'css/style-nm.css': 'less/style-nm.less'
        }
      },
      m: {
        files: {
          'css/style-m.css': 'less/style-m.less'
        }
      },
      osdp: {
        files: {
          'css/style-osdp.css': 'less/style-osdp.less'
        }
      },
      infrastructure: {
        files: {
          'css/style-infrastructure.css': 'less/style-infrastructure.less'
        }
      },
      sa: {
        files: {
          'css/style-sa.css': 'less/style-sa.less'
        }
      },
      ukeof: {
        files: {
          'src/css/style-ukeof.css': 'src/less/style-ukeof.less'
        }
      },
      ukscape: {
        files: {
          'css/style-ukscape.css': 'less/style-ukscape.less'
        }
      }
    },
    cssmin: {
      assist: {
        files: {
          'css/style-assist.css': 'css/style-assist.css'
        }
      },
      ceh: {
        files: {
          'css/style-ceh.css': 'css/style-ceh.css'
        }
      },
      cmp: {
        files: {
          'css/style-cmp.css': 'css/style-cmp.css'
        }
      },
      datalabs: {
        files: {
          'css/style-datalabs.css': 'css/style-datalabs.css'
        }
      },
      edge: {
        files: {
          'css/style-edge.css': 'css/style-edge.css'
        }
      },
      eidc: {
        files: {
          'css/style-eidc.css': 'css/style-eidc.css'
        }
      },
      elter: {
        files: {
          'css/style-elter.css': 'css/style-elter.css'
        }
      },
      erammp: {
        files: {
          'css/style-erammp.css': 'css/style-erammp.css'
        }
      },
      inlicensed: {
        files: {
          'css/style-inlicensed.css': 'css/style-inlicensed.css'
        }
      },
      inms: {
        files: {
          'css/style-inms.css': 'css/style-inms.css'
        }
      },
      nc: {
        files: {
          'css/style-nc.css': 'css/style-nc.css'
        }
      },
      nm: {
        files: {
          'css/style-nm.css': 'css/style-nm.css'
        }
      },
      m: {
        files: {
          'css/style-m.css': 'css/style-m.css'
        }
      },
      osdp: {
        files: {
          'css/style-osdp.css': 'css/style-osdp.css'
        }
      },
      infrastructure: {
        files: {
          'css/style-infrastructure.css': 'css/style-infrastructure.css'
        }
      },
      sa: {
        files: {
          'css/style-sa.css': 'css/style-sa.css'
        }
      },
      ukeof: {
        files: {
          'src/css/style-ukeof.css': 'src/css/style-ukeof.css'
        }
      },
      ukscape: {
        files: {
          'css/style-ukscape.css': 'css/style-ukscape.css'
        }
      }
    },
    watch: {
      less: {
        files: 'less/*',
        tasks: ['less']
      }
    },
    concurrent: {
      watch: {
        tasks: ['watch:less'],
        options: {
          logConcurrentOutput: true
        }
      }
    },
    clean: {
      test: ['test-compiled'],
      prep: ['css']
    }
  })
  grunt.registerTask('develop', ['less', 'concurrent:watch'])
  grunt.registerTask('build', ['clean', 'less', 'cssmin'])
  grunt.registerTask('default', ['prep', 'build'])
}
