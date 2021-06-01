package hu;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NFA2DFAConversion {

	public static void main(String[] args) throws IOException {
		
		try (BufferedReader br = new BufferedReader(new FileReader("C:\\HU\\CISC603\\CISC-603-Assignments\\nfa-2-dfa\\inputnfa.csv"))) {
			List<String> inputList = new ArrayList<>();
			List<String> initialStates = new ArrayList<>();
			
			String line = null;
			int lineNumber = 1;		
			
			while ((line = br.readLine()) != null) {
				List<String> list = Arrays.asList(line.split(","));
				if (lineNumber == 1) {
					list.forEach(text -> {
						if (!"state".equals(text)) {
							inputList.add(text);
						}
					});
				} else {
					initialStates.add(line);
				}
				lineNumber++;
			}			
			
			String[] inputs = new String[inputList.size()];
			for (int i = 0; i < inputList.size(); i++) {
				inputs[i] = inputList.get(i);
			}

			NFA2DFAConversion nFA2DFAConversion = new NFA2DFAConversion();		

			Map<String, Map<String, String>> nfaMap 
				= new LinkedHashMap<String, Map<String, String>>();
			// Creates NFA Hash Map. The key will be state and value is another hash map (with key = input and value is move).
			nFA2DFAConversion.createNFAMap(nfaMap, inputs, initialStates);
			
			
			Set<String> stateSet = nfaMap.keySet();
			Iterator<String> stateIterator = stateSet.iterator();
			String[] states = new String[nfaMap.keySet().size()];
			int index = 0;
			
			// Creates array of states from NFA map.
			while (stateIterator.hasNext()) {
				String key = stateIterator.next();
				states[index] = key;
				index++;
				
			}
			
			Map<String, Map<String, String>> dfaMap 
				= new LinkedHashMap<String, Map<String, String>>();
			
			List<String> newStates = new ArrayList<>();
			
			List<String> stateList = new ArrayList<>();
			//Add first start state to the list:
			char tempArray[] = states[0].toCharArray();
			Arrays.sort(tempArray);
			stateList.add(new String(tempArray));
			
			// Process first row. The first row from NFA is copied directly to DFA.
			nFA2DFAConversion
				.processFirstRowDFA(nfaMap, dfaMap, inputs, states, stateList, newStates);
			
			// Process remaining rows. 
			nFA2DFAConversion.computeRemainingRowsDFA(newStates, nfaMap, dfaMap, stateList, inputs);
			
			nFA2DFAConversion.printFinitaAutometa(dfaMap, inputs);
		}

	}
	
	private void computeRemainingRowsDFA(
			List<String> newStates,
			Map<String, Map<String, String>> nfaMap,
			Map<String, Map<String, String>> dfaMap,
			List<String> stateList,			
			String[] inputs) {
		
		// Process all the remaining new states. Process them with all the inputs and union the resultant states and make the union as new state
		//and add it if the new state was not added.
		while (newStates.size() > 0) {
			
			String state = newStates.get(0);
			dfaMap.put(state, new LinkedHashMap<String, String>());
			
			for (int i = 0; i < state.length(); i++) {
				
				for (String input: inputs) {
					List<String> tempStateList = new ArrayList<>();
					String tempString = "";
					for (int j = 0; j < state.length(); j++) {
						if (nfaMap.get(String.valueOf(state.charAt(j))) != null) {
							tempString = tempString + (nfaMap.get(String.valueOf(state.charAt(j))).get(input).replace(" ", ""));
						}
					}
					
					//This is to eliminate duplicates. For example, "DBD" should be "DB". Union of DBD is DB (or BD).
					for (int x = 0; x < tempString.length(); x++) {
						if (!tempStateList.contains(String.valueOf(tempString.charAt(x)))) {
							tempStateList.add(String.valueOf(tempString.charAt(x)));
						}
					}
					tempString = "";
					for (String st: tempStateList) {
						tempString = tempString + st;
					}

					 char tempArray[] = tempString.toCharArray();
					 Arrays.sort(tempArray);
					 tempString = new String(tempArray);
					 
					if (!stateList.contains(tempString)) {
						stateList.add(tempString);
						newStates.add(tempString);
					}
					dfaMap.get(state).put(input, tempString);
				}
			}
			
			//Once the state is processed, remove it from the list.
			newStates.remove(newStates.get(0));			
		}
	}
	
	private void processFirstRowDFA(
			Map<String, Map<String, String>> nfaMap,
			Map<String, Map<String, String>> dfaMap,
			String[] inputs, String[] states,
			List<String> stateList,
			List<String> newStates) {
		Map <String, String> map = new LinkedHashMap<>();
		
		// For each input 0, 1, take the first state and get the moves from nfa map. If the move has state like "A B", make it to "AB"
		// sort the characters for example, if its BA make to AB so that there are no duplicates and add it to list of new;y created
		//states if it was not already added.
		for (String input: inputs) {
			String state = nfaMap.get(states[0]).get(input);
			if (state != null) {
				state = state.replace(" ", "");
			}
			
			char tempArray[] = state.toCharArray();
			String s = new String(tempArray);
			map.put(input, s);
			if (!stateList.contains(s)) {
				newStates.add(s);
				stateList.add(s);
			}
		}
		dfaMap.put(states[0], map);
		System.out.println(" ---------DFA with First Row -------");
		printFinitaAutometa(dfaMap, inputs);
	}

	private void createNFAMap(Map<String, Map<String, String>> nfaMap, String[] inputs, List<String> stateList) {
		
		Map<String, String> map = null;
				
		for (String state: stateList) {
			
			map = new LinkedHashMap<>();
			String[] s1 = state.split(",");
			
			String trans = s1[1].replace("{", "").replace("}", "");
			map.put(inputs[0], trans);
			
			trans = s1[2].replace("{", "").replace("}", "");
			map.put(inputs[1], trans);
			
			nfaMap.put(s1[0], map);
		}

		printFinitaAutometa(nfaMap, inputs);
	}
	
	private void printFinitaAutometa(Map<String, Map<String, String>> map, String[] inputs) {
		
		try {
			try (BufferedWriter br = new BufferedWriter(new FileWriter("C:\\HU\\CISC603\\CISC-603-Assignments\\nfa-2-dfa\\output.csv"))) {
				Set<String> stateSet = map.keySet();
				Iterator<String> stateIterator = stateSet.iterator();
				String line = "state," + inputs[0] + "," + inputs[1];
				br.write(line);
				br.newLine();
				while (stateIterator.hasNext()) {
					String key = stateIterator.next();
					line = key;
					for (String input : inputs) {
						System.out.print(input + ": " + map.get(key).get(input) + " -- ");
						line = line + "," + map.get(key).get(input);
					}
					br.write(line);
					br.newLine();
				}
				br.flush();
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
