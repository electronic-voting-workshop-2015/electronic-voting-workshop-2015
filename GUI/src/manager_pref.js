function createRaces() {
	if(document.getElementById("group_element_size").value<2){
		errorAlert("שגיאה", "מספר הבתים באיברי החבורה צריך להיות לפחות 2");
		return;
	}
	var can_number = parseInt(document.getElementById("race_num").value);
	document.getElementById("manager").style.display = "none";
	document.getElementById("races").innerHTML = "";
	document.getElementById("title").innerHTML = "הגדרת המרוצים";
	for(i = 1; i <= can_number; i++){
		createRace(i);
	}
	var confirmButton = document.createElement("button");
	confirmButton.onclick = function(){
		if(approve(can_number)){
			var textToGui = '['
			var parameterText = '[{"RaceProperties": [';
			var numOfRaces = document.getElementById("race_num").value;
			var nom = JSON.parse(localStorage.nominees);
			for(i=1; i<=numOfRaces; i++){
				textToGui+=('{"position": "');
				textToGui+=(document.getElementById("raceName"+i).value);
				textToGui+=('", "candidates": [');	
				parameterText+=('{"position": "');
				parameterText+=(document.getElementById("raceName"+i).value);
				parameterText+=('", "candidates": [');							
				for(j=0; j<nom.nominees.length; j++){
					if(document.getElementById("div" + i + j).alt == 1){
						textToGui+=(JSON.stringify(nom.nominees[j]));
						textToGui+=(',');
						parameterText+=(JSON.stringify(nom.nominees[j]));
						parameterText+=(',');
					}
				}						
				parameterText += ('{"id": "0", "name": "fake candidate", "image": "noImage.jpg" }], "type": ');
				textToGui = textToGui.slice(0, -1);
				textToGui +=('], "type": ')
				var selectOption = document.getElementById("raceOption"+i);
				textToGui += selectOption.options[selectOption.selectedIndex].value;
				parameterText += selectOption.options[selectOption.selectedIndex].value;
				if(selectOption.options[selectOption.selectedIndex].value!=0){
					textToGui += (',"num": ');
					textToGui += document.getElementById("numOfChoices"+i).value;
					parameterText += (',"slots": ');
					parameterText += document.getElementById("numOfChoices"+i).value;
				}
				textToGui +=('}');
				parameterText +=('}');
				if (i<numOfRaces){
					textToGui += (',');
					parameterText += (',');
				}
			}
			textToGui += (']');
			parameterText += (']');
			localStorage.data = textToGui;
			parameterText +=('}, {"Group": [{"Order": "');
			parameterText += (document.getElementById("group_order").value);
			parameterText += ('"}, {"ElementSizeInBytes": "');
			parameterText += (document.getElementById("group_element_size").value);
			parameterText += ('"}, {"EC": [{"a": "');
			parameterText += (document.getElementById("a").value);	
			parameterText += ('"}, {"b": "');
			parameterText += (document.getElementById("b").value);	
			parameterText += ('"}, {"p": "');
			parameterText += (document.getElementById("p").value);
			parameterText += ('"}]}, {"Generator": "');
			parameterText += (document.getElementById("generator").value);
			parameterText += ('"}]}, {"NumOfMachines": ');
			parameterText += (document.getElementById("NumOfMachines").value);		
			parameterText += ('}, {"TimeStampLevel": ');
			if(document.getElementById("TimeStampLevel1").checked){
				parameterText += (1);
			}
			else{
				parameterText += (2);
			}						
			parameterText += ('}]');
			localStorage.param = parameterText;
		/*	
			 $.ajax({
            url: "http://localhost:4567/Vote",
            type: 'POST',
            contentType: 'jsonp',
            traditional: true,
            data: JSON.stringify(votingJSON),
            success: function () {
                sendAuditJSONData(auditJSON);
            },
            error: function () {
                 sendAuditJSONData(auditJSON);
            }
        });
		*/	
		
			
			
			$.post({
				//url: "http://46.101.148.106:4567/publishParametersFile",
                		url: "http://46.101.148.106:4567/publishParametersFile",
				data: {content: {ARBITRARY: 'JAVASCRIPT', OBJECT: 'HERE'}},
				success: function () {
                    successAlert( "OK!!!" );
				},
				error: function (ajaxrequest, ajaxOptions, thrownError) {
					successAlert("סיום","הגדרת המרוצים נשמרה בהצלחה!");
					// alert(thrownError);
				}
			});
		}
		
	};
	confirmButton.innerHTML = "סיום";
	
	var backButton = document.createElement("button");
	backButton.onclick = function(){
		document.getElementById("manager").style.display = "initial";
		document.getElementById("races").innerHTML = "";
		document.getElementById("title").innerHTML = " הגדרת נתוני מערכת הבחירות ";
	}
	backButton.innerHTML = "חזרה";
	
	document.getElementById("races").appendChild(backButton);
	document.getElementById("races").appendChild(confirmButton);

}

