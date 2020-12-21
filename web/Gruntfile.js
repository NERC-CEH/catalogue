module.exports = function(grunt) {
  grunt.loadNpmTasks('grunt-concurrent');
  grunt.loadNpmTasks('grunt-combine-harvester');
  grunt.loadNpmTasks('grunt-contrib-requirejs');
  grunt.loadNpmTasks('grunt-contrib-jasmine');
  grunt.loadNpmTasks('grunt-contrib-coffee');
  grunt.loadNpmTasks('grunt-contrib-clean');
  grunt.loadNpmTasks('grunt-contrib-copy');
  grunt.loadNpmTasks('grunt-contrib-less');
  grunt.loadNpmTasks('grunt-contrib-cssmin');
  grunt.loadNpmTasks('grunt-contrib-watch');

  grunt.initConfig({
    bowerDirectory: require('bower').config.directory,
    jasmine: {
      test: {
        options: {
          specs: 'test-compiled/**/*.spec.js',
          template: require('grunt-template-jasmine-requirejs'),
          templateOptions: {
            requireConfigFile: 'src/scripts/main.js',
            requireConfig: {
              baseUrl: 'src/scripts'
            }
          }
        }
      }
    },
    coffee: {
      test: {
        expand: true,
        cwd: 'test',
        src: ['**/*.coffee'],
        dest: 'test-compiled',
        ext: '.spec.js'
      }
    },
    less: {
      options: {
        compress: false,
        paths: ['less', '<%= bowerDirectory %>/bootstrap/less']
      },
      assist: {
        files: {
          'src/css/style-assist.css': 'src/less/style-assist.less'
        }
      },
      ceh: {
        files: {
          'src/css/style-ceh.css': 'src/less/style-ceh.less'
        }
      },
      cmp: {
        files: {
          'src/css/style-cmp.css': 'src/less/style-cmp.less'
        }
      },
      edge: {
        files: {
          'src/css/style-edge.css': 'src/less/style-edge.less'
        }
      },
      eidc: {
        files: {
          'src/css/style-eidc.css': 'src/less/style-eidc.less'
        }
      },
      elter: {
        files: {
          'src/css/style-elter.css': 'src/less/style-elter.less'
        }
      },
      erammp: {
        files: {
          'src/css/style-erammp.css': 'src/less/style-erammp.less'
        }
      },
      inlicensed: {
        files: {
          'src/css/style-inlicensed.css': 'src/less/style-inlicensed.less'
        }
      },
      inms: {
        files: {
          'src/css/style-inms.css': 'src/less/style-inms.less'
        }
      },
      nc: {
        files: {
          'src/css/style-nc.css': 'src/less/style-nc.less'
        }
      },
      nm: {
        files: {
          'src/css/style-nm.css': 'src/less/style-nm.less'
        }
      },
      m: {
        files: {
          'src/css/style-m.css': 'src/less/style-m.less'
        }
      },
      osdp: {
        files: {
          'src/css/style-osdp.css': 'src/less/style-osdp.less'
        }
      },
      sa: {
        files: {
          'src/css/style-sa.css': 'src/less/style-sa.less'
        }
      }
    },
    cssmin: {
      assist: {
        files: {
          'src/css/style-assist.css': 'src/css/style-assist.css'
        }
      },
      ceh: {
        files: {
          'src/css/style-ceh.css': 'src/css/style-ceh.css'
        }
      },
      cmp: {
        files: {
          'src/css/style-cmp.css': 'src/css/style-cmp.css'
        }
      },
      edge: {
        files: {
          'src/css/style-edge.css': 'src/css/style-edge.css'
        }
      },
      eidc: {
        files: {
          'src/css/style-eidc.css': 'src/css/style-eidc.css'
        }
      },
      elter: {
        files: {
          'src/css/style-elter.css': 'src/css/style-elter.css'
        }
      },
      erammp: {
        files: {
          'src/css/style-erammp.css': 'src/css/style-erammp.css'
        }
      },
      inlicensed: {
        files: {
          'src/css/style-inlicensed.css': 'src/css/style-inlicensed.css'
        }
      },
      inms: {
        files: {
          'src/css/style-inms.css': 'src/css/style-inms.css'
        }
      },
      nc: {
        files: {
          'src/css/style-nc.css': 'src/css/style-nc.css'
        }
      },
      nm: {
        files: {
          'src/css/style-nm.css': 'src/css/style-nm.css'
        }
      },
      m: {
        files: {
          'src/css/style-m.css': 'src/css/style-m.css'
        }
      },
      osdp: {
        files: {
          'src/css/style-osdp.css': 'src/css/style-osdp.css'
        }
      },
      sa: {
        files: {
          'src/css/style-sa.css': 'src/css/style-sa.css'
        }
      }
    },
    requirejs: {
      compile: {
        options: {
          exclude: ['coffee-script'],
          baseUrl: 'src/scripts',
          out: 'src/scripts/main-out.js',
          name: 'main',
          mainConfigFile: 'src/scripts/main.js'
        }
      }
    },
    combine_harvester: {
      openlayers: {
        options: {
          root: 'src/vendor/openlayers/lib/'
        },
        files: {
          'src/vendor/OpenLayers-custom.js': ['src/vendor/openlayers/lib/OpenLayers/Map.js', 'src/vendor/openlayers/lib/OpenLayers/Rule.js', 'src/vendor/openlayers/lib/OpenLayers/Marker.js', 'src/vendor/openlayers/lib/OpenLayers/Kinetic.js', 'src/vendor/openlayers/lib/OpenLayers/Projection.js', 'src/vendor/openlayers/lib/OpenLayers/Layer/OSM.js', 'src/vendor/openlayers/lib/OpenLayers/Layer/WMS.js', 'src/vendor/openlayers/lib/OpenLayers/Control/ModifyFeature.js', 'src/vendor/openlayers/lib/OpenLayers/Control/TouchNavigation.js', 'src/vendor/openlayers/lib/OpenLayers/Control/Navigation.js', 'src/vendor/openlayers/lib/OpenLayers/Control/SelectFeature.js','src/vendor/openlayers/lib/OpenLayers/Control/Zoom.js', 'src/vendor/openlayers/lib/OpenLayers/Control/Attribution.js', 'src/vendor/openlayers/lib/OpenLayers/Control/DrawFeature.js', 'src/vendor/openlayers/lib/OpenLayers/Control/TransformFeature.js', 'src/vendor/openlayers/lib/OpenLayers/Handler/RegularPolygon.js', 'src/vendor/openlayers/lib/OpenLayers/Handler/Polygon.js', 'src/vendor/openlayers/lib/OpenLayers/Layer/TMS.js', 'src/vendor/openlayers/lib/OpenLayers/Layer/Vector.js', 'src/vendor/openlayers/lib/OpenLayers/Layer/Markers.js', 'src/vendor/openlayers/lib/OpenLayers/Filter/Function.js', 'src/vendor/openlayers/lib/OpenLayers/Renderer/SVG.js', 'src/vendor/openlayers/lib/OpenLayers/Renderer/VML.js', 'src/vendor/openlayers/lib/OpenLayers/Renderer/Canvas.js', 'src/vendor/openlayers/lib/OpenLayers/Protocol/HTTP.js', 'src/vendor/openlayers/lib/OpenLayers/Strategy/Fixed.js', 'src/vendor/openlayers/lib/OpenLayers/TileManager.js', 'src/vendor/openlayers/lib/OpenLayers/Format/WKT.js', 'src/vendor/ol-loadingPanel/index.js']
        }
      }
    },
    watch: {
      less: {
        files: 'src/less/*',
        tasks: ['less']
      },
      requirejs: {
        files: 'src/scripts/main.js',
        tasks: ['copy:requirejs']
      }
    },
    concurrent: {
      watch: {
        tasks: ['watch:less', 'watch:requirejs'],
        options: {
          logConcurrentOutput: true
        }
      }
    },
    copy: {
      requirejs: {
        src: 'src/scripts/main.js',
        dest: 'src/scripts/main-out.js'
      }
    },
    clean: {
      test: ['test-compiled'],
      prep: ['src/css']
    }
  });
  grunt.registerTask('prep', ['clean', 'combine_harvester:openlayers']);
  grunt.registerTask('test', ['clean:test', 'coffee', 'jasmine']);
  grunt.registerTask('develop', ['less', 'copy:requirejs', 'concurrent:watch']);
  grunt.registerTask('build', ['clean', 'less', 'cssmin', 'requirejs']);
  grunt.registerTask('default', ['prep', 'build']);
};