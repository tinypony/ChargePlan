define(['jquery', 
        'underscore', 
        'backbone', 
        'moment',
        'hbs!templates/configure/schedule-import', 
        'knob', 
        'fileupload'], 
    function($, _, Backbone, moment, template) {
  var ScheduleImportView = Backbone.View.extend({
    initialize: function(options){
      this.history = {};
      this.history.date = moment(options.history.date).format('MMMM Do YYYY, HH:mm');
      this.history.imported = options.history.imported;
    },
    
    update: function(job) {
      this.$('ul').empty();
      
      if(job.done) {
        this.enableUploader();
      } else {
        this.disableUploader();
      }
      
      for(var i=0; i<=job.state; i++) {
        if(i<job.state) {
          this.$('ul').append('<li class="state"><label class="step">'+job.states[i]+'</label><label class="state">OK</label></li>');
        } else if(i == job.state && !job.done) {
          this.$('ul').append('<li class="state"><label class="step">'+job.states[i]+'</label><label class="state">In progress</label></li>');
        } else {
          this.$('ul').append('<li class="state"><label class="step">'+job.states[i]+'</li>');
          this.$('.last-uploaded').text('Imported '+moment().format('MMMM Do YYYY, HH:mm'));
        }
      }
      
    },
    
    initUploader: function() {
      var self = this;
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

              var tpl = $('<li class="working state"><label class="step">Upload</label><label class="state upload-state"></label></li>');
              self.$('ul').append(tpl);
              data.context = tpl;
              // Automatically upload the file once it is added to the queue
              self.disableUploader();
              data.submit();
          },

          progress: function(e, data) {
              // Calculate the completion percentage of the upload
              var progress = parseInt(data.loaded / data.total * 100, 10);
              console.log(progress);
              if(progress == 100){
                $('.upload-state').text('OK');
              } else {
                var stateContainer = data.context;
                $('.upload-state').text(progress+'%');
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
    },
    
    disableUploader: function() {
      $('#drop a').off('click');
      this.$('#upload').fileupload('disable');
    },
    

    enableUploader : function() {
      $('#drop a').click(function() {
        // Simulate a click on the file input button
        // to show the file browser dialog
        $(this).parent().find('input').click();
      });
      this.$('#upload').fileupload('enable');
    },
    
    
    
    render: function(){
      this.$el.html(template({
        history: this.history
      }));
      this.initUploader();
    }
  });
  
  return ScheduleImportView;
});