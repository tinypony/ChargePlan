define([ 'jquery','models/project-model', 'models/config-model'], function($, ProjectModel, ConfigModel){
	var ConfigManager = function() {
		this.model = new ConfigModel();
		this.model.fetch({async: false});
	};
	
	ConfigManager.prototype.getProject = function() {
		var self = this;
		if(this.project) {
			var promise = $.Deferred();
			promise.resolve(this.project);
			return promise;
		} else if(this.promise) {
			return this.promise;
		} else {
			this.project = new ProjectModel({id: this.model.get('recentProject')});
			this.promise = $.Deferred();
			this.project.fetch({silent:true, success: function(model){
			  self.promise.resolve(model);
			  self.promise = null;
			}});
			return this.promise;
		}
		
	}
	
	return new ConfigManager();
});