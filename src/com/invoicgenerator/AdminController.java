package com.invoicgenerator;

import static com.google.appengine.api.taskqueue.TaskOptions.Builder.withUrl;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.files.AppEngineFile;
import com.google.appengine.api.files.FileReadChannel;
import com.google.appengine.api.files.FileService;
import com.google.appengine.api.files.FileServiceFactory;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.repackaged.org.codehaus.jackson.JsonGenerationException;
import com.google.appengine.repackaged.org.codehaus.jackson.map.JsonMappingException;
//import com.itextpdf.text.DocumentException;
//import com.itextpdf.text.pdf.AcroFields;
//import com.itextpdf.text.pdf.PdfReader;
//import com.itextpdf.text.pdf.PdfStamper;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;


@Controller
@RequestMapping(value = "/admin")
public class AdminController {

	public static final Logger log = Logger.getLogger(AdminController.class.getName());
	public static final HashMap<String, Map<String, Object>> lReturnUserObj = new HashMap<String, Map<String, Object>>();
	public static boolean taskqueueStatus = false;
	public static BlobKey blobKey;
	@Autowired
	ServletContext context;
	@RequestMapping("/")
	public String re_directhome(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		return "home";
	}
	@RequestMapping("/home")
	public String homePage(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		System.out.println("heloo Welcome");
		return "home";
	}
	BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
	@RequestMapping("/uploadcsv")
	public @ResponseBody String  uploadCsvFile(HttpServletRequest request, HttpServletResponse response,@RequestBody String text) throws IOException {
		HttpSession lSession = request.getSession();
		String responseText = "";
		ArrayList<String> customersList = new ArrayList<String>();
		ServletOutputStream out = response.getOutputStream();
			Map<String, BlobKey> blobs = blobstoreService.getUploadedBlobs(request);
			blobKey = blobs.get("uploadFile");
			Queue queue 			= QueueFactory.getQueue("taskQueueToReadTextFileAndCreateStaticMap");
			queue.add(withUrl("/admin/taskQueueToReadTextFileAndCreateStaticMap").param("data",""));
			return blobKey.getKeyString();
		}
	@RequestMapping("/getUploadedData")
	public @ResponseBody HashMap<String, Map<String, Object>>  getUploadedData(HttpServletRequest request, HttpServletResponse response,@RequestBody String text) throws IOException {
		HttpSession lSession = request.getSession();
				System.out.println("getUploadedData service method  ...... "+lReturnUserObj.size());
			return lReturnUserObj;
		}
	@RequestMapping("/getTaskQueueStatus")
	public @ResponseBody boolean  getTaskQueueStatus(HttpServletRequest request, HttpServletResponse response) throws IOException {
			System.out.println("inside taskqueueStatus service method   "+taskqueueStatus);
			return taskqueueStatus;
		}
	@RequestMapping("/taskQueueToReadTextFileAndCreateStaticMap")
	public @ResponseBody void getUrlForImportedContactsOnChrome(HttpServletRequest request, HttpServletResponse response,@RequestBody String text) throws IOException {
		ServletOutputStream out = response.getOutputStream();
		try {
			FileService fileServices = FileServiceFactory.getFileService();
			AppEngineFile fileURL = fileServices.getBlobFile(blobKey);
			fileURL.getFileSystem();
			FileReadChannel readChannel = fileServices.openReadChannel(fileURL,true);
			BufferedReader reader = new BufferedReader(Channels.newReader(readChannel, "UTF8"));
			String line;
			String accountNumber = "";
			String phoneNumber = "";
			String invoiceDate = "";
			String dateDue = "";
			String[] TotalDue = new String[10];
			String[] address;
			Double totalCurrentCharges;
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("0100")) {
					HashMap<String, Object> detailsObject = new HashMap<String, Object>();
					String firstLine = line.replaceAll(" ", "").replaceFirst("0100", "");
					accountNumber = firstLine.substring(0, 10);
					phoneNumber = firstLine.substring(10, 22);
					invoiceDate = firstLine.substring(22, 30);
					dateDue = firstLine.substring(30, 38);
					String[] amtArray = generateAmountsFromFirstRow(firstLine
							.substring(38, firstLine.length()));
					detailsObject.put("TotalDue", amtArray);
					detailsObject.put("accountNumber", accountNumber);
					detailsObject.put("phoneNumber", phoneNumber);
					detailsObject.put("invoiceDate",generateRequiredDateFormat(invoiceDate));
					detailsObject.put("dateDue",generateRequiredDateFormat(dateDue));
					line = reader.readLine();
					String addressString = line.replaceFirst("0200", "");
					address = addressString.split("\\s{3,}");
					detailsObject.put("address", address);
					String accountName = address[0];
					detailsObject.put("accountName", accountName);
					ArrayList<Object> summaryOfCharges = new ArrayList<Object>();
					ArrayList<Object> summaryOfCharges2 = new ArrayList<Object>();
					ArrayList<Object> userIdSummary = new ArrayList<Object>();
					while ((line = reader.readLine()) != null) {
						if (line.startsWith("9900"))
							break;
						if (line.startsWith("1100")) {
							String summaryChargesString = line.replaceFirst(
									"1100", "");
							String[] each1100 = summaryChargesString
									.split("\\s{3,}");
							if(each1100.length == 2)
								each1100[1] = Double.toString(Double.parseDouble(each1100[1]));
							summaryOfCharges.add(each1100);
						}
						if(line.startsWith("4100"))
						{
							String summaryChargesString = line.replaceFirst(
									"4100", "");
							ArrayList<Object> each4100Array = new ArrayList<Object>();
							if ( summaryChargesString.trim().length() > 35) {
									String date = summaryChargesString.substring(0, 8);
									if (Character.isDigit(date.charAt(0))) {
										date = generateRequiredDateFormat(date);
										each4100Array.add(date);
										String description = summaryChargesString.substring(8, 34);
										each4100Array.add(description);
										Integer nom = Integer.parseInt(summaryChargesString
												.substring(34, 42));
										each4100Array.add(nom);
										Double fee = Double.parseDouble(summaryChargesString
												.substring(42));
										each4100Array.add(fee);
									}
								}
							else
								each4100Array.add(summaryChargesString);
							summaryOfCharges2.add(each4100Array);
						}
						if(line.startsWith("9200"))
						{
							String summaryChargesString = line.replaceFirst(
									"9200", "");
							ArrayList<Object> each9200Array = new ArrayList<Object>();
							if ( summaryChargesString.trim().length() > 0) {
								String[] each9200Description = summaryChargesString.split("\\s{3,}");
								if(each9200Description[0].trim().length() != 0)
										each9200Array.add(each9200Description[0]);
								String[] each9200RestOfFields = each9200Description[1].split(" ");
								Integer noOfCalls = Integer.parseInt(each9200RestOfFields[0]);
								each9200Array.add(noOfCalls);
								Double minutes = Double.parseDouble(each9200RestOfFields[1]);
								each9200Array.add(minutes);
								Double amount = Double.parseDouble(each9200RestOfFields[2]);
								each9200Array.add(amount);
							}
							else
								each9200Array.add(" ");
							userIdSummary.add(each9200Array);
							
						}
						if (line.startsWith("0500")) {
							String[] answerConnectAddress = line.replaceFirst(
									"0500", "").split("\\s{3,}");
							detailsObject.put("answerConnectAddress",
									answerConnectAddress);

						}
						if (line.startsWith("1200")) {

							try {
								totalCurrentCharges = Double.parseDouble(line
										.replace("1200", ""));
								detailsObject.put("totalCurrentCharges",
										totalCurrentCharges);
							} catch (Exception e) {
								// TotalDue=firstLine.substring(38, 50);
								detailsObject.put("totalCurrentCharges",
										line.replace("1200", ""));
							}

						}
					}

					detailsObject.put("summaryOfCharges", summaryOfCharges);
					detailsObject.put("summaryOfCharges2", summaryOfCharges2);
					detailsObject.put("userIdSummary", userIdSummary);

					lReturnUserObj.put(accountNumber, detailsObject);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			out.println("fileerroruploading");
			System.out.println("Exception in sendig File URL.." + e);
		}
		System.out.println(lReturnUserObj.size());
		taskqueueStatus = true;
    System.out.println("task Queue Execution completed now");
	}

