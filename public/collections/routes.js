define(['backbone', 'models/project-model'], function(Backbone, ProjectModel) {
  var Routes = Backbone.Collection.extend({
    model: ProjectModel,
    url: '/api/projects'
  });
  
  return Routes;
});