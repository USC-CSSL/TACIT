package edu.usc.cssl.tacit.crawlers.uscongress.services;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.swt.widgets.Display;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import edu.usc.cssl.tacit.common.ui.corpusmanagement.services.Corpus;
import edu.usc.cssl.tacit.common.ui.views.ConsoleView;

public class UsCongressCrawler {
	public int totalFilesDownloaded = 0;
	ArrayList<Integer> congresses = new ArrayList<Integer>();
	String dateFrom, dateTo;
	int maxDocs = 10;
	String outputDir;
	BufferedWriter csvWriter;
	String sortType;
	HashSet<String> irrelevantLinks = new HashSet<String>(
			Arrays.asList("Next Document", "New CR Search", "Prev Document", "HomePage", "Help", "GPO's PDF"));
	private ArrayList<String> congressMembers;
	private int congressNum;
	IProgressMonitor monitor;
	int progressSize;
	boolean isSenate;
	String crawlSenateRecords;
	String crawlHouseRepRecords;
	String crawlDailyDigest;
	String crawlExtension;
	public HashSet<String> filesDownload;
	boolean retryFlag, crawlAgain;
	int returnCode, counter;
	HashMap<String, HashMap<String, String>> congressSenatorMap = AvailableRecords.getCongressSenatorMap();
	HashMap<String, String> senatorDetails = SenatorDetails.getSenatorDetails(); // to
																					// populate
																					// all
																					// senator
																					// details

	HashMap<String, HashMap<String, String>> congressRepresentativeMap = AvailableRecords.getcongressRepMap();
	HashMap<String, String> representativeDetails = RepresentativeDetails.getRepersentativeDetails();

	private void formatMembersList() {
		ArrayList<String> tempMembers = new ArrayList<String>();
		for (String member : this.congressMembers) {
			tempMembers.add(member);
		}

		if (tempMembers.contains("All Senators")) {
			congressMembers.removeAll(congressMembers);
			congressMembers.add("All Senators");
		} else if (tempMembers.contains("All Representatives")) {
			congressMembers.removeAll(congressMembers);
			congressMembers.add("All Representatives");
		} else {
			if (tempMembers.contains("All Democrats")) {
				// remove all the remaining democrats
				for (Iterator<String> it = tempMembers.iterator(); it.hasNext();) {
					String s = it.next();
					if (s.contains("(D-") || s.contains("D/") || s.contains("[D-")) {
						it.remove();
					}
				}
			}
			if (tempMembers.contains("All Republicans")) {
				for (Iterator<String> it = tempMembers.iterator(); it.hasNext();) {
					String s = it.next();
					if (s.contains("(R-") || s.contains("R/") || s.contains("([R-")) {
						it.remove();
					}
				}
			}
			if (tempMembers.contains("All Independents")) {
				for (Iterator<String> it = tempMembers.iterator(); it.hasNext();) {
					String s = it.next();
					if (s.contains("(I-") || s.contains("I/") || s.contains("[I-")) {
						it.remove();
					}
				}
			}
		}
		this.congressMembers = tempMembers;
	}

