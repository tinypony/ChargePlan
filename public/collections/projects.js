define(['backbone', 'models/project-model'], function(Backbone, ProjectModel) {
	var Projects = Backbone.Collection.extend({
		model: ProjectModel,
		url: '/api/projects'
	});
	
	return Projects;
});