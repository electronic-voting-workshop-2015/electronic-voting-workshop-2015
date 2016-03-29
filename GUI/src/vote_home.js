var app = angular.module('homeApp', []);
app.controller('homeCtrl', function($scope) {
	$scope.EnterVote = function () {
		location.href = "vote_voting.html";
    };
});