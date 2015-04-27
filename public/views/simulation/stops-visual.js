define([ 'jquery', 
         'underscore', 
         'backbone',
         'config-manager',
         'd3', 
         'views/simulation/stop-details' ], 
    function($, _, Backbone, ConfigManager, d3, StopDetails) {

  var RouteVisualizationView = Backbone.View.extend({
    
    initialize : function(options) {
      this.route = options.route;
      this.project = options.project;
      this.chargers = options.chargers;
      this.firstTime = true;
      
      this.visual = {};
      _.bindAll(this, ['onStopClick']);
      
      this.listenTo(this.project, 'sync', this.render);
    },

    // https://www.domsammut.com/projects/pure-css-loading-animation
    determineTotalHeight : function() {
      return 200;
    },

    isCircular : function() {
      return _.first(this.route.trips[0].stops).stopId === _.last(this.route.trips[0].stops).stopId;
    },

    getTrip : function() {
      if (this.route.trips[0]) {
        return this.route.trips[0];
      } else {
        this.route.trips[1];
      }
    },

    getDirection0Trip : function() {
      return _.findWhere(this.route.trips, {
        direction : '0'
      });
    },

    getDirection1Trip : function() {
      return _.findWhere(this.route.trips, {
        direction : '1'
      });
    },

    drawCircular : function() {

    },

    isFirstSame : function(stops0, stops1) {
      return _.first(stops0).stopId === _.first(stops1).stopId;
    },

    isLastSame : function(stops0, stops1) {
      return _.last(stops0).stopId === _.last(stops1).stopId;
    },

    onStopClick : function(scheduleStop) {
      var self = this;
      electrifiedStop = _.findWhere(this.project.get('stops'), {stopId: scheduleStop.stop.stopId});
      
      if(!electrifiedStop) {
        electrifiedStop = scheduleStop.stop;
        electrifiedStop.chargers = [];
      }
      
      var stopDetails = new StopDetails({
        stop: electrifiedStop,
        chargers: this.chargers,
        project: this.project,
        routeId: this.route.routeId,
        isEndStop: scheduleStop.endstop
      });
      
      stopDetails.render();
      this.listenTo(stopDetails, 'close', function(){
        stopDetails.remove();
        self.stopListening(stopDetails);
      });
    },

    drawBidirectional : function() {
      var self = this;
      var trip0 = this.getDirection0Trip();
      var trip1 = this.getDirection1Trip();
      var endStopOffset = 50;
      var sideOffset = endStopOffset * 2;

      var stops0 = trip0.stops;
      var stops1 = trip1.stops;
      stops0[0].endstop = true;
      stops0[stops0.length-1].endstop = true;
      stops1[0].endstop = true;
      stops1[stops1.length-1].endstop = true;
      
      if(this.firstTime) {
        stops1.reverse();
        this.firstTime = false;
      }
      
      var x0 = d3.scale.linear().domain([ 0, stops0.length ]).range([ 0, this.visual.width - this.visual.offset.right - this.visual.offset.left ]);
      this.visual.x0 = x0;
      var x1 = d3.scale.linear().domain([ 0, stops1.length ]).range([ 0, this.visual.width - this.visual.offset.right - this.visual.offset.left ]);
      this.visual.x0 = x1;

      direction0 = this.visual.svg.append('g');
      direction1 = this.visual.svg.append('g').attr('transform', 'translate(0,' + sideOffset + ')');
      
      var getColor = function(d) {
        var elStop = _.findWhere(self.project.get('stops'), {stopId: d.stopId}); 
        if(elStop && elStop.charger) {
          return 'green';
        }
        return 'steelblue';
      };

      var circles0 = direction0.selectAll('circle').data(stops0).enter().append('circle');

      circles0.attr('cx', function(d, i) {
        return self.visual.offset.left + x0(i);
      }).attr('cy', function(d, i) {
        if ((i === 0 && self.isFirstSame(stops0, stops1)) || (i === stops0.length - 1 && self.isLastSame(stops0, stops1))) {
          return self.visual.offset.top + endStopOffset;
        } else {
          return self.visual.offset.top;
        }
      }).attr('r', function(d) {
        if (d.stop.first || d.stop.last) {
          return 15;
        } else {
          return 6;
        }
      }).on('click', this.onStopClick).style('fill', getColor)
      .attr('data-toggle', 'popover').append("svg:title").text(function(d, i) {
        return d.stop.name
      });

      var circles1 = direction1.selectAll('circle').data(stops1).enter().append('circle');

      circles1.attr('cx', function(d, i) {
        return self.visual.offset.left + x1(i);
      }).attr('cy', function(d, i) {
        if ((i === 0 && self.isFirstSame(stops0, stops1)) || (i === stops1.length - 1 && self.isLastSame(stops0, stops1))) {
          return self.visual.offset.top + endStopOffset;
        } else {
          return self.visual.offset.top;
        }
      }).attr('r', function(d, i) {
        if ((i === 0 && self.isFirstSame(stops0, stops1)) || (i === stops1.length - 1 && self.isLastSame(stops0, stops1))) {
          return 0;
        } else {
          if (d.stop.first || d.stop.last) {
            return 15;
          } else {
            return 6;
          }
        }
      }).on('click', this.onStopClick).style('fill', getColor).append("svg:title").text(function(d, i) {
        return d.stop.name
      });
    },

    drawStops : function() {
      var self = this;

      if (!this.isCircular()) {
        this.drawBidirectional();
      }
    },

    render : function() {
      this.$el.empty();
      var offset = {
        left : 70,
        right : 10,
        top : 70,
        bottom : 20
      };

      var width = this.$el.width();
      var svg = d3.select(this.el).append('svg').attr('width', '100%').attr('height', this.determineTotalHeight());
      this.visual.svg = svg;
      this.visual.offset = offset;
      this.visual.width = width;

      this.drawStops();
      $('circle[data-toggle="popover"]').popover({
        html : '<h2>Hop hei</h2>'
      });
    }
  });

  return RouteVisualizationView;
});