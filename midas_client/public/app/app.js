/*#######################################################################
 Normally like to break AngularJS apps into the following folder structure
 at a minimum:

 /app
 /controllers
 /directives
 /services
 /partials
 /views

 #######################################################################*/

var urlBase = 'app/partials/';
var midasApp = angular.module('midasApp', ['ngRoute', 'ngTable', 'flotang',
    'ui.bootstrap', 'ui.select', 'nvd3',
    'formatFilters','DataService', 'UtilService', 'ui.grid']);

midasApp.config(['$routeProvider',
    function ($routeProvider) {
        $routeProvider.when('/Overview', {
                controller: 'overviewController',
                templateUrl: urlBase + 'overviewContent.html'
            })
            .when('/Index/:stockCode', {
                controller: 'indexController',
                templateUrl: urlBase + 'indexContent.html'
            })
            .when('/Comparison/:stockCodes', {
                controller: 'cmpController',
                templateUrl: urlBase + 'comparisonContent.html'
            })
            .when('/Task', {
                controller: 'taskController',
                templateUrl: urlBase + 'taskContent.html'
            })
            .when('/Score', {
                controller: 'scoreController',
                templateUrl: urlBase + 'scoreContent.html'
            })
            .when('/SingleTrain', {
                controller: 'singleTrainController',
                templateUrl: urlBase + 'singleTrainContent.html'
            })
            .when('/Commands', {
                controller: 'TypeaheadCtrl',
                templateUrl: urlBase + 'commandContent.html'
            })
            .when('/Bond', {
                controller: 'bondController',
                templateUrl: urlBase + 'bondContent.html'
            })
            .when('/Reports', {
                controller: 'reportsController',
                templateUrl: urlBase + 'reportsContent.html'
            })
            .when('/AIP', {
                controller: 'aipController',
                templateUrl: urlBase + 'aipContent.html'
            })
            .otherwise({
                redirectTo: '/Overview'
            });
    }
]);