	public void crawl() throws IOException {
		if (null != monitor && monitor.isCanceled()) {
			monitor.subTask("Cancelling.. ");
			return;
		}
		returnCode =0;

		formatMembersList();

		DateFormat df = new SimpleDateFormat("MM-dd-yyyy-HH-mm-ss");
		Date dateobj = new Date();

		csvWriter = new BufferedWriter(new FileWriter(new File(outputDir + System.getProperty("file.separator")
				+ "congress-crawler-summary-" + df.format(dateobj) + ".csv")));
		if (isSenate)
			csvWriter.write("Congress,Date,Senator,Political Affiliation,Congressional Section,State,Title,File");
		else
			csvWriter.write(
					"Congress,Date,Representative,Political Affiliation,Congressional Section,State,District,Title,File");
		csvWriter.newLine();

		for (int i = 0; i < congressMembers.size(); i++) {
			String memberText = congressMembers.get(i);
			System.out.println(memberText + i);
			// try{
			int tempProgressSize = progressSize / congressMembers.size();
			if (memberText.equals("All Representatives") || memberText.equals("All Senators")
					|| memberText.equals("All Republicans") || memberText.equals("All Democrats")
					|| memberText.equals("All Independents")) {
				if (congressNum != -1) {
					if (null != monitor && monitor.isCanceled()) {
						monitor.subTask("Cancelling.. ");
						return;
					}
					if (isSenate)
						getAllSenators(congressNum, memberText, tempProgressSize);
					else
						getAllRepresentatives(congressNum, memberText, tempProgressSize);
				} else {
					for (int congress : congresses) {
						if (null != monitor && monitor.isCanceled()) {
							monitor.subTask("Cancelling.. ");
							return;
						}
						if (isSenate)
							getAllSenators(congress, memberText, tempProgressSize / congresses.size());
						else
							getAllRepresentatives(congress, memberText, tempProgressSize / congresses.size());
					}
				}
				if (null != monitor && monitor.isCanceled()) {
					monitor.subTask("Cancelling.. ");
					return;
				}
			} else {
				String politicalAffiliation = "";
				if (memberText.lastIndexOf('(') != -1) {
					String affiliation = memberText.substring(memberText.lastIndexOf('(') + 1, memberText.length() - 1);
					if (-1 != affiliation.indexOf('-'))
						politicalAffiliation = affiliation.split("-")[0];
					else
						politicalAffiliation = (isSenate) ? senatorDetails.get(memberText).split("-")[0]
								: representativeDetails.get(memberText).split("-")[0];
				}

				if (congressNum == -1) { // All congress
					for (int congress : congresses) {
						if (null != monitor && monitor.isCanceled()) {
							monitor.subTask("Cancelling.. ");
							return;
						}
						System.out.println("Extracting Records from Congress " + congress + "...");
						String memberName = (isSenate)
								? congressSenatorMap.get(String.valueOf(congress)).get(memberText)
								: congressRepresentativeMap.get(String.valueOf(congress)).get(memberText);
						if (null != memberName) {
							// Data can be added here for corpus
							while (true) {
								try {
									if(returnCode ==1)
										break;
									if (isSenate) {
										searchRecords(congress, memberName, "", tempProgressSize / congresses.size(),
												politicalAffiliation);
										break;
									} else {
										searchRecords(congress, "", memberName, tempProgressSize / congresses.size(),
												politicalAffiliation);
										break;
									}
								} catch (Exception e) {
									System.out.println(e.getMessage() + " ");
									Display.getDefault().syncExec(new Runnable() {
										@Override
										public void run() {
											String[] labels = new String[] { IDialogConstants.OK_LABEL,
													IDialogConstants.CANCEL_LABEL };
											if (!retryFlag) {
												MessageDialogWithToggle dialog = new MessageDialogWithToggle(
														Display.getDefault().getActiveShell(), "Time out", null,
														"You must've lost internet connection, re-establish connection and try again!",
														MessageDialog.INFORMATION, labels, 0, "Retry Automatically",
														false);
												returnCode = dialog.open();
												retryFlag = dialog.getToggleState();

											}
											if (!retryFlag && returnCode == 1) {
												monitor.setCanceled(true);
											} else {
												crawlAgain = true;
											}
											if (retryFlag) {
												counter += 1;
												if (counter > 500)
													retryFlag = false;
											}
										}
									});
								}
							}
						}
					}
				} else {
					if (null != monitor && monitor.isCanceled()) {
						monitor.subTask("Cancelling.. ");
						return;
					}
					System.out.println("Extracting Records from Congress " + congressNum + "...");
					String memberName = (isSenate) ? congressSenatorMap.get(String.valueOf(congressNum)).get(memberText)
							: congressRepresentativeMap.get(String.valueOf(congressNum)).get(memberText);
					if (null != memberName) {
						while (true) {
							try {
								if(returnCode ==1)
									break;
						if (isSenate){
							searchRecords(congressNum, memberName, "", tempProgressSize, politicalAffiliation);
							break;}
						else{
							searchRecords(congressNum, "", memberName, tempProgressSize, politicalAffiliation);
							break;}
							}
							catch (Exception e) {
								System.out.println(e.getMessage() + " ");
								Display.getDefault().syncExec(new Runnable() {
									@Override
									public void run() {
										String[] labels = new String[] { IDialogConstants.OK_LABEL,
												IDialogConstants.CANCEL_LABEL };
										if (!retryFlag) {
											MessageDialogWithToggle dialog = new MessageDialogWithToggle(
													Display.getDefault().getActiveShell(), "Time out", null,
													"You must've lost internet connection, re-establish connection and try again!",
													MessageDialog.INFORMATION, labels, 0, "Retry Automatically",
													false);
											returnCode = dialog.open();
											retryFlag = dialog.getToggleState();

										}
										if (!retryFlag && returnCode == 1) {
											monitor.setCanceled(true);
										} else {
											crawlAgain = true;
										}
										if (retryFlag) {
											counter += 1;
											if (counter > 500)
												retryFlag = false;
										}
									}
								});
							}
						}
					}
				}
				if (null != monitor && monitor.isCanceled()) {
					monitor.subTask("Cancelling.. ");
					return;
				}
			}
		}

		csvWriter.close();

	}