	public String generateRequiredDateFormat(String data) {
		String year = data.substring(0, 4);
		String month = data.substring(4, 6);
		String date = data.substring(6, 8);
		return month + "/" + date + "/" + year.substring(2, 4);
	}

	public String[] generateAmountsFromFirstRow(String str) {
		String[] tempArray = new String[10];
		for (int i = 0; i < 10; i++) {
			tempArray[i] = str.substring(0, (str.indexOf(".") + 3));
			str = str.substring((str.indexOf(".") + 3), str.length());
		}
		tempArray[2] = (tempArray[2].substring(8, tempArray[2].length()));
		String[] TotalDue = new String[10];
		for (int i = 0; i < tempArray.length; i++) {
			try {
				TotalDue[i] = Double.parseDouble(tempArray[i]) + "";
			}
			catch (Exception e) {
				TotalDue[i] = tempArray[i].substring(8, tempArray[i].length());
			}
		}
		return TotalDue;
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public String generator(Map<String, Map<String, Object>> totalDetails) {
		String totalData = "";
		Iterator iterateTotalDetails = totalDetails.entrySet().iterator();
		while (iterateTotalDetails.hasNext()) {
			String eachInvoice = "";
			Map.Entry mapEntry = (Map.Entry) iterateTotalDetails.next();
			Map<String, Object> individualDetails = (Map<String, Object>) mapEntry
					.getValue();
			Iterator iterateIndividualDetails = individualDetails.entrySet().iterator();
			while (iterateIndividualDetails.hasNext()) {
				Map.Entry mapEntry1 = (Map.Entry) iterateIndividualDetails.next();
				Object eachField = mapEntry1.getValue();
				String eachKey = (String) mapEntry1.getKey();
				
				if (eachField instanceof String) {
					eachInvoice += eachField + "\t";
				} 
				
				else if (eachField instanceof String[]) {
					String[] stringArray = (String[]) eachField;
					String address = "";
					if (eachKey.equals("TotalDue")) {
						for (int i = 0; i < stringArray.length; i++) 
							eachInvoice += stringArray[i] + "\t ";
					} else if (eachKey.equals("address")) {
						for (int i = 0; i < stringArray.length; i++) 
							address += stringArray[i] + " ";
						eachInvoice += address + "\t";
					} else if (eachKey.equals("answerConnectAddress")) {
						for (int i = 0; i < stringArray.length; i++) 
							address += stringArray[i] + " ";
						eachInvoice += address + "\t";
					}
				}
				
				else if (eachField instanceof ArrayList) {
					ArrayList<Object> list = (ArrayList<Object>) eachField;
					String summary = "";
					if (eachKey.equals("summaryOfCharges")) {
						for (Object summaryOfChargesList : list) {
							if (summaryOfChargesList instanceof String[]) {
								String[] stringArray = (String[]) summaryOfChargesList;
								for (int i = 0; i < stringArray.length; i++) 
									summary += stringArray[i] + " ";
							}
						}
						eachInvoice += summary + "\t";
					} else if (eachKey.equals("summaryOfCharges2")) {
						for (Object summaryOfCharges2List : list) {
							if (summaryOfCharges2List instanceof String[]) {
								String[] stringArray = (String[]) summaryOfCharges2List;
								for (int i = 0; i < stringArray.length; i++) 
									summary += stringArray[i] + " ";
							}
						}
						eachInvoice += summary + "\t";
					}
					else if(eachKey.equals("userIdSummary")){
						for (Object userIdSummaryList : list){
							if (userIdSummaryList instanceof String[])
							{
								String[] stringArray = (String[]) userIdSummaryList;
								for (int i = 0; i < stringArray.length; i++)
									summary += stringArray[i] + " ";
							}
						}
						eachInvoice += summary + " ";
					}
				}
				
				else if (eachField instanceof Double) {
					Double floatValue = (Double) eachField;
					eachInvoice += floatValue + "\t";
				}
				
				else
					eachInvoice += "\t";
			}
			totalData += eachInvoice + "\n";

		}
		// System.out.println("In generator:"+totalData);
		return totalData;
	}

	@RequestMapping(value = "/downloadcsv", method = RequestMethod.POST)
	public void downloadFile(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		final Logger log = Logger.getLogger(AdminController.class.getName());
		System.out.println("in download");
		String eachInvoice = generator(lReturnUserObj);
		// log.info(eachInvoice);
		response.setContentType("text/csv");
		response.setHeader("Content-Disposition",
				"attachment;filename=InvoiceDataDelimited.txt");
		response.getWriter().write(eachInvoice);
		System.out.println("in download");
	}
	@SuppressWarnings("rawtypes")
	@RequestMapping(value = "/generatepdf", method = RequestMethod.POST)
	public void generatePdf(@RequestBody String currentRecord,
			HttpServletRequest request, HttpServletResponse response)
			throws ServletException, JsonGenerationException,
			JsonMappingException, IOException, DocumentException {
	
		String accountName = "";
		String accountNumber = "";
		String invoiceDate = "";
		String dateDue = "";
		String phoneNumber = "";
		String[] TotalDue = new String[10];
		String addressString = "";
		String[] answerConnectAddress = new String[10];
		String[] summaryOfCharges = new String[20];
		String description = "";
		String noOfCalls = "";
		String minutes = "";
		String amount = "";
		String datePage2 ="";
		String summary ="";
		String nom ="";
		String charges = "";
		Double totalCurrentCharges = 0.0;
		log.info("data from frontend" + currentRecord);
		String[] index = currentRecord.split("=");
		Map<String, Object> mapDetails = lReturnUserObj.get(index[1]);
		System.out.println(mapDetails);
		Iterator it = mapDetails.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry mapEntry = (Map.Entry) it.next();
			Object eachValue = mapEntry.getValue();
			String eachKey = (String) mapEntry.getKey();
			if (eachValue instanceof String) {
				if (eachKey.equals("accountNumber"))
					accountNumber = (String) mapEntry.getValue();
				else if (eachKey.equals("accountName"))
					accountName = (String) mapEntry.getValue();
				else if (eachKey.equals("invoiceDate"))
					invoiceDate = (String) mapEntry.getValue();
				else if (eachKey.equals("phoneNumber"))
					phoneNumber = (String) mapEntry.getValue();
				else if (eachKey.equals("dateDue"))
					dateDue = (String) mapEntry.getValue();
				System.out.println("accountnumber::" + accountNumber);
			} else if (eachValue instanceof String[]) {
				String[] stringArray = (String[]) eachValue;
				if (eachKey.equals("TotalDue")) {
					for (int i = 0; i < stringArray.length; i++) {
						TotalDue[i] = stringArray[i];
					}
				} else if (eachKey.equals("address")) {
					for (int i = 0; i < stringArray.length; i++) {
						addressString += stringArray[i] + "\n";
					}
				} else if (eachKey.equals("answerConnectAddress")) {
					for (int i = 0; i < stringArray.length; i++) {
						answerConnectAddress[i] = stringArray[i];
					}
				}
			} else if (eachValue instanceof ArrayList) {
				ArrayList<Object> list = (ArrayList<Object>) eachValue;
				if (eachKey.equals("summaryOfCharges")) {
					for (Object o : list) {
						if (o instanceof String[]) {
							String[] stringArray = (String[]) o;
							for (int i = 0; i < stringArray.length; i++) {
								summaryOfCharges[i] = stringArray[i];
							}
						}
					}
				} else if (eachKey.equals("summaryOfCharges2")) {
					for (Object o : list) {
						if (o instanceof ArrayList) {
							ArrayList<Object> each4100 = (ArrayList<Object>) o;
							if(each4100.size() == 4){
								datePage2 += each4100.get(0).toString().substring(0, 5)+"\n";
								summary += each4100.get(1).toString().trim()+"\n";
								nom += each4100.get(2).toString()+"\n";
								charges += each4100.get(3).toString()+"\n";
							}
							else if(each4100.size() == 1){
								datePage2 += "\n";
								summary += each4100.get(0).toString().trim()+"\n";
								nom += "\n";
								charges += "\n";
								
							}
						}
								
					}
				}
				else if(eachKey.equals("userIdSummary")){
					for (Object o : list){
						if (o instanceof ArrayList)
						{
							ArrayList<Object> each9200 = (ArrayList<Object>) o;
							if(each9200.size() == 4){
								description += "     "+each9200.get(0).toString()+"\n";
								noOfCalls += each9200.get(1).toString()+"\n";
								minutes += each9200.get(2).toString()+"\n";
								amount += each9200.get(3).toString()+"\n";
								}
						}
					}
				}
			} else if (eachValue instanceof Double) {
				Double floatValue = (Double) eachValue;
				totalCurrentCharges = floatValue;
			}
		}
		log.info("b4 try");
		response.setContentType("application/pdf");
		response.setHeader("Content-Disposition",
				"attachment;filename="+accountNumber+"_"+invoiceDate+".pdf");
		log.info("entering try");
		PdfReader reader;
		try {
			// We get a resource from our web app
			if(description.equals(""))
				reader = new PdfReader("templates/TemplateWith2pagesModified.pdf");
			else 
				reader = new PdfReader("templates/TemplateWith3pagesModified.pdf");
			log.info("Read file successfully");
			// We create an OutputStream for the new PDF
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			// Now we create the PDF
			PdfStamper stamper = new PdfStamper(reader, baos);
			log.info("Created file successfully");
			// We alter the fields of the existing PDF
			AcroFields form = stamper.getAcroFields();
			form.setField("accountNumber", accountNumber);
			form.setField("phoneNumber", phoneNumber);
			form.setField("invoiceDate", invoiceDate);
			form.setField("dueDate", dateDue);
			form.setField("totalDue", "$" + TotalDue[0]);
			form.setField("amountofLastStatement", TotalDue[1]);
			form.setField("paymentReceived", TotalDue[2]);
			form.setField("debitOrCredit", TotalDue[3]);
			form.setField("balanceForward", TotalDue[6]);
			form.setField("currentCharges", TotalDue[7]);
			form.setField("totalDueByDate", dateDue);
			form.setField("totalDueAfterDate", dateDue);
			form.setField("totalDueByDate", dateDue);
			form.setField("totalDueByDateAmount", TotalDue[8]);
			form.setField("totalDueAfterDateAmount", TotalDue[9]);
			if (summaryOfCharges[0] != null) {
				if (summaryOfCharges[0].trim().equals("Plan Overage Charges")) {
					form.setField("planOverageCharges", Double.toString(Double
							.parseDouble(summaryOfCharges[1])));
					if (summaryOfCharges[2] != null) {
						if (summaryOfCharges[2].trim().equals("Other Charges"))
							form.setField("otherCharges", Double
									.toString(Double
											.parseDouble(summaryOfCharges[3])));
					} else
						form.setField("otherCharges", "0.0");
				} else if (summaryOfCharges[0].trim().equals("Other Charges")) {
					form.setField("otherCharges", Double.toString(Double
							.parseDouble(summaryOfCharges[1])));
					form.setField("planOverageCharges", "0.0");
				}
			} else {
				form.setField("otherCharges", "0.0");
				form.setField("planOverageCharges", "0.0");
			}

			form.setField("address", addressString);
			form.setField("totalCurrentCharges",
					Double.toString(totalCurrentCharges));

			// page 2

			form.setField("accountName", accountName);
			form.setField("accountNumber", accountNumber);
			form.setField("invoiceDate", invoiceDate);

			// ----summary of charges------

			form.setField("datePage2", datePage2);
			form.setField("summary", summary);
			form.setField("nom", nom);
			form.setField("totals", charges);
			
			form.setField("Description3_1", description);
			form.setField("Calls3_1", noOfCalls);
			form.setField("Minutes3_1", minutes);
			form.setField("Amount3_1", amount);
			//page 3 completed
			stamper.setFormFlattening(true);
			stamper.close();
			reader.close();
			log.info("altered successfully");

			// We write the PDF bytes to the OutputStream
			ServletOutputStream os = response.getOutputStream();
			baos.writeTo(os);
			os.flush();
		} catch (DocumentException e) {
			throw new IOException(e.getMessage());
		}
	}
}
