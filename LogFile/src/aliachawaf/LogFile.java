package aliachawaf;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

public class LogFile {

	private String fileName;
	private List<CSVRecord> listLines;
	private List<String> nonMatching;
	private LogPattern patternMostMatching;
	private ListPatternLineMatching list;

	// constructor
	public LogFile(String fileName, ListPatternLineMatching list) {
		this.fileName = fileName;
		this.listLines = new ArrayList<CSVRecord>();
		this.nonMatching = new ArrayList<String>();
		this.list = list;
	}

	// getters
	public String getFileName() {
		return fileName;
	}
	
	public ListPatternLineMatching getList() {
		return list;
	}


	public List<CSVRecord> getListLines() {
		return listLines;
	}

	public List<String> getNonMatching() {
		return nonMatching;
	}

	// setter : we read the logfile and add its lines in the list of lines
	public boolean setListLines(char delimiter, int startLine, int finishLine) {

		boolean noMemory = false;

		try {
			Reader reader = Files.newBufferedReader(Paths.get(fileName));
			CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withDelimiter(delimiter));

			int i = 0;
			int j = 0;
			for (CSVRecord line : csvParser) {

				// we consider that when startLine and finishLine are both equal to -1, then we
				// have to analyse ALL the lines of the file
				if (startLine == -1 && finishLine == -1) {
					System.out.println(j);
					this.listLines.add(line);
					j++;
				} else if (finishLine == -1) {
					System.out.println(i);
					if (i >= startLine - 1) {
						this.listLines.add(line);
					}
					i++;
				} else {
					System.out.println(i);
					if (i >= startLine - 1 && i < finishLine) {
						this.listLines.add(line);
					}
					i++;
				}
			}

			// this.listLines = csvParser.getRecords();
			csvParser.close();

		} catch (java.nio.file.NoSuchFileException e) {
			System.out.print("File not found ! Please check your input (path) and re-enter it : ");
		} catch (java.nio.file.AccessDeniedException e) {
			System.out.print("File not found ! Please check your input (path) and re-enter it : ");
		} catch (java.nio.file.InvalidPathException e) {
			System.out.print("Invalid path ! Re-enter it : ");
		} catch (java.lang.OutOfMemoryError e) {
			System.out.print("no memoryyyyy !");
			noMemory = true;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return noMemory;
	}

	
	public int compare(ListRegexp listRegexp, char delimiter, ListLogPatterns listLogPatterns) {

		int nbLinesMatching = 0;
		boolean fieldMatches = true;
		boolean lineMatches = true;

		String regexNameExpected;
		String regexDefExpected;
		String infosLineNonMatching;

		try {
			Reader reader = Files.newBufferedReader(Paths.get(fileName));
			CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withDelimiter(delimiter));
int k =0;
			// we analyse each line of logfile
			for (CSVRecord line : csvParser) {
		System.out.println(k);		
				// make the comparison for each pattern
				for (LogPattern pattern : listLogPatterns.getListPatterns()) {

					//System.out.println(pattern.getLogInfos()[0]);
					
					// we compare only if the line has the same number of fields than the pattern
					if (line.size() == pattern.getListRegexName().size()) {

						// for each field of the current line, we check if it matches the regex expected
						for (int i = 0; i < line.size(); i++) {

							regexNameExpected = pattern.getListRegexName().get(i);

							// get the definition of the regex from its name
							regexDefExpected = listRegexp.getDefinitionByName(regexNameExpected);

							// compare the current field of the line with the pattern's regex expected
							fieldMatches = Pattern.matches(regexDefExpected, line.get(i));

							if (!fieldMatches) {

								/*
								 * if the field doesn't match, we record the infos of this line : line = (l + 1)
								 * column = (i + 1) pattern expected = pattern.getLogInfos()[0] +
								 * pattern.getLogInfos()[2] field expected = pattern.getListRegexName().get(i) =
								 * found = line.get(i)
								 */
								infosLineNonMatching = line.getRecordNumber() + " " + (i + 1) + " "
										+ pattern.getLogInfos()[0] + pattern.getLogInfos()[2]
										+ pattern.getListRegexName().get(i) + " " + line.get(i);

								this.getNonMatching().add(infosLineNonMatching);

								// if one field doesn't match so the line doesn't match too.
								lineMatches = false;
							}
						}

						if (lineMatches) {
							this.list.incrementNbLinesMatching(pattern);
							nbLinesMatching++;
						}

						// for the next line to analyse, we give initial values
						lineMatches = true;
						fieldMatches = true;
					}

				}
				k++;
				
			}

			csvParser.close();

		} catch (

		java.nio.file.NoSuchFileException e) {
			System.out.print("File not found ! Please check your input (path) and re-enter it : ");
		} catch (java.nio.file.AccessDeniedException e) {
			System.out.print("File not found ! Please check your input (path) and re-enter it : ");
		} catch (java.nio.file.InvalidPathException e) {
			System.out.print("Invalid path ! Re-enter it : ");
		} catch (IOException e) {
			e.printStackTrace();
		}