	int currentProgress = 0; // stores the progress to be updated for the
								// current iteration
	float changeProgress = 0; // used in case the current progress value is less
								// than 1 for the current iteration
	int republican, democrat, independent; // store the count of all democrats,
											// republicans and independents for
											// a current congress.

	public void getAllSenators(int congressNum, String senText, int maxProgressLimit) throws IOException {
		boolean foundSenator = false;
		republican = 0;
		democrat = 0;
		independent = 0;
		// counts the total number of democrats, republics and independents
		if (senText.contains("All Republicans") || senText.contains("All Democrats")
				|| senText.contains("All Independents")) {
			for (String senator : congressSenatorMap.get(String.valueOf(congressNum)).keySet()) {
				String senatorName = senator;
				if (null != monitor && monitor.isCanceled())
					return;
				if (senatorName.contains("(R-") || senatorName.contains("R/") || (senatorName.contains("[R-")))
					republican++;
				if (senatorName.contains("(D-") || senatorName.contains("D/") || (senatorName.contains("[D-")))
					democrat++;
				if (senatorName.contains("(I-") || senatorName.contains("I/") || (senatorName.contains("[I-")))
					independent++;
			}
		}

		for (String senator : congressSenatorMap.get(String.valueOf(congressNum)).keySet()) {
			String senatorName = senator;
			if (null != monitor && monitor.isCanceled())
				return;
			if (senator.contains("Any Senator")) // We just need the senator
													// names
				continue;
			if (senText.contains("All Republicans")) {
				if (!senatorName.contains("(R-") && !senatorName.contains("R/") && !senatorName.contains("[R-"))
					continue;
			}
			if (senText.contains("All Democrats")) {
				if (!senatorName.contains("(D-") && !senatorName.contains("D/") && !senatorName.contains("[D-"))
					continue;
			}
			if (senText.contains("All Independents")) {
				if (!senatorName.contains("(I-") && !senatorName.contains("I/") && !senatorName.contains("[I-"))
					continue;
			}
			String politicalAffiliation = "";
			if (senatorName.lastIndexOf('(') != -1) {
				String affiliation = senatorName.substring(senatorName.lastIndexOf('(') + 1, senatorName.length() - 1);
				if (-1 != affiliation.indexOf('-'))
					politicalAffiliation = affiliation.split("-")[0];
				else
					politicalAffiliation = senatorDetails.get(senatorName).split("-")[0];
			}
			// update the current progress
			if (null != congressSenatorMap.get(String.valueOf(congressNum)).get(senator)) {
				if (senText.contains("All Democrats")) {
					if (democrat != 0)
						currentProgress = maxProgressLimit / democrat;
					else
						currentProgress = 0;
				} else if (senText.contains("All Republicans")) {
					if (republican != 0)
						currentProgress = maxProgressLimit / republican;
					else
						currentProgress = 0;
				} else if (senText.contains("All Independents")) {
					if (independent != 0)
						currentProgress = maxProgressLimit / independent;
					else
						currentProgress = 0;
				} else {

					int val = congressSenatorMap.get(String.valueOf(congressNum)).keySet().size();
					if (maxProgressLimit / congressSenatorMap.get(String.valueOf(congressNum)).keySet().size() < 1) {
						changeProgress += (float) (maxProgressLimit)
								/ congressSenatorMap.get(String.valueOf(congressNum)).keySet().size();
						if (changeProgress > 1) {
							currentProgress = (int) changeProgress;
							changeProgress -= currentProgress;
						}

					} else {
						currentProgress = (maxProgressLimit) / val;
					}
				}
				while (true) {
					try {
						if(returnCode ==1)
							break;
						searchRecords(congressNum, congressSenatorMap.get(String.valueOf(congressNum)).get(senator), "",
								currentProgress, politicalAffiliation);
						break;
					} catch (Exception e) {
						System.out.println(e.getMessage() + " ");
						Display.getDefault().syncExec(new Runnable() {
							@Override
							public void run() {
								String[] labels = new String[] { IDialogConstants.OK_LABEL,
										IDialogConstants.CANCEL_LABEL };
								if (!retryFlag) {
									MessageDialogWithToggle dialog = new MessageDialogWithToggle(
											Display.getDefault().getActiveShell(), "Time out", null,
											"You must've lost internet connection, re-establish connection and try again!",
											MessageDialog.INFORMATION, labels, 0, "Retry Automatically", false);
									returnCode = dialog.open();
									retryFlag = dialog.getToggleState();

								}
								if (!retryFlag && returnCode == 1) {
									monitor.setCanceled(true);
									
								} else {
									crawlAgain = true;
								}
								if (retryFlag) {
									counter += 1;
									if (counter > 500)
										retryFlag = false;
								}
							}
						});
					}
				}
				foundSenator = true;
			}
		}
		if (!foundSenator) {
			if (senText.contains("All Republicans")) {
				ConsoleView.printlInConsoleln("No republicans found");
			} else if (senText.contains("All Democrats")) {
				ConsoleView.printlInConsoleln("No democrats found");
			} else if (senText.contains("All Independents")) {
				ConsoleView.printlInConsoleln("No independents found");
			} else {
				ConsoleView.printlInConsoleln("No senators found");
			}
		}
	}

