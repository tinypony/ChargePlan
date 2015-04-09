require.config({
  paths : {
    jquery : 'lib/jquery-2.1.1',
    'jquery-ui' : 'lib/jquery-ui',
    underscore : 'lib/underscore',
    fileupload : 'lib/jquery.fileupload',
    knob : 'lib/jquery.knob',
    sidebar: 'lib/jquery-plugins/jquery.slidereveal.min',
    'jquery.ui.widget': 'lib/jquery.ui.widget',
    amcharts : 'lib/amcharts/amcharts',
    'amcharts.funnel' : 'lib/amcharts/funnel',
    'amcharts.gauge' : 'lib/amcharts/gauge',
    'amcharts.pie' : 'lib/amcharts/pie',
    'amcharts.radar' : 'lib/amcharts/radar',
    'amcharts.serial' : 'lib/amcharts/serial',
    'amcharts.xy' : 'lib/amcharts/xy',
    backbone : 'lib/backbone/backbone',
    chroma: 'lib/chroma.min',
    hbs : 'lib/hbs',
    scroller: 'lib/jquery.nanoscroller.min',
    Handlebars : 'lib/hbs/handlebars',
    moment : 'lib/moment.min',
    d3 : 'lib/d3.min',
    mapbox : 'lib/mapbox',
    bootstrap :  '//netdna.bootstrapcdn.com/bootstrap/3.1.1/js/bootstrap.min',
    datatables : 'lib/datatables/jquery.dataTables'
  },

  shim : {
    'jquery-ui' : {
      deps : [ 'jquery' ],
      exports : '$'
    },
    
    scroller: {
      deps: ['jquery'],
      exports: '$'
    },
    
    sidebar: {
    	deps: ['jquery'],
    	exports: '$'
    },

    bootstrap : {
      deps : [ 'jquery' ]
    },
    
    datatables: {
      deps: ['jquery']
    },

    mapbox : {
      exports : 'L'
    },
    

    'amcharts.funnel' : {
      deps : [ 'amcharts' ],
      exports : 'AmCharts',
      init : function() {
        AmCharts.isReady = true;
      }
    },
    
    'amcharts.gauge' : {
      deps : [ 'amcharts' ],
      exports : 'AmCharts',
      init : function() {
        AmCharts.isReady = true;
      }
    },
    'amcharts.pie' : {
      deps : [ 'amcharts' ],
      exports : 'AmCharts',
      init : function() {
        AmCharts.isReady = true;
      }
    },
    'amcharts.radar' : {
      deps : [ 'amcharts' ],
      exports : 'AmCharts',
      init : function() {
        AmCharts.isReady = true;
      }
    },
    'amcharts.serial' : {
      deps : [ 'amcharts' ],
      exports : 'AmCharts',
      init : function() {
        AmCharts.isReady = true;
      }
    },
    'amcharts.xy' : {
      deps : [ 'amcharts' ],
      exports : 'AmCharts',
      init : function() {
        AmCharts.isReady = true;
      }
    },

    backbone : {
      deps : [ 'underscore', 'jquery' ],
      exports : 'Backbone'
    }
  },
  locale : "en_us",
  // default plugin settings, listing here just as a reference
  hbs : {
    templateExtension : 'html',
    disableI18n : false
  }
});

require([ 'app', 'backbone', 'router', 'jquery-ui', 'moment', 'config-manager' ], 
		function(App, Backbone, router, JUI, moment, ConfigManager) {
	
	App.init();
});