var frostApp = angular.module("frost", [ "ui.router" ]);

frostApp.config(function($stateProvider, $urlRouterProvider) {
	$urlRouterProvider.when("", "/executors");
	$stateProvider.state("executors", {
		url : "/executors",
		templateUrl : "executors.html",
		controller : executorsController
	}).state("jobs", {
		url : "/jobs",
		templateUrl : "jobs.html",
		controller : jobsController
	}).state("logs", {
		url : "/logs",
		templateUrl : "logs.html",
		controller : logsController
	});
});

function executorsController($scope, $http) {
	$scope.toggleExpandAll = function() {
		$scope.expandAll = !$scope.expandAll;
	};

	$scope.queryExecutors = function() {
		$http.get('queryExecutors').success(function(data) {
			if (data.code === "00000") {
				$scope.executorList = data.data;
				$scope.executorList.forEach(r => {
					
				});
			}
		});
	};

	$scope.queryExecutors()

	$scope.executors = [ {
		group : {
			groupKey : "frost-executor1",
			count : 2
		},
		apps : [ {
			groupKey : "",
			name : "e1-1"
		}, {
			groupKey : "",
			name : "e1-2"
		} ]
	}, {
		group : {
			groupKey : "frost-executor2",
			count : 1
		},
		apps : [ {
			groupKey : "frost-executor2",
			name : "e2"
		} ]
	} ];
}

function jobsController($scope) {
	console.log('jobsController');
}

function logsController($scope) {
	console.log('logsController');
}