	public void getAllRepresentatives(int congressNum, String repText, int maxProgressLimit) throws IOException {
		boolean foundRep = false;
		republican = 0;
		democrat = 0;
		independent = 0;
		if (repText.contains("All Republicans") || repText.contains("All Democrats")
				|| repText.contains("All Independents")) {
			for (String representative : congressRepresentativeMap.get(String.valueOf(congressNum)).keySet()) {
				String repName = representative;
				if (null != monitor && monitor.isCanceled())
					return;
				if (repName.contains("(R-") || repName.contains("R/") || repName.contains("[R-"))
					republican++;
				if (repName.contains("(D-") || repName.contains("D/") || repName.contains("[D-"))
					democrat++;
				if (repName.contains("(I-") || repName.contains("I/") || repName.contains("[I-"))
					independent++;
			}
		}
		for (String rep : congressRepresentativeMap.get(String.valueOf(congressNum)).keySet()) {
			String repName = rep;
			if (null != monitor && monitor.isCanceled())
				return;
			if (rep.contains("Any Representative")) // We just need the senator
													// names
				continue;
			if (repText.contains("All Republicans")) {
				if (!repName.contains("(R-") && !repName.contains("R/") && !repName.contains("[R-"))
					continue;
			}
			if (repText.contains("All Democrats")) {
				if (!repName.contains("(D-") && !repName.contains("D/") && !repName.contains("[D-"))
					continue;
			}
			if (repText.contains("All Independents")) {
				if (!repName.contains("(I-") && !repName.contains("I/") && !repName.contains("[I-"))
					continue;
			}
			String politicalAffiliation = "";
			if (repName.lastIndexOf('(') != -1) {
				String affiliation = repName.substring(repName.lastIndexOf('(') + 1, repName.length() - 1);
				if (-1 != affiliation.indexOf('-'))
					politicalAffiliation = affiliation.split("-")[0];
				else
					politicalAffiliation = representativeDetails.get(repName).split("-")[0];
			}
			if (null != congressRepresentativeMap.get(String.valueOf(congressNum)).get(rep)) {
				if (repText.contains("All Democrats")) {
					if (democrat != 0)
						currentProgress = maxProgressLimit / democrat;
					else
						currentProgress = 0;
				} else if (repText.contains("All Republicans")) {
					if (republican != 0)
						currentProgress = maxProgressLimit / republican;
					else
						currentProgress = 0;
				} else if (repText.contains("All Independents")) {
					if (independent != 0)
						currentProgress = maxProgressLimit / independent;
					else
						currentProgress = 0;
				} else {
					int val = congressRepresentativeMap.get(String.valueOf(congressNum)).keySet().size();
					if (maxProgressLimit
							/ congressRepresentativeMap.get(String.valueOf(congressNum)).keySet().size() < 1) {
						changeProgress += (float) (maxProgressLimit)
								/ congressRepresentativeMap.get(String.valueOf(congressNum)).keySet().size();
						if (changeProgress > 1) {
							currentProgress = (int) changeProgress;
							changeProgress -= currentProgress;
						}

					} else {
						currentProgress = (maxProgressLimit) / val;
					}
				}
				while (true) {
					try {
						if(returnCode ==1)
							break;
						searchRecords(congressNum, "",
								congressRepresentativeMap.get(String.valueOf(congressNum)).get(rep), currentProgress,
								politicalAffiliation);
						break;
					} catch (Exception e) {
						System.out.println(e.getMessage() + " ");
						Display.getDefault().syncExec(new Runnable() {
							@Override
							public void run() {
								String[] labels = new String[] { IDialogConstants.OK_LABEL,
										IDialogConstants.CANCEL_LABEL };
								if (!retryFlag) {
									MessageDialogWithToggle dialog = new MessageDialogWithToggle(
											Display.getDefault().getActiveShell(), "Time out", null,
											"You must've lost internet connection, re-establish connection and try again!",
											MessageDialog.INFORMATION, labels, 0, "Retry Automatically", false);
									returnCode = dialog.open();
									retryFlag = dialog.getToggleState();
									System.out.println(returnCode + "+++++++++++++++");

								}
								if (!retryFlag && returnCode == 1) {
									monitor.setCanceled(true);
								} else {
									crawlAgain = true;
								}
								if (retryFlag) {
									counter += 1;
									if (counter > 500)
										retryFlag = false;
								}
							}
						});
					}
				}
				foundRep = true;
			}

		}
		if (!foundRep) {
			if (repText.contains("All Republicans")) {
				ConsoleView.printlInConsoleln("No republicans found");
			} else if (repText.contains("All Democrats")) {
				ConsoleView.printlInConsoleln("No democrats found");
			} else if (repText.contains("All Independents")) {
				ConsoleView.printlInConsoleln("No independents found");
			} else {
				ConsoleView.printlInConsoleln("No representatives found");
			}
		}
	}

