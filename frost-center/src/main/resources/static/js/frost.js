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
	}).state("script", {
		url : "/script",
		params : {
			jobId: null
		},
		templateUrl : "script.html",
		controller : scriptController
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
	};
    
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
	
	$scope.defaultScript = 
`package justlive.earth.breeze.frost.executor.example;
 
import java.util.Random;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import justlive.earth.breeze.frost.core.job.IJob;
import justlive.earth.breeze.frost.core.job.Job;
import justlive.earth.breeze.frost.core.job.JobContext;

 
@Job(value = "DemoScriptJob", desc = "示例脚本job")
public class DemoScriptJob implements IJob {
 
 @Autowired
 InjectExampleBean bean;
 
 @Override
 public void execute(JobContext ctx) {
   System.out.println(String.format("参数：%s", ctx.getParam()));
   bean.say();
 }
 
}`;
	 
	$scope.pageIndex = 1;
	$scope.pageSize = 10;
	
	$scope.queryJobs = function() {
		postForm('queryJobInfos', {pageIndex: $scope.pageIndex, pageSize: $scope.pageSize}, function(data) {
			if (data.success) {
				$scope.totalCount = data.data.totalCount;
				$scope.jobs = data.data.items;
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
		
		$scope.modalDatas = { opt: 1};
		$scope.modalDatas.type = 'BEAN';
		$scope.modalDatas.failStrategy = 'NOTIFY';
		
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
			windowClass: 'modal-addJob',
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
			var mails = null;
			if($scope.modalDatas.notifyMails) {
				mails = $scope.modalDatas.notifyMails.split(',');
			}
			var job = {
				 name: $scope.modalDatas.name,
				 cron: $scope.modalDatas.cron,
				 type: $scope.modalDatas.type,
				 param: $scope.modalDatas.param,
				 auto: $scope.modalDatas.auto,
				 failStrategy: $scope.modalDatas.failStrategy,
				 notifyMails: mails
			 };
			 var flag = true;
			 if($scope.modalDatas.type == 'SCRIPT'){
				 job.script = $scope.defaultScript;
				 
				 if ($scope.modalDatas.useExecutor) {
					 job.group = {
						 groupKey: $scope.modalDatas.executorMap.get($scope.modalDatas.executorId).key
					 };
				 }
				 
			 } else {
				 job.group = {
					 jobKey: $scope.modalDatas.jobKey,
					 groupKey: $scope.modalDatas.executorMap.get($scope.modalDatas.executorId).key
				 };
			 }
			 //
			 postJson('addJob', job, function(data){
					 if (data.success) {
						 $scope.queryJobs();
					 } else {
						 $scope.modalDatas.error = data.message;
						 flag = false;
					 }
				 },
				 function(req, textStatus, errorThrown){
					 $scope.modalDatas.error = req.responseJSON.message;
					 flag = false;
				 });
			 
			 return flag;
		};
	};
	
	$scope.triggleJob = function (id) {
		openConfirm($scope, $uibModal, "确定执行?", function(data) {
			var flag = true;
			
			postForm('triggerJob', {id: id}, function(resp){
				 if (resp.success) {
					$scope.queryJobs();
				 } else {
					$scope.modalDatas.error = resp.message;
					flag = false;
				 }
			 },
			 function(XMLHttpRequest, textStatus, errorThrown){
				 $scope.modalDatas.error = errorThrown;
			 });
			
			return flag;
		});
	};
	
	$scope.pauseJob = function (id) {
		openConfirm($scope, $uibModal, "确定暂停?", function(data) {
			var flag = true;
			postForm('pauseJob', {id: id}, function(resp){
				 if (resp.success) {
					$scope.queryJobs();
				 } else {
					$scope.modalDatas.error = resp.message;
					flag = false;
				 }
			 },
			 function(XMLHttpRequest, textStatus, errorThrown){
				 $scope.modalDatas.error = errorThrown;
			 });
			return flag;
		});
	};
	
	$scope.resumeJob = function (id) {
		openConfirm($scope, $uibModal, "确定恢复?", function(data) {
			var flag = true;
			postForm('resumeJob', {id: id}, function(resp){
				 if (resp.success) {
					$scope.queryJobs();
				 } else {
					$scope.modalDatas.error = resp.message;
					flag = false;
				 }
			 },
			 function(XMLHttpRequest, textStatus, errorThrown){
				 $scope.modalDatas.error = errorThrown;
			 });
			return flag;
		});
	};
	
	$scope.removeJob = function (id) {
		openConfirm($scope, $uibModal, "确定删除?", function(data) {
			var flag = true;
			postForm('removeJob', {id: id}, function(resp){
				 if (resp.success) {
					$scope.queryJobs();
				 } else {
					$scope.modalDatas.error = resp.message;
					flag = false;
				 }
			 },
			 function(XMLHttpRequest, textStatus, errorThrown){
				 $scope.modalDatas.error = errorThrown;
			 });
			return flag;
		});
	};
	
	$scope.jumpToLogs = function (id) {
		$state.go("logs", {jobId: id});
	};
	
	$scope.jumpToScript = function (id) {
		$state.go("script", {jobId: id});
	};
	
	$scope.updateJob = function(id) {
		
		$scope.modalDatas = {};
		
		postForm('queryExecutors', null, function(data) {
			if (data.success) {
				$scope.modalDatas.executorList = data.data;
				var map = new Map(), groupMap = new Map();
				data.data.forEach(r => {
					map.set(r.id, r);
					groupMap.set(r.key, r);
				});
				$scope.modalDatas.executorMap = map;
				$scope.modalDatas.executorGroupMap = groupMap;
				
				if(data.data.length > 0){
					$scope.modalDatas.executorId = data.data[0].id;
					$scope.modalDatas.jobs = data.data[0].groups;
					if (data.data[0].groups && data.data[0].groups.length >　0){
						$scope.modalDatas.jobKey = data.data[0].groups[0].jobKey;
					}
				}
			}
		});
		
		postForm('findJobInfoById', {id: id}, function(data) {
			if (data.success && data.data) {
				var mails = null;
				if (data.data.notifyMails) {
					mails = data.data.notifyMails.join();
				}
				$scope.modalDatas.type = data.data.type;
				$scope.modalDatas.preType = data.data.type;
				$scope.modalDatas.cron = data.data.cron;
				$scope.modalDatas.name = data.data.name;
				$scope.modalDatas.param = data.data.param;
				$scope.modalDatas.failStrategy = data.data.failStrategy;
				$scope.modalDatas.notifyMails = mails;
				if (data.data.group) {
					var executor = $scope.modalDatas.executorGroupMap.get(data.data.group.groupKey);
					if(executor){
						$scope.modalDatas.executorId = executor.id;
					}
					if ($scope.modalDatas.type == 'SCRIPT') {
						$scope.modalDatas.useExecutor = true;
					} else {
						$scope.modalDatas.jobKey = data.data.group.jobKey;
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
			windowClass: 'modal-addJob',
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
			var mails = null;
			if($scope.modalDatas.notifyMails) {
				mails = $scope.modalDatas.notifyMails.split(',');
			}
			var job = {
				 id: id,
				 name: $scope.modalDatas.name,
				 cron: $scope.modalDatas.cron,
				 type: $scope.modalDatas.type,
				 param: $scope.modalDatas.param,
				 failStrategy: $scope.modalDatas.failStrategy,
				 notifyMails: mails
			 };
			 if (job.type == 'SCRIPT') {
				 if ($scope.modalDatas.useExecutor) {
					 job.group = {
						 groupKey: $scope.modalDatas.executorMap.get($scope.modalDatas.executorId).key
					 };
				 } 
				 if ($scope.modalDatas.preType != job.type) {
					 job.script = $scope.defaultScript;
				 }
			 } else {
				 job.group = {
					 jobKey: $scope.modalDatas.jobKey,
					 groupKey: $scope.modalDatas.executorMap.get($scope.modalDatas.executorId).key
				 };
			 }
			 var flag = true;
			 postJson('updateJob', job, function(data){
					 if (data.success) {
						 $scope.queryJobs();
					 } else {
						 $scope.modalDatas.error = data.message;
						 flag = false;
					 }
				 },
				 function(XMLHttpRequest, textStatus, errorThrown){
					 $scope.modalDatas.error = errorThrown;
				 });
			 
			 return flag;
		};
	};
	
}


function logsController($rootScope, $scope, $http, $stateParams, $filter, $sce) {
	
	$rootScope.navActive = 2;
	
	$scope.pageIndex = 1;
	$scope.pageSize = 10;
	
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
			pageIndex: $scope.pageIndex, 
			pageSize: $scope.pageSize,
			groupKey: groupKey,
			jobKey: jobKey
		};
		$http.post('queryJobExecuteRecords', null, {params: params}).success(function(data) {
			if (data.success) {
				$scope.totalCount = data.data.totalCount;
				$scope.logs = data.data.items;
				if($scope.logs) {
					$scope.logs.forEach(r => {
						$scope.popoverExecute(r);
					});
				}
			}
		});
	};
	
	$scope.popoverExecute = function (log) {
	
		var executeDetail = '', dispatchDetial = '';
		log.recordStatuses.forEach(r => {
			if (r.type == 0){
				dispatchDetial += `<div class="form-group" style="text-align: center;"><label class="status-UNKNOWN">>>>任务调度<<<</label><div>${r.msg}</div></div>`;
			} else if (r.type == 1) {
				executeDetail += `<div class="form-group" style="text-align: center;"><label class="status-UNKNOWN">>>>任务执行<<<</label><div>${r.msg}</div></div>`;
			} else if (r.type == 2) {
				dispatchDetial += `<div class="form-group" style="text-align: center;"><label class="status-FAIL">>>>失败重试<<<</label><div>${r.msg}</div></div>`;
			} else if (r.type == 3) {
				executeDetail += `<div class="form-group" style="text-align: center;"><label class="status-FAIL">>>>失败重试<<<</label><div>${r.msg}</div></div>`;
			}
		});
		log.dispatchDetail = $sce.trustAsHtml(dispatchDetial);
		log.executeDetail = $sce.trustAsHtml(executeDetail);
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
		if (!value.group && $scope.jobKey) {
			return false;
		}
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
		$http.get('queryAllJobs').success(function(data) {
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
	};
	
}

function scriptController($scope, $stateParams, $state, $uibModal) {
	
	var myTextarea = document.getElementById('editor');
	
	$scope.jobId = $stateParams.jobId;
	
	$scope.queryVersions = function () {
		postForm('queryJobScripts', {jobId: $stateParams.jobId}, function(data) {
			if (data.success && data.data) {
				$scope.scriptList = data.data;
				$scope.scriptMap = new Map();
				data.data.forEach(r => $scope.scriptMap.set(r.id, r));
			}
		});
	};
	
	postForm('findJobInfoById', {id: $stateParams.jobId}, function(data) {
		if (data.success && data.data) {
			myTextarea.value = data.data.script;
			$scope.queryVersions();
		}
	});
	
	$scope.codeMirrorEditor = CodeMirror.fromTextArea(myTextarea, {
	    mode: "text/x-java",
	    lineNumbers: true
	});
	
	$scope.switchScript = function (id) {
		var script = $scope.scriptMap.get(id);
		$scope.codeMirrorEditor.doc.setValue(script.script);
		$scope.version = script.version;
	};
	
	$scope.save = function () {
		if (!$scope.version) {
			$scope.versionError = true;
			return;
		}
		
		$scope.versionError = false;
		
		var script = $scope.codeMirrorEditor.doc.getValue();
		
		postJson("addJobScript", {jobId: $scope.jobId, script: script, version: $scope.version}, function(data){
			if(data.success){
				openConfirm($scope, $uibModal, data.data);
			} else {
				openConfirm($scope, $uibModal, data.message);
			}
			$scope.queryVersions();
		},
		function(XMLHttpRequest, textStatus, errorThrown){
			$scope.modalDatas.error = errorThrown;
		});
	};
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

function postJson(url, data, success, error) {
	 $.ajax({
		 url: url, 
		 data: JSON.stringify(data), 
		 async: false,
		 type: 'post',
		 contentType: 'application/json',
		 success: success,
		 error: error
	 });
}

function postForm(url, data, success, error) {
	$.ajax({
		 url: url, 
		 data: data, 
		 async: false,
		 type: 'post',
		 success: success,
		 error: error
	 });
}

