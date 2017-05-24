package com.wid;

import java.net.*;
import java.util.*;
import java.util.regex.*;
import java.io.*;
import com.stata.sfi.*;

public class WIDDownloader {
	
	private static String apiKey = "<API key>";
	
	private static String apiCountriesAvailableVariables = "https://rfap9nitz6.execute-api.eu-west-1.amazonaws.com/prod/wid-countries-available-variables";
	private static String apiCountriesVariablesDownload  = "https://rfap9nitz6.execute-api.eu-west-1.amazonaws.com/prod/wid-countries-variables-dl";
	private static String apiCountriesVariablesMetadata  = "https://rfap9nitz6.execute-api.eu-west-1.amazonaws.com/prod/wid-countries-variables-metadata";

    static int importCountriesAvailableVariables(String[] args) {
    	try {
    		// Retrieve the arguments of the query
    		String countries = args[0];
    		
    		// Create the query
    		String charset = java.nio.charset.StandardCharsets.UTF_8.name();
    		String query = String.format("countries=%s&variables=all", URLEncoder.encode(countries, charset));
     		
     		// Perform the GET query
     		URL queryURL = new URL(apiCountriesAvailableVariables + "?" + query);
     		HttpURLConnection connection = (HttpURLConnection) queryURL.openConnection();
     		connection.setRequestMethod("GET");
    		connection.setRequestProperty("x-api-key", apiKey);
    		
    		// Read the response
    		InputStream response = connection.getInputStream();
    		Scanner scanner = new Scanner(response);
     		
    		List<String>  listVariable   = new ArrayList<String>();
    		List<String>  listCountry    = new ArrayList<String>();
    		List<String>  listPercentile = new ArrayList<String>();
    		List<Integer> listAge        = new ArrayList<Integer>();
    		List<String>  listPop        = new ArrayList<String>();
    		
    		// Skip the first line (with variable names)
     		scanner.useDelimiter("\\\\n").next();
    		
     		// Regex matching one line
     		Pattern pattern = Pattern.compile("^(.*?),(.*?),(.*?),(.*?),(.*?)$");
     		Matcher matcher;
     		
     		// The final double quote marks the end of the file
     		long lineIndex = 0;
     		String line = scanner.next();
     		while (!line.equals("\"")) {
     			lineIndex++;
     			
     			matcher = pattern.matcher(line);
     			matcher.matches();
     			
     			listVariable.add(matcher.group(1));
     			listCountry.add(matcher.group(2));
     			listPercentile.add(matcher.group(3));
				listAge.add(Integer.parseInt(matcher.group(4)));
				listPop.add(matcher.group(5));
     		
     			line = scanner.next();
     		}
    		
    		// Fill the Stata dataset
      		Data.addVarStr("variable", 6);
      		Data.addVarStr("country", 5);
      		Data.addVarStr("percentile", 14);
      		Data.addVarInt("age");
      		Data.addVarStr("pop", 1);
      		
      		Data.setObsTotal(lineIndex);
      		
      		int variableVariableIndex   = Data.getVarIndex("variable");
			int variableCountryIndex    = Data.getVarIndex("country");
			int variablePercentileIndex = Data.getVarIndex("percentile");
			int variableAgeIndex        = Data.getVarIndex("age");
			int variablePopIndex        = Data.getVarIndex("pop");
     		
    		for (int i = 0; i < lineIndex; i++) {
    			Data.storeStr(variableVariableIndex,   i + 1, listVariable.get(i));
    			Data.storeStr(variableCountryIndex,    i + 1, listCountry.get(i));
    			Data.storeStr(variablePercentileIndex, i + 1, listPercentile.get(i));
    			Data.storeNum(variableAgeIndex,        i + 1, listAge.get(i));
    			Data.storeStr(variablePopIndex,        i + 1, listPop.get(i));
    		}			
    	} catch (Exception e) {
    		// Display the error in Stata
    		StringWriter writer = new StringWriter();
			PrintWriter printWriter = new PrintWriter(writer);
			e.printStackTrace(printWriter);
			printWriter.flush();

			SFIToolkit.errorln(writer.toString());
    	} finally {
        	return(0);
    	}
    }
    