	public void initialize(String sortType, int maxDocs, int congressNum, ArrayList<String> congressMemberDetails,
			String dateFrom, String dateTo, String outputDir, ArrayList<Integer> allCongresses,
			IProgressMonitor monitor, int progressSize, boolean isSenate, boolean crawlSenateRecords,
			boolean crawlHouseRepRecords, boolean crawlDailyDigest, boolean crawlExtension) throws IOException {
		this.outputDir = outputDir;
		this.maxDocs = maxDocs;
		this.dateFrom = dateFrom;
		this.dateTo = dateTo;
		filesDownload = new HashSet<String>();
		this.congressMembers = congressMemberDetails;
		this.congressNum = congressNum;
		this.sortType = sortType;
		this.congresses = allCongresses;
		this.monitor = monitor;
		this.progressSize = progressSize;
		this.isSenate = isSenate;
		this.crawlSenateRecords = (crawlSenateRecords) ? "1" : "0";
		this.crawlHouseRepRecords = (crawlHouseRepRecords) ? "2" : "0";
		this.crawlExtension = (crawlExtension) ? "4" : "0";
		this.crawlDailyDigest = (crawlDailyDigest) ? "8" : "0";
		totalFilesDownloaded = 0;

		if (null != monitor && monitor.isCanceled()) {
			monitor.subTask("Cancelling.. ");
			return;
		}
	}