function approve(can_number){
	var errorStr = "";
	for(i=1; i<=can_number; i++){
		if (document.getElementById("raceName"+ i ).value.localeCompare("") == 0){  //missing name
			errorStr += "למירוץ מספר " + i + " חסר שם" + "<br>";
		}
	}
	
	if (errorStr == "")
		return true;
	else{
		errorAlert("שגיאה", errorStr)
		return false;
	}
}


function createRace(i){
	var raceForm = document.createElement("div");
	document.getElementById("races").appendChild(raceForm);
	document.getElementById("races").appendChild(document.createElement("br"));
	raceForm.id = "race" + i;
	raceForm.className = "race";
	var l1 = document.createElement("H4");
	l1.className = "subtitle";
	l1.appendChild(document.createTextNode("הגדרת מרוץ מספר " + i));
	raceForm.appendChild(l1);
	raceForm.appendChild(document.createTextNode("שם המרוץ: "));
	var raceName = document.createElement("input");
	raceName.type = "text";
	raceName.id = "raceName"+i;
	raceForm.appendChild(raceName);
	raceForm.appendChild(document.createElement("br"));
	raceForm.appendChild(document.createElement("br"));
	var l2 = document.createTextNode("בחר שיטת בחירות רצויה: ");
	raceForm.appendChild(l2);
	var l3 = document.createElement("select");
	l3.id = "raceOption"+i;
	var l4 = document.createElement("div");
	l4.id = "optiondiv"+i;
	var raceOptionArray = ["מועמד יחיד","בחירה מרובה","בחירה מדורגת"];
	for (var j = 0; j < raceOptionArray.length; j++) {
		var option = document.createElement("option");
		option.value = j;
		option.text = raceOptionArray[j];
		l3.appendChild(option);
	}
	raceForm.appendChild(l3);
	raceForm.appendChild(l4);
	var nom = JSON.parse(localStorage.nominees);
	l3.onchange=function(){
		$('#optiondiv'+i).empty();
		if(l3.options[l3.selectedIndex].value == 0)
			return;
		else{
			l4.appendChild(document.createElement("br"));

			if(l3.options[l3.selectedIndex].value==1)
				l4.appendChild(document.createTextNode("מספר המועמדים לבחירה: "));
			else if(l3.options[l3.selectedIndex].value==2)
				l4.appendChild(document.createTextNode("מספר המועמדים לדירוג: "));
			
			var numOfChoices = document.createElement("input");
			numOfChoices.id = "numOfChoices"+i;
			numOfChoices.type = "number";
			numOfChoices.value = "1";
			numOfChoices.min = "1";
			numOfChoices.max = nom.nominees.length;
			l4.appendChild(numOfChoices);
		}
	};
	raceForm.appendChild(document.createElement("br"));
	raceForm.appendChild(document.createTextNode("בחר את המתמודדים במירוץ: "));
	raceForm.appendChild(document.createElement("br"));
	raceForm.appendChild(document.createElement("br"));
	for(j=0; j<nom.nominees.length; j++){
		var div = document.createElement("div");
		var img = document.createElement("img");
		var name = document.createElement("p");
		div.className = "race";
		div.id = "div" + i + j;
		div.appendChild(img);
		div.appendChild(name);
		div.style.display = "inline-block";
		div.style.cursor = "pointer";
		div.style.border = "5px solid #008CBA";
		if (nom.nominees.length != 1)
			div.style.marginLeft = "1.5vw"
		raceForm.appendChild(div);
		name.id = "name" + i + j;
		name.style.fontWeight = "bold";
		name.style.color = "#008CBA";
		img.id = "image" + i + j;
		img.style.width = "8vw";
		img.style.height = "8vw";
		div.alt = 1; //-1 - not chosen, 1- chosen
		$(div).click(function(){
			this.alt =(-1)*this.alt; //chosen
			if (this.alt==1){
				this.style.border = "5px solid #008CBA";
				this.childNodes[1].style.fontWeight = "bold";
				this.childNodes[1].style.color = "#008CBA";
			}
			else{	
				this.style.border = "2px solid #008CBA";
				this.childNodes[1].style.fontWeight = "normal";
				this.childNodes[1].style.color = "black";
			}
		});
		img.src = "src/".concat(nom.nominees[j].image);
		name.innerHTML = nom.nominees[j].name;
	}
}
