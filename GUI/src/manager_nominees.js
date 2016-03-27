function createRemoveButton(){
	existRemoveButton = true;
	var but = document.createElement("BUTTON");
	but.className="button1";
	but.onclick = function(){
		nomCount--;
		document.getElementById("nominees").removeChild(document.getElementById("nominees").lastChild);
		document.getElementById("nominees").removeChild(document.getElementById("nominees").lastChild);
		if(nomCount<2 && existRemoveButton){ //remove the button	
			document.getElementById("removeButtonDiv").innerHTML = "";
			existRemoveButton = false;
		}
	};
	but.innerHTML = "הסר את המועמד האחרון";
	document.getElementById("removeButtonDiv").appendChild(but);
}

function createNominee(){ 
	nomCount++;
	if(nomCount>1 && !existRemoveButton){
		createRemoveButton();				
	}
	var nomForm = document.createElement("div");
	nomForm.className = "candidate";
	var header = document.createElement("H4");
	header.className = "subtitle";
	header.appendChild(document.createTextNode("פרטי מועמד מספר " + nomCount));
	nomForm.appendChild(header); //0 child
	var nameHead = document.createTextNode(" הכנס שם מלא של המועמד: ");
	nomForm.appendChild(nameHead); //1 child
	var fullName  = document.createElement("input");
	fullName.type = "text";
	fullName.id = "fullName";
	nomForm.appendChild(fullName); //2 child
	nomForm.appendChild(document.createElement("BR")); //3 child
	nomForm.appendChild(document.createElement("BR")); //4 child
	var picHead = document.createTextNode("בחר תמונה של המועמד: ");
	nomForm.appendChild(picHead); // 5 child
	var pic = document.createElement("input");
	pic.type = "file";
	pic.id = nomCount;
	pic.name = "pic";
	pic.className = "inputfile";
	nomForm.appendChild(pic); // 6 child
	var label = document.createElement("label");
	label.setAttribute("for",nomCount);
	label.className="button2";
	label.innerHTML = "בחר קובץ...";
	nomForm.appendChild(label); // 7 child
	document.getElementById("nominees").appendChild(nomForm);
	document.getElementById("nominees").appendChild(document.createElement("BR"));
	inputListener();
}

function toManager(){
	var nominees = '{"nominees":[';
	var forms = document.getElementById("nominees").getElementsByClassName("candidate");
	for(i=0; i<forms.length; i+=1){
		var imgArrName = forms[i].childNodes[6].value.replace(/\\/g,"/").split('/');
		var index = imgArrName.length-1;
		var imagePath = imgArrName[index];
		nominees += '{"id":"'+(i+1)+'", "name":"'+forms[i].childNodes[2].value+'", "image": "' + imagePath + '"}'
		if(i<forms.length-1){
			nominees+=",";
		}
	}
	nominees += "]}";
	;
	if (checkInput()){
		localStorage.nominees = nominees;					
		window.location.href = "manager_pref.html";
	}
	else{
		return;
	}
}

function checkInput(){
	var forms = document.getElementById("nominees").getElementsByClassName("candidate");
	var errorStr = "";
	for(i=0; i<forms.length; i++){
		var candidateName = forms[i].childNodes[2].value;
		var candidatePicture = forms[i].childNodes[6].value;
		if(candidateName.localeCompare("") == 0 && candidatePicture.localeCompare("") == 0)
				errorStr += " למועמד מספר"+"&nbsp"+(i+1)+"&nbsp"+ "חסרים שם ותמונה" + "<br>";
		else if (candidateName.localeCompare("") == 0)
				errorStr += " למועמד מספר"+"&nbsp"+(i+1)+"&nbsp"+ "חסר שם " + "<br>";
		else if (candidatePicture.localeCompare("") == 0)
				errorStr += " למועמד מספר"+"&nbsp"+(i+1)+"&nbsp"+ "חסרה תמונה" + "<br>";
		
		for(j=i+1; j<forms.length; j++){
			var otherCandidateName = forms[j].childNodes[2].value;
			var otherCandidatePicture = forms[j].childNodes[6].value;
			if(candidateName.localeCompare("") != 0 && candidatePicture.localeCompare("") != 0 && candidateName.localeCompare(otherCandidateName) == 0 && candidatePicture.localeCompare(otherCandidatePicture) == 0)
				errorStr += " למועמדים"+"&nbsp"+(i+1)+" ו- "+(j+1)+"&nbsp"+ "שם ותמונה זהים" + "<br>";
			else if(candidateName.localeCompare("") != 0 && candidateName.localeCompare(otherCandidateName) == 0)
				errorStr += " למועמדים"+"&nbsp"+(i+1)+" ו- "+(j+1)+"&nbsp"+ "שם זהה" + "<br>";
			else if(candidatePicture.localeCompare("") != 0 && candidatePicture.localeCompare(otherCandidatePicture) == 0)
				errorStr += " למועמדים"+"&nbsp"+(i+1)+" ו- "+(j+1)+"&nbsp"+ "תמונה זהה" + "<br>";
		}
	}
	if (errorStr == "")
		return(true);
	else{
		errorAlert("שגיאה", errorStr)
		return false;
	}
}

function inputListener(){
			var inputs = document.querySelectorAll( '.inputfile' );
			Array.prototype.forEach.call( inputs, function( input )
			{
				var label = input.nextElementSibling,
				labelVal  = label.innerHTML;

				input.addEventListener( 'change', function( e )
				{
					var fileName = e.target.value.split( '\\' ).pop();

					if( fileName )
						label.innerHTML = fileName;
					else
						label.innerHTML = labelVal;
				});
			});
}