	public void searchRecords(int congress, String senText, String repText, int progressSize,
			String politicalAffiliation) throws IOException, NullPointerException {
		if ((null == senText || senText.isEmpty()) && (null == repText || repText.isEmpty()))
			return;
		String memText = (null == senText || senText.isEmpty()) ? repText : senText;
		ConsoleView.printlInConsoleln("Current Congress Member - " + memText);
		String memberDir = this.outputDir + File.separator + memText.replaceAll("[\\/:*?\"<>|]+", "");
		if (!new File(memberDir).exists()) {
			new File(memberDir).mkdir();
		}

		if (null != monitor && !monitor.isCanceled()) {
			monitor.subTask("Crawling data for " + memText + "...");
		}
		Document doc = Jsoup.connect("http://thomas.loc.gov/cgi-bin/thomas2").data("xss", "query") // Important.
																									// If
																									// removed,
																									// "301
																									// Moved
																									// Permanently"
																									// error
																									// //
																									// If
				.data("queryr" + congress, "") // Important. 113 - congress
												// number. Make this auto? if
												// removed, "Database Missing"
												// error
				.data("MaxDocs", "2000") // Doesn't seem to be working
				.data("Stemming", "No").data("HSpeaker", repText).data("SSpeaker", senText).data("member", "speaking")
				.data("relation", "or") // or | and -- when there are multiple
										// speakers in the query
				.data("SenateSection", crawlSenateRecords).data("HouseSection", crawlHouseRepRecords)
				.data("ExSection", crawlExtension).data("DigestSection", crawlDailyDigest).data("LBDateSel", "Thru")
				.data("DateFrom", dateFrom).data("DateTo", dateTo).data("sort", sortType).data("submit", "SEARCH")
				.userAgent("Mozilla").timeout(15 * 1000).post();
		Elements links;
		try {
			links = doc.getElementById("content").getElementsByTag("a");
		} catch (NullPointerException ne) {
			if (null != senText || !senText.isEmpty())
				ConsoleView.printlInConsoleln("*** No data found for " + senText);
			else
				ConsoleView.printlInConsoleln("*** No data found for " + repText);
			return;
		}

		// Extracting the relevant links
		Elements relevantLinks = new Elements();
		for (Element link : links) {
			if (!irrelevantLinks.contains(link.text()))
				if (link.text().contains("Senate") || link.text().contains("House of Representatives")
						|| link.text().contains("Extensions of Remarks") || link.text().contains("Daily Digest"))
					relevantLinks.add(link);
		}

		String memberAttribs = memText.substring(memText.lastIndexOf('(') + 1, memText.length() - 1).trim();
		// String memberAttribs = memText.split("\\(")[1].replace(")",
		// "").trim();
		String memberState = memberAttribs;
		String district = "NA";
		if (-1 != memberAttribs.indexOf('-')) {
			String[] temp = memberAttribs.split("-");
			memberState = temp[1];
			if (temp.length >= 3)
				district = temp[2];
		}
		String lastName = memText.split(",")[0];
		String[] tempName = (memText.lastIndexOf('(') != -1) ? memText.substring(0, memText.lastIndexOf('(')).split(",")
				: lastName.split(",");
		// String tempRepName = StringUtil.join(Arrays.asList(tempName),";");
		String tempRepName = tempName[1] + " " + tempName[0];

		if (relevantLinks.size() == 0) {
			ConsoleView.printlInConsoleln("No Records Found.");
			if (isSenate)
				csvWriter.write(congress + "," + "NA" + "," + tempRepName + "," + politicalAffiliation + "," + "NA"
						+ "," + memberState + "," + "NA" + "," + "No records found");
			else
				csvWriter.write(congress + "," + "NA" + "," + tempRepName + "," + politicalAffiliation + "," + "NA"
						+ "," + memberState + "," + district + "," + "NA" + "," + "No records found");
			csvWriter.newLine();
			csvWriter.flush();
			return;
		}

		links = relevantLinks;

		int count = 0;
		int tempCount = 0;
		// Process each search result
		for (Element link : links) {
			if (null != monitor && monitor.isCanceled()) {
				monitor.subTask("Cancelling.. ");
				return;
			}
			if (maxDocs == -1)
				count = -2000;
			if (count++ >= maxDocs)
				break;
			String recordDate = "";
			String recordType = "";
			if (link.text().contains("Senate")) {
				recordDate = link.text().replace("(Senate - ", "").replace(",", "").replace(")", "").trim();
				recordType = "Senate";
			} else if (link.text().contains("House of Representatives")) {
				recordDate = link.text().replace("(House of Representatives - ", "").replace(",", "").replace(")", "")
						.trim();
				recordType = "House";
			} else if (link.text().contains("Extensions of Remarks")) {
				recordDate = link.text().replace("(Extensions of Remarks - ", "").replace(",", "").replace(")", "")
						.trim();
				recordType = "Extension of Remarks";
			} else if (link.text().contains("Daily Digest")) {
				recordDate = link.text().replace("(Daily Digest - ", "").replace(",", "").replace(")", "").trim();
				recordType = "Daily Digest";
			}

			Document record = Jsoup.connect("http://thomas.loc.gov" + link.attr("href")).timeout(10 * 1000).get();
			Elements tabLinks = record.getElementById("content").select("a[href]");

			String extractLink = "";
			for (Element tabLink : tabLinks) {
				if (tabLink.text().equals("Printer Friendly Display")) {
					extractLink = tabLink.attr("href");
					break;
				}
			}

			String[] contents = extract(extractLink, lastName);

			if (contents[1].length() == 0)
				count--;
			else {
				String[] split = contents[0].split("-");
				String title = split[0].trim();
				title = title.replaceAll(",", "");
				title = title.replaceAll("\\.", "");
				String shortTitle = title;
				if (title.length() > 15)
					shortTitle = title.substring(0, 15).trim().replaceAll("[^\\w\\s]", "");
				shortTitle.replaceAll("[.,;\"!-(){}:?'/\\`~$%#@&*_=+<>]", ""); // replaces
																				// all
																				// special
																				// characters
				String fileName = congress + "-" + tempRepName + "-" + memberAttribs + "-" + recordDate + "-"
						+ shortTitle + "-" + (System.currentTimeMillis() % 1000) + ".txt";
				writeToFile(memberDir, fileName, contents);
				if (isSenate)
					csvWriter.write(congress + "," + recordDate + "," + tempRepName + "," + politicalAffiliation + ","
							+ recordType + "," + memberState + "," + title + "," + fileName);
				else
					csvWriter.write(congress + "," + recordDate + "," + tempRepName + "," + politicalAffiliation + ","
							+ recordType + "," + memberState + "," + district + "," + title + "," + fileName);
				csvWriter.newLine();
				csvWriter.flush();
			}

			tempCount++;
			tempCount = updateWork(maxDocs, links.size(), progressSize, tempCount);
		}
	}

