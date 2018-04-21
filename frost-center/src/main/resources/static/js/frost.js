var frostApp = angular.module("frost", [ "ui.router",'ui.bootstrap' ]);

frostApp.config(function($stateProvider, $urlRouterProvider, $locationProvider) {
	$locationProvider.html5Mode(false); 
	$urlRouterProvider.otherwise("/executors");
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
		params : {
			executorId: null,
			jobId: null
		},
		templateUrl : "logs.html",
		controller : logsController
	});
});

frostApp.controller('appModalInstanceCtrl', function ($scope, $uibModalInstance, modalDatas) {
    var $ctrl = this;
    // 双向绑定，方便在确认中回传可能修改的字段
    $scope.modalDatas = modalDatas; 

    $ctrl.ok = function (val) {
    	if ($scope.modalDatas.ok && !$scope.modalDatas.ok()) {
    		return;
    	}
    	// 在模态框View中修改的值传递回去，view中可以直接添加属性
    	$uibModalInstance.close($scope.modalDatas);
    };
    
    $ctrl.cancel = function () {
    	$uibModalInstance.dismiss('cancel');
    };
    
    $ctrl.executorChange = function() {
		if($scope.modalDatas.executorId){
			var executor = $scope.modalDatas.executorMap.get($scope.modalDatas.executorId);
			$scope.modalDatas.jobs = executor.groups;
			if(executor.groups && executor.groups.length > 0){
				$scope.modalDatas.jobKey = executor.groups[0].jobKey;
			}
		}
	}
  });

function executorsController($rootScope, $scope, $http, $filter, $state) {
	
	$scope.searchFilter = '';
	$rootScope.navActive = 0;
	 
	$scope.toggleExpandAll = function() {
		$scope.expandAll = !$scope.expandAll;
		$scope.executors.forEach(r => r.group.collapsed = !$scope.expandAll);
	};

	$scope.queryExecutors = function() {
		$http.get('queryExecutors').success(function(data) {
			if (data.success) {
				$scope.executorList = data.data;
				var map = new Map();
				var rs = [];
				$scope.executorList.forEach(r => {
					if(!map.has(r.key)) {
						map.set(r.key, []);
					}
					map.get(r.key).push(r);
				});
				map.forEach((v,k) => {
					rs.push({
						group: { 
							groupKey: k,
							count: v.length
						},
						apps: v
					});
				});
				$scope.executors = rs;
			}
		});
	};

	$scope.queryExecutors();
	
	$scope.doFilter = function (value) {
	    if (!$scope.searchFilter) {
	      return true;
	    }
	    var projection = angular.copy(value);
	    return $filter('filter')([projection], $scope.searchFilter).length > 0;
	};
	
	$scope.jumpToLogs = function (id) {
		$state.go("logs", {executorId: id});
	}
}