		//System.out.println(pattern.getLogInfos()[0] + pattern.getLogInfos()[2] + " nb lines matching : " + nbLinesMatching); 
		return nbLinesMatching;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	// depending on start and finish lines, we compare the lines of the logfile with
	// the pattern in parameter
	public int compareLogPattern(LogPattern pattern, ListRegexp listRegexp, char delimiter) {

		System.out.println("here");
		int nbLinesMatching = 0;
		boolean fieldMatches = true;
		boolean lineMatches = true;

		String regexNameExpected;
		String regexDefExpected;
		String infosLineNonMatching;

		try {
			Reader reader = Files.newBufferedReader(Paths.get(fileName), StandardCharsets.ISO_8859_1);
			CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withDelimiter(delimiter));

			// we analyse each line of logfile
			for (CSVRecord line : csvParser) {
				
				// we compare only if the line has the same number of fields than the pattern
				if (line.size() == pattern.getListRegexName().size()) {

					// for each field of the current line, we check if it matches the regex expected
					for (int i = 0; i < line.size(); i++) {

						regexNameExpected = pattern.getListRegexName().get(i);

						// get the definition of the regex from its name
						regexDefExpected = listRegexp.getDefinitionByName(regexNameExpected);

						// compare the current field of the line with the pattern's regex expected

						if (regexDefExpected!=null) {
							fieldMatches = Pattern.matches(regexDefExpected, line.get(i));
						} else {
							fieldMatches = false;
						}
						

						if (!fieldMatches) {

							/*
							 * if the field doesn't match, we record the infos of this line : line = (l + 1)
							 * column = (i + 1) pattern expected = pattern.getLogInfos()[0] +
							 * pattern.getLogInfos()[2] field expected = pattern.getListRegexName().get(i) =
							 * found = line.get(i)
							 */
							infosLineNonMatching = line.getRecordNumber() + " " + (i + 1) + " "
									+ pattern.getLogInfos()[0] + pattern.getLogInfos()[2]
									+ pattern.getListRegexName().get(i) + " " + line.get(i);

							this.getNonMatching().add(infosLineNonMatching);

							// if one field doesn't match so the line doesn't match too.
							lineMatches = false;
						}
					}

					if (lineMatches) {
						nbLinesMatching++;
					}

					// for the next line to analyse, we give initial values
					lineMatches = true;
					fieldMatches = true;
				}
			}

			csvParser.close();

		} catch (

		java.nio.file.NoSuchFileException e) {
			System.out.print("File not found ! Please check your input (path) and re-enter it : ");
		} catch (java.nio.file.AccessDeniedException e) {
			System.out.print("File not found ! Please check your input (path) and re-enter it : ");
		} catch (java.nio.file.InvalidPathException e) {
			System.out.print("Invalid path ! Re-enter it : ");
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println(pattern.getLogInfos()[0] + pattern.getLogInfos()[2] + " nb lines matching : " + nbLinesMatching); 
		return nbLinesMatching;
	}

	
	// depending on start and finish lines, we compare the logfile with ALL the
	// patterns of listLogPatterns
	public String compareAllLogPatterns(ListLogPatterns listLogPatterns, ListRegexp listRegexp, char delimiter) {

		System.out.println("begin");

		String result = "";
		int nbLinesMatching;
		int nbLinesProcessed;

		int nbMax = 0;

		nbLinesProcessed = this.listLines.size();

		// if the first line is a header line, we begin the analyse at the second line
		// and decrement the number of lines processed

		// if (startLine == 1 && this.hasHeaderLine(listLogPatterns, listRegexp)) {
		// startLine = 2;
		// nbLinesProcessed = nbLinesProcessed - 1;
		// }
		int i =0;
		// make the comparison for each pattern
		for (LogPattern pattern : listLogPatterns.getListPatterns()) {

			System.out.println(i + " " + pattern.getLogInfos()[0]);
			
			nbLinesMatching = this.compareLogPattern(pattern, listRegexp, delimiter);

			result = result + pattern.getLogInfos()[0] + pattern.getLogInfos()[2] + " : " + nbLinesMatching + " / "
					+ nbLinesProcessed + "\n";

			// to record the pattern matching the most
			if (nbLinesMatching > nbMax) {
				nbMax = nbLinesMatching;
				this.patternMostMatching = pattern;
			}
			i++;
		}

		System.out.println("begin");
		
		if (this.patternMostMatching != null) {
			result = "Pattern matching the most : " + this.patternMostMatching.getLogInfos()[0]
					+ this.patternMostMatching.getLogInfos()[2] + "\n\n" + result;
		}

		try {
			// record the non-matching lines in a new csv file
			this.recordNonMatchingLines(this.patternMostMatching, listRegexp);
		} catch (java.lang.NullPointerException e) {

			System.out.println("\nNo pattern is matching with any line !");
			result = "";
		}

		return result;
	}

	
	public void recordInfosNonMatchingLine(String file) {

		// CSV file header
		String[] FILE_HEADER = { "file name", "line", "column", "pattern expected", "field expected", "found" };

		FileWriter fileWriter = null;
		CSVPrinter csvFilePrinter = null;

		// Create the CSVFormat object with a header line
		CSVFormat csvFileFormat = CSVFormat.DEFAULT.withHeader(FILE_HEADER);

		try {

			// initialise FileWriter object
			fileWriter = new FileWriter(file);

			// initialise CSVPrinter object
			csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat);

			for (String record : this.nonMatching) {

				String[] field = record.split(" ", 5);

				List<String> data = Arrays.asList(this.fileName, field[0], field[1], field[2], field[3], field[4]);

				// Write the recordNonMatching to the CSV file
				csvFilePrinter.printRecord(data);
			}

			System.out.println("Write " + file + " successfully!");

		} catch (Exception e) {
			System.out.println("Writing CSV error!");
			e.printStackTrace();
		} finally {
			try {
				fileWriter.flush();
				fileWriter.close();
				csvFilePrinter.close();
			} catch (IOException e) {
				System.out.println("Flushing/closing error!");
				e.printStackTrace();
			}
		}
	}

	// record the non-matching lines in a new csv file only for the pattern which
	// matches the most
	public void recordNonMatchingLines(LogPattern pattern, ListRegexp listRegexp) {

		List<CSVRecord> list = new ArrayList<CSVRecord>();

		boolean matches = true;

		String regexNameExpected;
		String regexDefExpected;

		// we analyse each line of logfile
		// for (int l = startLine - 1; l < finishLine; l++) {

		for (CSVRecord line : this.listLines) {
			// CSVRecord line = this.listLines.get(l);

			// for each field of the current line, we check if it matches the regex expected
			for (int i = 0; i < line.size(); i++) {

				regexNameExpected = pattern.getListRegexName().get(i);
				regexDefExpected = listRegexp.getDefinitionByName(regexNameExpected);
				matches = Pattern.matches(regexDefExpected, line.get(i));

				if (!matches && !list.contains(line)) {
					list.add(line);
				}
			}
			matches = true;
		}

		FileWriter fileWriter = null;
		CSVPrinter csvFilePrinter = null;

		// Create the CSVFormat object
		CSVFormat csvFileFormat = CSVFormat.DEFAULT;

		try {

			// initialise FileWriter object
			fileWriter = new FileWriter("nonMatchingLines.csv");

			// initialise CSVPrinter object
			csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat);

			for (CSVRecord record : list) {

				csvFilePrinter.printRecord(record);
			}

			System.out.println("Write nonMatchingLines.csv successfully!");

		} catch (Exception e) {
			System.out.println("Writing CSV error!");
			e.printStackTrace();
		} finally {
			try {
				fileWriter.flush();
				fileWriter.close();
				csvFilePrinter.close();
			} catch (IOException e) {
				System.out.println("Flushing/closing error!");
				e.printStackTrace();
			}
		}
	}

	public boolean hasHeaderLine(ListLogPatterns listLogPatterns, ListRegexp listRegexp) {
		// returns true if the first line is header line, else returns false

		boolean matches = true;
		int nbPatternMatching = 0;

		String regexNameExpected;
		String regexDefExpected;

		CSVRecord firstLine = this.listLines.get(0);

		// for each pattern of the list
		for (LogPattern pattern : listLogPatterns.getListPatterns()) {

			if (firstLine.size() == pattern.getListRegexName().size()) {
				// for each field of the line
				for (int i = 0; i < firstLine.size(); i++) {

					if (matches && i < pattern.getListRegexName().size()) {

						// we get the regex we expect to match with from the current pattern
						regexNameExpected = pattern.getListRegexName().get(i);

						// we get the definition of the regex from its name
						regexDefExpected = listRegexp.getDefinitionByName(regexNameExpected);

						// compare the current field of the line with the pattern's regex expected
						matches = Pattern.matches(regexDefExpected, firstLine.get(i));
					}
				}

				if (matches) {
					nbPatternMatching++;
				} else {
					matches = true;
				}

			} else {
				matches = false;
			}
		}

		// if no pattern matches the first line, then it is a header line
		return (nbPatternMatching == 0);
	}
}