	private int updateWork(int maxDocs, int totalLinks, int progressSize, int tempCount) {
		int tempMaxDocs = maxDocs == -1 ? 2000 : maxDocs;
		int numDocs2Download = tempMaxDocs > totalLinks ? totalLinks : tempMaxDocs;
		if (tempCount == numDocs2Download) {
			tempCount = 0;
			monitor.worked(progressSize);
		}
		return tempCount;
	}

	private void writeToFile(String senatorOutputDir, String fileName, String[] contents) throws IOException {
		// ConsoleView.printlInConsoleln("Writing senator data - "+fileName);
		fileName = fileName.replaceAll("[\\/:*?\"<>|]+", "");
		String name = fileName.substring(0, fileName.lastIndexOf("-"));
		if (filesDownload.contains(name))
			return;
		filesDownload.add(name);
		ConsoleView.printlInConsoleln("Writing " + senatorOutputDir + File.separator + fileName);
		BufferedWriter bw = new BufferedWriter(
				new FileWriter(new File(senatorOutputDir + System.getProperty("file.separator") + fileName)));
		bw.write(contents[0]);
		bw.newLine();
		bw.newLine();
		bw.write(contents[1]);
		bw.close();
		totalFilesDownloaded++;
	}

	private String[] extract(String extractLink, String lastName) throws IOException {
		Document page = Jsoup.connect("http://thomas.loc.gov" + extractLink).timeout(10 * 1000).get();

		String title = page.getElementById("container").select("b").text();
		StringBuilder content = new StringBuilder();
		/*
		 * Elements lines = page.getElementById("container").select("p"); String
		 * currentLine; boolean extractFlag = false; for (Element line : lines)
		 * { currentLine = line.text().trim(); if (currentLine!=null &&
		 * !currentLine.isEmpty()){ String[] words =
		 * currentLine.replaceAll("\u00A0", "").trim().split(" "); if
		 * (words.length>1){ String currentName = words[1].trim().replace(".",
		 * ""); // Check the second word of the sentence. currentName =
		 * currentName.replace(",", ""); String firstWord =
		 * words[0].trim().replace(".", "");
		 * 
		 * if (currentName.equals(lastName.toUpperCase())) { // Found senator
		 * dialogue extractFlag = true;
		 * content.append(currentLine.replace("\u00A0", "").trim()+"\n"); } else
		 * { // If first word is uppercase too, stop extracting. if
		 * (firstWord.length()<=1 && !firstWord.equals(firstWord.toUpperCase())
		 * ){ extractFlag = false; } // If "I", continue extracting. if
		 * (!currentName.equals("I") && !isNumeric(currentName) &&
		 * currentName.equals(currentName.toUpperCase())){ // Next speaker.
		 * extractFlag = false; } // if already extracting, continue until end
		 * of file or until next speaker's dialogue if (extractFlag)
		 * content.append(currentLine.replace("\u00A0", "").trim()+"\n"); } } }
		 * }
		 */
		String[] contents = new String[2];
		contents[0] = title;
		contents[1] = page.getElementById("container").text();

		return contents;
	}

	private boolean isNumeric(String word) {
		try {
			Integer.parseInt(word);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

}
