function saveDetails(){
	alert("hi from js");
var accountNumber =$('#acc_no').val();
var phoneNumber=$('#phone_number').val();
var invoiceDate=$('#invoice_date').val();
var dateDue=$('#date_due').val();
var amountEnclosed=$('#amount_enclosed').val();

var individualArray=[10];
var stringArray=[1];
var floatArray=[0];

var totalDue=$('#total_due').val();
individualArray[0]=totalDue;

individualArray[1]=0;
var paymentsReceived=$('#payment_rec').val();
individualArray[2]=paymentsReceived;

var debitsCredits=$('#debit_credit').val();
individualArray[3]=debitsCredits;

var balanceForward=$('#bal_fwd').val();
individualArray[6]=balanceForward;

individualArray[4]=0;
individualArray[5]=0;

var currentCharges=$('#curent_charge').val();
individualArray[7]=currentCharges;

var totalDueby=$('#total_due_by').val();
individualArray[8]=totalDueby;

var totalDueafter=$('#total_due_after').val();
individualArray[9]=totalDueafter;

var planOverage=$('#poc').val();
stringArray[0]="PlanOverCharges "+planOverage;

var otherCharges=$('#ocharges').val();
stringArray[1]="OtherCharges "+otherCharges;

var totalCurrentCharges=$('#tcc').val();
floatArray[0]=totalCurrentCharges;

var dataToBeSent={};
dataToBeSent["accountNumber"]=accountNumber;
dataToBeSent["phoneNumber"]=phoneNumber;
dataToBeSent["invoiceDate"]=invoiceDate;
dataToBeSent["dateDue"]=dateDue;
dataToBeSent["amountEnclosed"]=amountEnclosed;

dataToBeSent["totalDue"]=individualArray;

dataToBeSent["planOverage"]=stringArray;
dataToBeSent["totalCurrentCharges"]=floatArray;

var values = 
{
    "Name": "Chris",
    "Color": "Green"
}

alert(dataToBeSent+"**********************************");
alert("values"+values);
alert("after invoice");
alert("get ready ");
alert("accountNumber="+accountNumber+"&phoneNumber="+phoneNumber+"&invoiceDate="+invoiceDate+"&dateDue="+dateDue+"&totalDue="+totalDue+"&amountEnclosed="+amountEnclosed+"&paymentsReceived="+paymentsReceived+"&debitsCredits="+debitsCredits+"&balanceForward="+balanceForward+"&currentCharges="+currentCharges+"&totalDueby="+totalDueby+"&totalDueafter="+totalDueafter+"&planOverage="+planOverage+"&otherCharges="+otherCharges+"&totalCurrentCharges="+totalCurrentCharges);

console.log(accountNumber);
console.log(amountEnclosed);
if(accountNumber.length==0){
	alert("Account Number is empty");
	return false;
}
else if(phoneNumber.length==0)
{
	alert("phone number is empty");
	return false;
}
else if(invoiceDate.length==0){
		alert("invoice date is empty");
return false;
}
	else if(dateDue.length==0){
	alert("date due is empty");
return false;
	}
else if(totalDue.length==0){
	alert("total due is Empty");
	return false;
	}
else if(amountEnclosed.length==0){
	alert("Amount Enclosed is Empty");
	return false;
	}
else if(paymentsReceived.length==0){
	alert("Payments Received is Empty");
	return false;
	}
else if(debitsCredits.length==0){
;	alert("Debits & Credits is Empty");
	return false;
	}
else if(balanceForward.length==0){
	alert("Balance Forward is Empty");
	return false;
	}
else if(currentCharges.length==0){
	alert("Current Charges is Empty");
	return false;
	}
else if(totalDueby.length==0){
	alert("total due by is Empty");
	return false;
	}
else if(totalDueafter.length==0){
	alert("total due after is Empty");
	return false;
	}
else if(planOverage.length==0){
	alert("plan Overage Charges is Empty");
	return false;
	}
else if(otherCharges.length==0){
	alert("Other Charges is Empty");
	return false;
	}
else if(totalCurrentCharges.length==0){
	alert("Total Current Charges is Empty");
	return false;
	}
alert("ajax call started");
	
alert("ajax call ended");

var parametersJSON =JSON.stringify(dataToBeSent) ;
alert(parametersJSON);
$.ajax({
	   type: 'POST',
	   url: '/admin/ShowDetailsSubmitted',
	   contentType: "application/json",
	   beforeSend: function ( jqXHR )
	   {
	    jqXHR.setRequestHeader("Connection", "close");
	   },
	   async: false,
	   data:parametersJSON ,
	    success: function response(resultObject)
	   {
	    	console.log(resultObject);
	    	
	    	alert("resultObject");
	    	taskQueueStatus=resultObject;
	    	if(taskQueueStatus){
	    		getUploadedData();
	    	}
	    	else{
	    		alert("Processing... please wait ")
	    		//setTimeout(checkTaskQueueStatus, 5000);
	    	}

	   },
	   dataType: ''
	  });



function dataStoreInTable(detailsMap)
{
	$(document).ready(
	function(){		
	
	$('.tableIndividual').show();
	$('#headersIndividual').show();
	});
	var data="";
	data='<tr><td>'+detailsMap.accountNumber+'</td><td>'+detailsMap.phoneNumber+'</td><td>'+detailsMap.invoiceDate+'</td><td>'+detailsMap.dateDue+'</td><td>'+detailsMap.address+'</td></tr>';
	


}





















	/*$.ajax({
		type:'post'
		  dataType: '',
		  url: "/ShowDetailsSubmitted",
		  data: JSON.stringify(parametersJSON),
		  success: function (){
			  alert("test");
		  }
		});

*/
/*
$(document).ready(
function(){
	$('.tableIndividual').show();
	$('#headersIndividual').show();
	console.log("the value of accountNumber is "+accountNumber+"THE VALUE OF PHONE NUMBER IS "+phoneNumber);
	var acc="";
	acc = $('#acc_no').val();
	var storingIndividualDataInTable ="";
	storingIndividualDataInTable='<tr><td>'+acc.accountNumber+'</td><td>'+acc.phoneNumber+'</td><td>'+invoiceDate+'</td><td>'+dateDue+'</td><td>'+address+'</td></tr>';
	$('#bodyIndividual').html(storingIndividualDataInTable);
	
	});

	
	*/
	




}
