define([ 'jquery', 
         'underscore', 
         'backbone', 
         'views/configure/schedule-import', 
         'hbs!templates/configure'], 
    function($, _, Backbone, ScheduleImportView, template) {

  var ConfigurationView = Backbone.View.extend({
    
    initialize: function(options) {
      this.monitoring = [];
      this.retrieveConfig();
      this.connectToMonitor();
    },
    
    retrieveConfig: function() {
      var self = this;
      $.get('/api/configuration').done(function(data) {
        self.config = data;
        self.render();
      });
    },
    
    connectToMonitor: function() {
      var self = this;
      this.connection = new WebSocket('ws://'+window.location.hostname+':'+window.location.port+'/ws/jobmonitor');
  
      
      this.connection.onmessage = function(msg) {
        var message = JSON.parse(msg.data);
        console.log(message);
        
        if(message.messageType === 'newJob' && message.job.type === 'ScheduleImport') {
          self.monitorJob(message.job);
        } else if(message.messageType === 'activeJobs') {
          _.each(message.jobs, function(job) {
            console.log(job);
            
            if(job.type === 'ScheduleImport') {
              self.monitorJob(job);
            }
          });
        } else if(message.messageType == 'jobProgress') {
          self.scheduleImport.update(message.job);
        } else {
          console.log('Unhandled message');
        }
      };
      
      this.connection.onopen = function() {
        self.connection.send(JSON.stringify({
          action: 'listJobs'
        }));
      };

      // Log errors
      this.connection.onerror = function (error) {
        console.log('WebSocket Error ' + error);
      };
    },
    
    monitorJob: function(job) {
      this.connection.send(JSON.stringify({
        action: 'monitor',
        jobs: [job.id]
      }));
      this.monitoring.push(job);
    },
    
    render: function() {
      var self = this;
      this.$el.html(template());
      this.scheduleImport = new ScheduleImportView({
        el: this.$('.schedule-import-container'),
        history: this.config.schedules
      });
      this.scheduleImport.render();
      _.each(this.monitoring, function(job){
        self.scheduleImport.update(job);
      });
    }
  });
  
  return ConfigurationView;
})