function jobsController($rootScope, $scope, $http, $filter, $uibModal, $state) {

	$scope.searchFilter = '';
	$rootScope.navActive = 1;
	 
	$scope.queryJobs = function() {
		$http.get('queryJobInfos').success(function(data) {
			if (data.success) {
				$scope.jobs = data.data;
			}
		});
	};

	$scope.queryJobs();
	
	$scope.doFilter = function (value) {
	    if (!$scope.searchFilter) {
	      return true;
	    }
	    var projection = angular.copy(value);
	    delete projection.logId;
	    delete projection.taskId;
	    return $filter('filter')([projection], $scope.searchFilter).length > 0;
	};
	
	$scope.addJob = function() {
		
		$scope.modalDatas = {};
		
		$http.get('queryExecutors').success(function(data) {
			if (data.success) {
				$scope.modalDatas.executorList = data.data;
				var map = new Map();
				data.data.forEach(r => map.set(r.id, r));
				$scope.modalDatas.executorMap = map;
				
				if(data.data.length > 0){
					$scope.modalDatas.executorId = data.data[0].id;
					$scope.modalDatas.jobs = data.data[0].groups;
					if (data.data[0].groups && data.data[0].groups.length >　0){
						$scope.modalDatas.jobKey = data.data[0].groups[0].jobKey;
					}
				}
			}
		});
		
		var modal = $uibModal.open({
		    animation: true,
		    ariaLabelledBy: 'modal-title',
		    ariaDescribedBy: 'modal-body',
			templateUrl: "addJob.html",
			controller: 'appModalInstanceCtrl',
			controllerAs: '$ctrl',
		    resolve: {
		      modalDatas: function () {
		        return $scope.modalDatas;
		      }
		    }
		});
		
		modal.result.then(function(data) {
			 delete $scope.modalDatas.error;
		});
		
		$scope.modalDatas.ok = function () {
			var job = {
				 name: $scope.modalDatas.name,
				 cron: $scope.modalDatas.cron,
				 group: {
					 jobKey: $scope.modalDatas.jobKey,
					 groupKey: $scope.modalDatas.executorMap.get($scope.modalDatas.executorId).key
				 }
			 };
			 var flag = true;
			 $http.post('addJob', job, {async: false}).success(function(data){
				 if (data.success) {
					 $scope.queryJobs();
				 } else {
					 $scope.modalDatas.error = data.message;
					 flag = false;
				 }
			 });
			 return flag;
		};
	};
	
	$scope.triggleJob = function (id) {
		openConfirm($scope, $uibModal, "确定执行?", function(data) {
			var flag = true;
			$http.post('triggerJob', {id: id}, {async: false, params: {id: id}}).success(function(resp){
				if (resp.success) {
					$scope.queryJobs();
				} else {
					$scope.modalDatas.error = resp.message;
					flag = false;
				}
			});
			return flag;
		});
	};
	
	$scope.pauseJob = function (id) {
		openConfirm($scope, $uibModal, "确定暂停?", function(data) {
			var flag = true;
			$http.post('pauseJob', {id: id}, {async: false, params: {id: id}}).success(function(resp){
				 if (resp.success) {
					 $scope.queryJobs();
				 } else {
					 $scope.modalDatas.error = resp.message;
					 flag = false;
				 }
			 });
			return flag;
		});
	};
	
	$scope.resumeJob = function (id) {
		openConfirm($scope, $uibModal, "确定恢复?", function(data) {
			var flag = true;
			$http.post('resumeJob', {id: id}, {async: false, params: {id: id}}).success(function(resp){
				if (resp.success) {
					$scope.queryJobs();
				} else {
					$scope.modalDatas.error = resp.message;
					flag = false;
				}
			});
			return flag;
		});
	};
	
	$scope.removeJob = function (id) {
		openConfirm($scope, $uibModal, "确定删除?", function(data) {
			var flag = true;
			$http.post('removeJob', {id: id}, {async: false, params: {id: id}}).success(function(resp){
				if (resp.success) {
					$scope.queryJobs();
				} else {
					$scope.modalDatas.error = resp.message;
					flag = false;
				}
			});
			return flag;
		});
	};
	
	$scope.jumpToLogs = function (id) {
		$state.go("logs", {jobId: id});
	};
}


function logsController($rootScope, $scope, $http, $stateParams, $filter) {
	
	$rootScope.navActive = 2;
	$scope.from = 0;
	$scope.to = 100;
	
	
	$scope.search = function() {
		var jobKey = '', groupKey = '';
		if ($scope.executorId){
			groupKey = $scope.executorMap.get($scope.executorId).key;
			if ($scope.jobKey) {
				jobKey = $scope.jobKey;
			}
		}
		var params = {
			jobId: $scope.jobId, 
			from: $scope.from, 
			to: $scope.to,
			groupKey: groupKey,
			jobKey: jobKey
		};
		$http.post('queryJobExecuteRecords', null, {async: false, params: params}).success(function(data) {
			if (data.success) {
				$scope.logs = data.data;
			}
		});
	};
	
	$scope.doFilter = function (value) {
	    if (!$scope.searchFilter) {
	      return true;
	    }
	    var projection = angular.copy(value);
	    delete projection.id;
	    return $filter('filter')([projection], $scope.searchFilter).length > 0;
	};
	
	$scope.filterJobs = function (value) {
		if ($scope.groupKey && value.group.groupKey != $scope.groupKey) {
			return false;
		}
		if ($scope.jobKey && value.group.jobKey != $scope.jobKey) {
			return false;
		}
		return true;
	}
	
	$http.get('queryExecutors').success(function(data) {
		if (data.success) {
			$scope.executorList = data.data;
			var map = new Map();
			data.data.forEach(r => map.set(r.id, r));
			$scope.executorMap = map;
			$scope.executorId = $stateParams.executorId;
		}
		$http.get('queryJobInfos').success(function(data) {
			if (data.success) {
				$scope.jobInfos = data.data;
			}
			$scope.jobId = $stateParams.jobId;
			$scope.search();
		});
	});
	
	
	$scope.executorChange = function() {
		if($scope.executorId){
			var executor = $scope.executorMap.get($scope.executorId);
			$scope.jobs = executor.groups;
		}
	}
	
}

function openConfirm($scope, $uibModal, msg, ok) {
	$scope.modalDatas = {
		title: "提示信息",
		msg: msg,
		ok: ok
	};
	var modal = $uibModal.open({
	    animation: true,
	    ariaLabelledBy: 'modal-title',
	    ariaDescribedBy: 'modal-body',
		templateUrl: "modal.html",
		controller: 'appModalInstanceCtrl',
		controllerAs: '$ctrl',
		windowClass: 'modal-confirm',
	    resolve: {
	      modalDatas: function () {
	        return $scope.modalDatas;
	      }
	    }
	});
	
	modal.result.then(function(data) {
		 delete $scope.modalDatas.error;
	});
	
}

