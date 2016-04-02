var app = angular.module('voteApp', ['lvl.directives.dragdrop', 'lvl.services','ui.sortable']);
app.controller('voteCtrl', function ($scope) {
	
	$scope.page = 0; // current page

    var voteConfiguration = JSON.parse(localStorage.data);
    $scope.positionsCount = voteConfiguration.length;
    $scope.data = {}; // vote data will be stored here
    $scope.finishMode = 0; // 0 - not in finish mode, 1 - in finish mode
    $scope.position = voteConfiguration[$scope.page]; // current page position
    $scope.candidatesArray = $scope.position.candidates; // current page candidates

    $scope.checkChanged = function (th) { // type 1 position handler
        if ($scope.position.checked == undefined) { $scope.position.checked = 0; }
        if (th) $scope.position.checked++;
        else $scope.position.checked--;
    }

    $scope.Next = function () { // called when 'next' button is pressed
        $scope.page++;
        $scope.position = voteConfiguration[$scope.page];
        $scope.candidatesArray = $scope.position.candidates;
    };
	
    $scope.Prev = function () { // called when 'previous' button is pressed
        $scope.page--;
        $scope.position = voteConfiguration[$scope.page];
        $scope.candidatesArray = $scope.position.candidates;
    };
    
    $scope.FinalFinish2 = function (booli){
	booli = booli.toString();
	needToAudit= [];
	needToAudit.push({"audit":booli});
	sendAuditJSONData(needToAudit);
}

    $scope.Finish = function () { // called when 'finish' button is pressed
        var errors = [];
        for (var i = 0; i < $scope.positionsCount; i++) { // check for errors in vote
            if (voteConfiguration[i].type == 0 && $scope.data[i] == undefined) { // candidate was not selected
                errors.push([i, 0]); // i is the position number in voteConfiguration, 0 is type
            }
            if (voteConfiguration[i].type == 1 && (voteConfiguration[i].checked == undefined || voteConfiguration[i].checked < voteConfiguration[i].num)) { // not enough candidates were selected
                errors.push([i, 1]);
            }
			if (voteConfiguration[i].type == 2) { 
				if ($scope.data[i] == undefined) // no candidates were chosen
					errors.push([i, 2]); 
				else {
					for (var j = 0; j < $scope.data[i].length; j++){ // not enough candidates were chosen
						if ($scope.data[i][j].id == -1){
							errors.push([i, 2]);
							break;
						}
					}
				}
            }
        }
        if (errors.length == 0) { // no errors, we move to finish mode
        FinalFinish();
        }
        else { // there are errors, alerting error message
            var errorStr = "";
            for (var i = 0; i < errors.length; i++) {
                if (errors[i][1] == 0) {
                    errorStr += "לא נבחר מועמד לתפקיד "
                }
                else {
                    errorStr += "לא נבחרו " + voteConfiguration[errors[i][0]].num + " מועמדים לתפקיד "
                }
                errorStr += voteConfiguration[errors[i][0]].position + "<br>";
            }
            errorAlert('ההצבעה לא הושלמה', errorStr);
        }
    };

    $scope.BackFromFinish = function () {
        $scope.finishMode = 0;
    };

    function FinalFinish() {
	var finalDataJSON = [];

        if(!localStorage.machineId)
            localStorage.machineId = generateUUID();

        finalDataJSON.push({machineId: localStorage.machineId});

		for (var i = 0; i < $scope.positionsCount; i++) {
			voteArray = [];
			if (voteConfiguration[i].type == 0){
				voteArray[0] = $scope.data[i];
			}
			if (voteConfiguration[i].type == 1){
				for (var j = 0; j < voteConfiguration[i].candidates.length; j++){
					if ($scope.data[i][j] == true)
						voteArray.push(voteConfiguration[i].candidates[j].name);
				}
			}
			if (voteConfiguration[i].type == 2){
				for (var j = 0; j < $scope.data[i].length; j++){
					voteArray.push($scope.data[i][j].name);
				}
			}
			
			finalDataJSON.push({
				position: voteConfiguration[i].position,
				type: voteConfiguration[i].type,
				chosenCandidates: voteArray,
			});
		}
        sendVotingJSONData(finalDataJSON);
		
		$scope.finishMode = 1;
    };
	
	$scope.$watch('candidatesDroppedByOrder', function () {
        if ($scope.position.type == 2) {
            saveSelectedCandidatesWithTheirOrder($scope.page);
        }
    }, true);
    $scope.$watchGroup(['position.type', 'finishMode'], function () {
        if ($scope.position.type == 2 && $scope.finishMode == 0) {
            loadSelectedCandidatesWithTheirOrder($scope.page);
        }
    }, true);

    function saveSelectedCandidatesWithTheirOrder(pageIndex) {
        $scope.data[pageIndex] = angular.copy($scope.candidatesDroppedByOrder);
    }
    function loadSelectedCandidatesWithTheirOrder(pageIndex) {
        $scope.candidatesToDragFrom = angular.copy($scope.candidatesArray);
        $scope.candidatesDroppedByOrder = [$scope.position.num];
        for (i = 0 ; i < $scope.position.num ; i++) {
            $scope.candidatesDroppedByOrder[i] = { id: "-1", name: '' };
        }

        var candidatesOrder = $scope.data[pageIndex]; // array of candidates ordered by their rank (place)

        if (candidatesOrder) //if defined then load its data - otherwise, keep the defaults settings
        {
            $scope.candidatesDroppedByOrder = angular.copy(candidatesOrder);
            for (i = 0 ; i < $scope.candidatesDroppedByOrder.length ; i++) {
                candidateId = $scope.candidatesDroppedByOrder[i].id; // get the id of the candidate (if empty cell - we get -1)
                if (candidateId > -1) { // if we found save data for this candidate (his rank) update drag&drop according
                    $scope.candidatesToDragFrom = _.reject($scope.candidatesToDragFrom, function (candidate) { return candidate.id == candidateId; });
                }
            }
        }
    }

    function generateUUID() {
        var d = new Date().getTime();
        var uuid = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
            var r = (d + Math.random()*16)%16 | 0;
            d = Math.floor(d/16);
            return (c=='x' ? r : (r&0x3|0x8)).toString(16);
        });
        return uuid;
    };

    function sendVotingJSONData(votingJSON){
        //call vote function
		 $.ajax({
			url: "http://localhost:4567/Vote",
			crossDomain: true,
            type: 'OPTIONS',
            contentType: 'jsonp',
            traditional: true,
            data: JSON.stringify(votingJSON),
            success: function () {
                return;
            },
            error: function () {
				alert("failed!");
                 return;
            }
        });
        $.ajax({
			url: "http://localhost:4567/Vote",
			crossDomain: true,
            type: 'POST',
            contentType: 'jsonp',
            traditional: true,
            data: JSON.stringify(votingJSON),
            success: function () {
                return;
            },
            error: function () {
				alert("failed!");
                return;
            }
        });
    }

    function sendAuditJSONData(auditJSON){
        //call audit function
        $.ajax({
        	url: "http://localhost:4567/Audit",
            crossDomain: true,
            type: 'OPTIONS',
            contentType: 'jsonp',
            traditional: true,
            data: JSON.stringify(auditJSON),
            success: function () {
                window.location = 'vote_finish.html';
            },
            error: function () {
				alert("failed!");
                return;
            }
        });
		$.ajax({
			url: "http://localhost:4567/Audit",
			crossDomain: true,
            type: 'POST',
            contentType: 'jsonp',
            traditional: true,
            data: JSON.stringify(auditJSON),
            success: function () {
                window.location = 'vote_finish.html';
            },
            error: function () {
				alert("failed!");
                return;
            }
        });
    }
});


