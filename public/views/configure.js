define([ 'jquery', 'underscore', 'backbone', 'hbs!templates/configure', 'knob', 'fileupload'], 
    function($, _, Backbone, template) {

  var ConfigurationView = Backbone.View.extend({
    initialize: function(options) {
      this.connectToMonitor();
    },
    
    connectToMonitor: function() {
      var self = this;
      this.connection = new WebSocket('ws://'+window.location.hostname+':'+window.location.port+'/ws/jobmonitor');
  
      
      this.connection.onmessage = function(msg) {
        var message = JSON.parse(msg.data);
        console.log(message);
        
        if(message.messageType === 'newJob' && message.job.type === 'ScheduleImport') {
          self.monitorJob(message.job.id);
        } else if(message.messageType === 'activeJobs') {
          _.each(message.jobs, function(job) {
            console.log(job);
            if(job.type === 'ScheduleImport') {
              self.monitorJob(job.id);
            }
          });
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
    
    monitorJob: function(jobId) {
      this.connection.send(JSON.stringify({
        action: 'monitor',
        jobs: [jobId]
      }));
    },
    
    render: function() {
      this.$el.html(template());
      
      var ul = $('#upload ul');

      $('#drop a').click(function(){
          // Simulate a click on the file input button
          // to show the file browser dialog
          $(this).parent().find('input').click();
      });

      // Initialize the jQuery File Upload plugin
      $('#upload').fileupload({

          // This element will accept file drag/drop uploading
          dropZone: $('#drop'),

          // This function is called when a file is added to the queue;
          // either via the browse button, or via drag/drop:
          add: function (e, data) {

              var tpl = $('<li class="working"><input type="text" value="0" data-width="48" data-height="48"'+
                  ' data-fgColor="#0788a5" data-readOnly="1" data-bgColor="#3e4043" /><p></p><span></span></li>');

              // Append the file name and file size
              tpl.find('p').text(data.files[0].name)
                           .append('<i>' + formatFileSize(data.files[0].size) + '</i>');

              // Add the HTML to the UL element
              data.context = tpl.appendTo(ul);

              // Initialize the knob plugin
              tpl.find('input').knob();

              // Listen for clicks on the cancel icon
              tpl.find('span').click(function(){

                  if(tpl.hasClass('working')){
                      jqXHR.abort();
                  }

                  tpl.fadeOut(function(){
                      tpl.remove();
                  });

              });

              // Automatically upload the file once it is added to the queue
              var jqXHR = data.submit();
          },

          progress: function(e, data){

              // Calculate the completion percentage of the upload
              var progress = parseInt(data.loaded / data.total * 100, 10);

              // Update the hidden input field and trigger a change
              // so that the jQuery knob plugin knows to update the dial
              data.context.find('input').val(progress).change();

              if(progress == 100){
                  data.context.removeClass('working');
              }
          },

          fail: function(e, data){
              // Something has gone wrong!
              data.context.addClass('error');
          }

      });


      // Prevent the default action when a file is dropped on the window
      $(document).on('drop dragover', function (e) {
          e.preventDefault();
      });

      // Helper function that formats the file sizes
      function formatFileSize(bytes) {
          if (typeof bytes !== 'number') {
              return '';
          }

          if (bytes >= 1000000000) {
              return (bytes / 1000000000).toFixed(2) + ' GB';
          }

          if (bytes >= 1000000) {
              return (bytes / 1000000).toFixed(2) + ' MB';
          }

          return (bytes / 1000).toFixed(2) + ' KB';
      }

    }
    
  });
  
  return ConfigurationView;
})