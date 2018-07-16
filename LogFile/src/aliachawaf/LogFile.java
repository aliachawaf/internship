package aliachawaf;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
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

	// constructor
	public LogFile(String fileName) {
		this.fileName = fileName;
		this.listLines = new ArrayList<CSVRecord>();
		this.nonMatching = new ArrayList<String>();
	}

	// getters & setters
	public String getFileName() {
		return fileName;
	}

	public List<CSVRecord> getListLines() {
		return listLines;
	}

	public List<String> getNonMatching() {
		return nonMatching;
	}

	// read the logfile and add its lines in the list
	public void setListLines(char delimiter) {

		try {
			Reader reader = Files.newBufferedReader(Paths.get(fileName));
			CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withDelimiter(delimiter));

			for (CSVRecord line : csvParser) {

				this.listLines.add(line);
			}

			csvParser.close();

		} catch (java.nio.file.NoSuchFileException e) {
			System.out.print("File not found ! Please check your input (path) and re-enter it : ");
		} catch (java.nio.file.AccessDeniedException e) {
			System.out.print("File not found ! Please check your input (path) and re-enter it : ");
		} catch (java.nio.file.InvalidPathException e) {
			System.out.print("Invalid path ! Re-enter it : ");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// compare each lines of the logfile with the pattern entered as parameter
	public int compareLogPattern(LogPattern pattern, ListRegexp listRegexp, int startLine, int finishLine) {

		int nbLinesMatching = 0;
		boolean matches = true;
		boolean lineMatches = true;

		String regexNameExpected;
		String regexDefExpected;
		String lineNonMatching;

		// we analyse each line of logfile
		for (int l = startLine - 1; l < finishLine; l++) {

			CSVRecord line = this.listLines.get(l);
			// for (CSVRecord line : this.listLines) {

			if (line.size() == pattern.getListRegexName().size()) {

				// for each field of the current line, we check if it matches the regex expected
				for (int i = 0; i < line.size(); i++) {

					regexNameExpected = pattern.getListRegexName().get(i);

					// we get the definition of the regex from its name
					regexDefExpected = listRegexp.getDefinitionByName(regexNameExpected);

					// compare the current field of the line with the pattern's regex expected
					matches = Pattern.matches(regexDefExpected, line.get(i));

					if (!matches) {

						lineNonMatching = (l + 1) + " " + (i + 1) + " " + pattern.getLogInfos()[0]
								+ pattern.getLogInfos()[2] + pattern.getListRegexName().get(i) + " " + line.get(i);

						this.getNonMatching().add(lineNonMatching);
						lineMatches = false;
					}
				}

				if (lineMatches) {
					nbLinesMatching++;
				} else {
					// for the next line to analyse, we give matches its initial value
					lineMatches = true;
				}

				matches = true;

			}
		}
		return nbLinesMatching;
	}

	// compare all the lines of the logfile with all the patterns of listLogPatterns
	public String compareAllLogPatterns(ListLogPatterns listLogPatterns, ListRegexp listRegexp, int startLine,
			int finishLine) {

		String result = "";
		int nbLinesMatching;
		int nbLinesProcessed;

		int nbMax = 0;

		// we consider that when startLine and finishLine are both equal to -1, then we
		// have to analyse ALL the lines of the file
		if (startLine == -1 && finishLine == -1) {
			nbLinesProcessed = this.listLines.size();
			startLine = 1;
			finishLine = this.listLines.size();
		} else if (finishLine > this.listLines.size()) {
			finishLine = this.listLines.size();
			nbLinesProcessed = finishLine - startLine + 1;
		} else {
			nbLinesProcessed = finishLine - startLine + 1;
		}

		if (startLine == 1 && this.hasHeaderLine(listLogPatterns, listRegexp)) {
			startLine = 2;
			nbLinesProcessed = nbLinesProcessed - 1;
		}

		for (LogPattern pattern : listLogPatterns.getListPatterns()) {

			nbLinesMatching = this.compareLogPattern(pattern, listRegexp, startLine, finishLine);

			result = result + pattern.getLogInfos()[0] + pattern.getLogInfos()[2] + " : " + nbLinesMatching + " / "
					+ nbLinesProcessed + "\n";

			if (nbLinesMatching > nbMax) {
				nbMax = nbLinesMatching;
				this.patternMostMatching = pattern;
			}
		}

		if (this.patternMostMatching != null) {
			result = "Pattern matching the most : " + this.patternMostMatching.getLogInfos()[0]
					+ this.patternMostMatching.getLogInfos()[2] + "\n\n" + result;
		}

		try {
			this.recordNonMatchingLines(this.patternMostMatching, listRegexp, startLine, finishLine);
		} catch (java.lang.NullPointerException e) {

			System.out.println("No pattern is matching !");
			result = "";
		}
		
		return result;
	}

	// record the non-matching lines only for the pattern which matches the most
	public void recordNonMatchingLines(LogPattern pattern, ListRegexp listRegexp, int startLine, int finishLine) {

		List<CSVRecord> list = new ArrayList<CSVRecord>();

		boolean matches = true;

		String regexNameExpected;
		String regexDefExpected;

		// we analyse each line of logfile
		for (int l = startLine - 1; l < finishLine; l++) {

			CSVRecord line = this.listLines.get(l);

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

		// Create the CSVFormat object with "\n" as a record delimiter
		CSVFormat csvFileFormat = CSVFormat.DEFAULT;

		try {

			// initialise FileWriter object
			fileWriter = new FileWriter("linesss.csv");

			// initialise CSVPrinter object
			csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat);

			for (CSVRecord record : list) {

				// Write the recordNonMatching to the CSV file
				csvFilePrinter.printRecord(record);
			}

			System.out.println("Write CSV successfully!");

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

	public void recordInfosNonMatchingLine(String file) {

		// CSV file header
		String[] FILE_HEADER = { "file name", "line", "column", "pattern expected", "field expected", "found" };

		FileWriter fileWriter = null;
		CSVPrinter csvFilePrinter = null;

		// Create the CSVFormat object with "\n" as a record delimiter
		CSVFormat csvFileFormat = CSVFormat.DEFAULT.withHeader(FILE_HEADER);

		try {

			// initialise FileWriter object
			fileWriter = new FileWriter(file + ".csv");

			// initialise CSVPrinter object
			csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat);

			for (String record : this.nonMatching) {

				String[] field = record.split(" ", 5);

				List<String> data = Arrays.asList(this.fileName, field[0], field[1], field[2], field[3], field[4]);

				// Write the recordNonMatching to the CSV file
				csvFilePrinter.printRecord(data);
			}

			System.out.println("Write CSV successfully!");

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

		// if non pattern matches the first line, then it is a header line
		return (nbPatternMatching == 0);
	}
}