// drag and drop controller
app.controller('ddController', ['$scope', 'uuid', function ($scope, uuid) {
    var emptyCandidate = { id: "-1", name: "" };
    $scope.dropped = function (dragEl, dropEl) {
        var drop = $('#' + dropEl);
        var drag = $('#' + dragEl);

        var dropSpan = drop.parent();
        var dropCandidateId = dropSpan.attr("candidate-id");
        var dropCandidateIndex = dropSpan.attr("candidate-index");
        var dragSpan = drag.find("span").first();
        var dragCandidateId = dragSpan.attr("candidate-id");

        //if the dropped cell is already occupied by another candidate - clear it
        var candidateOnDropCell = angular.copy(_.where($scope.candidatesArray, { id: dropCandidateId }));
        if (candidateOnDropCell.length > 0) //the cell was already occupied by another candidate - clear it and add the candidate back to the candidates list not chosen
        {
            $scope.candidatesDroppedByOrder[dropCandidateIndex] = angular.copy(emptyCandidate); //will be set again later on this function
            $scope.candidatesToDragFrom.push(candidateOnDropCell[0]);
        }

        //delete the dragged candidate from the 'candidatesToDragFrom' and set the text of the cell selected to be the candidate name we dragged
        var draggedCandidate = angular.copy(_.where($scope.candidatesArray, { id: dragCandidateId }));
        if (draggedCandidate.length > 0) { //check if the candidate dragged isn't on the dropped list already (dragged from the candidates list)
            $scope.candidatesToDragFrom = _.reject($scope.candidatesToDragFrom, function (candidate) { return candidate.id == dragCandidateId; }); //remove that candidate from the draggable candidates
            $scope.candidatesDroppedByOrder[dropCandidateIndex] = draggedCandidate[0];
        }
        else { //the candidate dragged must be on the dropped list already

        }

        $scope.$apply();
    }
    $scope.removeCandidate = function (candidateId, dropIndex) {
        var candidate = angular.copy(_.where($scope.candidatesArray, { id: candidateId }));
        $scope.candidatesToDragFrom.push(candidate[0]);
        $scope.candidatesDroppedByOrder[dropIndex] = angular.copy(emptyCandidate);
    }
}]);