    static int importCountriesVariablesDownload(String[] args) {
    	try {
    		// Retrieve the arguments of the query
    		String countries = args[0];
    		String variables = args[1];
    		String years     = args[2];
    		
    		// Create the query
    		String charset = java.nio.charset.StandardCharsets.UTF_8.name();
    		String query = String.format("countries=%s&variables=%s&years=%s",
    			URLEncoder.encode(countries, charset),
    			URLEncoder.encode(variables, charset),
    			URLEncoder.encode(years, charset)
    		);
     		
     		// Perform the GET query
     		URL queryURL = new URL(apiCountriesVariablesDownload + "?" + query);
     		HttpURLConnection connection = (HttpURLConnection) queryURL.openConnection();
     		connection.setRequestMethod("GET");
    		connection.setRequestProperty("x-api-key", apiKey);
    		
    		// Read the response
    		InputStream response = connection.getInputStream();
    		Scanner scanner = new Scanner(response);
     		
    		List<String>  listCountry    = new ArrayList<String>();
    		List<String>  listIndicator  = new ArrayList<String>();
    		List<String>  listPercentile = new ArrayList<String>();
    		List<Integer> listYear       = new ArrayList<Integer>();
    		List<Double>  listValue      = new ArrayList<Double>();
						
    		// Skip the first line (with variable names)
     		scanner.useDelimiter("\\\\n").next();
    		
     		// Regex matching one line
     		Pattern pattern = Pattern.compile("^(.*?),(.*?),(.*?),(.*?),(.*?)$");
     		Matcher matcher;
     		
     		// The final double quote marks the end of the file
     		long lineIndex = 0;
     		String line = scanner.next();
     		while (!line.equals("\"")) {
     			lineIndex++;
     			
     			matcher = pattern.matcher(line);
     			matcher.matches();
     			
     			listCountry.add(matcher.group(1));
     			listIndicator.add(matcher.group(2));
     			listPercentile.add(matcher.group(3));
				listYear.add(Integer.parseInt(matcher.group(4)));
				listValue.add(Double.parseDouble(matcher.group(5)));
     		
     			line = scanner.next();
     		}
    		
    		// Fill the Stata dataset
      		Data.addVarStr("country", 5);
      		Data.addVarStr("indicator", 12);
      		Data.addVarStr("percentile", 14);
      		Data.addVarInt("year");
      		Data.addVarDouble("value");
      		
      		Data.setObsTotal(lineIndex);
      		
      		int variableCountryIndex    = Data.getVarIndex("country");
			int variableIndicatorIndex  = Data.getVarIndex("indicator");
			int variablePercentileIndex = Data.getVarIndex("percentile");
			int variableYearIndex       = Data.getVarIndex("year");
			int variableValueIndex      = Data.getVarIndex("value");
     		
    		for (int i = 0; i < lineIndex; i++) {
    			Data.storeStr(variableCountryIndex,    i + 1, listCountry.get(i));
    			Data.storeStr(variableIndicatorIndex,  i + 1, listIndicator.get(i));
    			Data.storeStr(variablePercentileIndex, i + 1, listPercentile.get(i));
    			Data.storeNum(variableYearIndex,       i + 1, listYear.get(i));
    			Data.storeNum(variableValueIndex,      i + 1, listValue.get(i));
    		}			
    	} catch (Exception e) {
    		// Display the error in Stata
    		StringWriter writer = new StringWriter();
			PrintWriter printWriter = new PrintWriter(writer);
			e.printStackTrace(printWriter);
			printWriter.flush();

			SFIToolkit.errorln(writer.toString());
    	} finally {
    		return(0);
    	}
    }

	static int importCountriesVariablesMetadata(String[] args) {
    	try {
    		// Retrieve the arguments of the query
    		String countries = args[0];
    		String variables = args[1];
    		
    		// Create the query
    		String charset = java.nio.charset.StandardCharsets.UTF_8.name();
    		String query = String.format("countries=%s&variables=%s",
    			URLEncoder.encode(countries, charset),
    			URLEncoder.encode(variables, charset)
    		);
     		
     		// Perform the GET query
     		URL queryURL = new URL(apiCountriesVariablesMetadata + "?" + query);
     		HttpURLConnection connection = (HttpURLConnection) queryURL.openConnection();
     		connection.setRequestMethod("GET");
    		connection.setRequestProperty("x-api-key", apiKey);
    		
    		// Read the response
    		InputStream response = connection.getInputStream();
    		Scanner scanner = new Scanner(response);
    		
    		List<String> listVariable  = new ArrayList<String>();
    		List<String> listShortName = new ArrayList<String>();
    		List<String> listShortDes  = new ArrayList<String>();
    		List<String> listPop       = new ArrayList<String>();
    		List<String> listAge       = new ArrayList<String>();
    		List<String> listCountry   = new ArrayList<String>();
    		List<String> listSource    = new ArrayList<String>();
    		List<String> listMethod    = new ArrayList<String>();
    		
    		// Skip the first line (with variable names)
     		scanner.useDelimiter("\\\\n").next();
     		
     		// Regex matching one line
     		Pattern pattern = Pattern.compile("^(.*?),(.*?),(.*?),(.*?),(.*?),(.*?),\\\\\"(.*?)\\\\\",\\\\\"(.*?)\\\\\",$");
     		Matcher matcher;
     		
     		// The final double quote marks the end of the file
     		long lineIndex = 0;
     		String line = scanner.next();
     		while (!line.equals("\"")) {
     			lineIndex++;
     			
     			matcher = pattern.matcher(line);
     			matcher.matches();
     			
     			listVariable.add(matcher.group(1));
     			listShortName.add(matcher.group(2));
     			listShortDes.add(matcher.group(3));
				listPop.add(matcher.group(4));
				listAge.add(matcher.group(5));
				listCountry.add(matcher.group(6));
				listSource.add(matcher.group(7));
				listMethod.add(matcher.group(8));
     		
     			line = scanner.next();
     		}
    		
    		// Maximum size for string variables
    		int shortNameLength = 0;
    		int shortDesLength  = 0;
    		int popLength       = 0;
    		int ageLength       = 0;
    		int sourceLength    = 0;
    		int methodLength    = 0;
    		for (int i = 0; i < lineIndex; i++) {
    			if (shortNameLength < listShortName.get(i).length()) {
    				shortNameLength = listShortName.get(i).length();
    			}
    			if (shortDesLength < listShortDes.get(i).length()) {
    				shortDesLength = listShortDes.get(i).length();
    			}
    			if (popLength < listPop.get(i).length()) {
    				popLength = listPop.get(i).length();
    			}
    			if (ageLength < listAge.get(i).length()) {
    				ageLength = listAge.get(i).length();
    			}
    			if (sourceLength < listSource.get(i).length()) {
    				sourceLength = listSource.get(i).length();
    			}
    			if (methodLength < listMethod.get(i).length()) {
    				methodLength = listMethod.get(i).length();
    			}
    		}
    		
    		// Fill the Stata dataset
      		Data.addVarStr("variable", 10);
      		Data.addVarStr("shortname", shortNameLength);
      		Data.addVarStr("shortdes", shortDesLength);
      		Data.addVarStr("pop", popLength);
      		Data.addVarStr("age", ageLength);
      		Data.addVarStr("country", 5);
      		Data.addVarStr("source", sourceLength);
      		Data.addVarStr("method", methodLength);
      		
      		Data.setObsTotal(lineIndex);
      		
      		int variableVariableIndex  = Data.getVarIndex("variable");
			int variableShortNameIndex = Data.getVarIndex("shortname");
			int variableShortDesIndex  = Data.getVarIndex("shortdes");
			int variablePopIndex       = Data.getVarIndex("pop");
			int variableAgeIndex       = Data.getVarIndex("age");
			int variableCountryIndex   = Data.getVarIndex("country");
			int variableSourceIndex    = Data.getVarIndex("source");
			int variableMethodIndex    = Data.getVarIndex("method");
      		
    		for (int i = 0; i < lineIndex; i++) {
    			Data.storeStr(variableVariableIndex,  i + 1, listVariable.get(i));
    			Data.storeStr(variableShortNameIndex, i + 1, listShortName.get(i));
    			Data.storeStr(variableShortDesIndex,  i + 1, listShortDes.get(i));
    			Data.storeStr(variablePopIndex,       i + 1, listPop.get(i));
    			Data.storeStr(variableAgeIndex,       i + 1, listAge.get(i));
    			Data.storeStr(variableCountryIndex,   i + 1, listCountry.get(i));
    			Data.storeStr(variableSourceIndex,    i + 1, listSource.get(i));
    			Data.storeStr(variableMethodIndex,    i + 1, listMethod.get(i));
    		}			
    	} catch (Exception e) {
    		// Display the error in Stata
    		StringWriter writer = new StringWriter();
			PrintWriter printWriter = new PrintWriter(writer);
			e.printStackTrace(printWriter);
			printWriter.flush();

			SFIToolkit.errorln(writer.toString());
    	} finally {
    		return(0);
    	}
	}
	
}
