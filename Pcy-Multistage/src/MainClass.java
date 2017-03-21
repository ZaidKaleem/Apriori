import java.io.*;
import java.util.*;

public class MainClass {


    public static void main(String[] args) {

        String fileName = "retail.txt";


        int PCYHashSize = 10000;

        int totalLines = 88162;
        int lineCheck = 0;

        // Using HashMap and ArrayList structures to store and count frequency of items
        HashMap<String, Integer> itemCount = new HashMap<String, Integer>();
        ArrayList<String> items = new ArrayList<String>();
        // ArrayList to hold frequent items
        ArrayList<String> frequentItems = new ArrayList<String>();
        // Using HashMap to keep count of pairs made up of individual frequent items
        HashMap<String, Integer> pairCount = new HashMap<String, Integer>();
        // ArrayList to hold frequent pairs
        ArrayList<String> frequentPairs = new ArrayList<String>();

        Scanner input = new Scanner(System.in);

        System.out.println("Welcome!");
        System.out.print("Enter a decimal percentage of data you want to use (E.g. 50% is 0.50): ");
        float chunk = input.nextFloat();

        System.out.print("Enter a decimal percentage of support you want to use (E.g. 20% is 0.20): ");
        float support = input.nextFloat();

        float realTotalLines = chunk * totalLines;
        float realSupport = support * realTotalLines;


        //Start timer
        Long startTime = System.currentTimeMillis();

        // First pass over the data to read and parse the items while keeping track of their frequency

        Map<Integer, Integer> PCYMap = new HashMap<Integer, Integer>();

        try {
            FileInputStream fstream = new FileInputStream(fileName);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;

            int lineCount = 0;

            while ((strLine = br.readLine()) != null && lineCount <= realTotalLines) {
                String[] tokens = strLine.split(" ");

                for (int i = 0; i < tokens.length; i++) {

                    if (itemCount.containsKey(tokens[i]) && items.contains(tokens[i]))
                        itemCount.put(tokens[i], itemCount.get(tokens[i]) + 1);
                    else {
                        itemCount.put(tokens[i], 1);
                        items.add(tokens[i]);
                    }
                }

                lineCount++;

                //PCY
                for (int i = 0; i < tokens.length; i++) {
                    int itemNoI = Integer.parseInt(tokens[i]);

                    for (int j = i + 1; j < tokens.length; j++) {
                        int itemNoJ = Integer.parseInt(tokens[j]);

                        int hashValue = (itemNoI + itemNoJ) % PCYHashSize;

                        if (PCYMap.containsKey(hashValue)) {
                            int currentNum = PCYMap.get(hashValue);
                            currentNum++;
                            PCYMap.put(hashValue, currentNum);
                        } else {
                            PCYMap.put(hashValue, 1);
                        }
                    }
                }
            }
            in.close();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }

        // Add frequent items to a list
        addFrequentItems (items, itemCount, realSupport, frequentItems);

        //PCY Bit Map
        Map<Integer, Boolean> PCYBitMap = new HashMap<Integer, Boolean>();

        for (int key : PCYMap.keySet()) {
            int count = PCYMap.get(key);
            if (count > realSupport) {
                PCYBitMap.put(key, true);
            } else {
                PCYBitMap.put(key, false);
            }
        }


        Map<Integer, Integer> PCYMap2 = new HashMap<Integer, Integer>();

        // Second pass over the data to rehash frequent pairs from pass 1
        try {
            FileInputStream fstream = new FileInputStream(fileName);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;

            int lineCount = 0;

            while ((strLine = br.readLine()) != null && lineCount <= realTotalLines) {
                String[] tokens = strLine.split(" ");

                for (int i = 0; i < tokens.length; i++) {
                    String itemNoI = tokens[i];

                    for (int j = i + 1; j < tokens.length; j++) {
                        String itemNoJ = tokens[j];
                        int hashValue = (Integer.parseInt(itemNoI.trim()) + Integer.parseInt(itemNoJ.trim())) % PCYHashSize;

                        if (frequentItems.contains(itemNoI) && frequentItems.contains(itemNoJ)) {
                            if (PCYBitMap.get(hashValue)) {
                                if (PCYMap2.containsKey(hashValue)) {
                                    int currentNum = PCYMap2.get(hashValue);
                                    currentNum++;
                                    PCYMap2.put(hashValue, currentNum);
                                } else {
                                    PCYMap2.put(hashValue, 1);
                                }
                            }
                        }
                    }

                    lineCount++;
                }
            }
            in.close();
            lineCheck = lineCount;
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }


        //PCY Bit Map 2
        Map<Integer, Boolean> PCYBitMap2 = new HashMap<Integer, Boolean>();

        for (int key : PCYMap2.keySet()) {
            int count = PCYMap2.get(key);
            if (count > realSupport) {
                PCYBitMap2.put(key, true);
            } else {
                PCYBitMap2.put(key, false);
            }
        }



        // Third pass
        try {
            FileInputStream fstream = new FileInputStream(fileName);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;

            int lineCount = 0;

            while ((strLine = br.readLine()) != null && lineCount <= realTotalLines) {
                String[] tokens = strLine.split(" ");

                for(int i=0; i<tokens.length; i ++) {

                    String itemNoI = tokens[i];

                    for (int j = i + 1; j < tokens.length; j++) {
                        String itemNoJ = tokens[j];

                        int hashValue = (Integer.parseInt(itemNoI.trim()) + Integer.parseInt(itemNoJ.trim()))%PCYHashSize;

                        if(frequentItems.contains(itemNoI) && frequentItems.contains(itemNoJ)){
                            if(PCYBitMap.get(hashValue) && PCYBitMap2.get(hashValue)) {
                                String itemKey = itemNoI + "-" + itemNoJ;
                                if (pairCount.containsKey(itemKey)) {
                                    int currentNum = pairCount.get(itemKey);
                                    currentNum++;
                                    pairCount.put(itemKey, currentNum);
                                } else {
                                    pairCount.put(itemKey, 1);
                                }
                            }
                        }
                    }

                }

                lineCount++;
            }
            in.close();
            lineCheck = lineCount;
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }



        //Add pairs to frequentPairs list if > than support and no already in

        for(String key:pairCount.keySet()){
            int count = pairCount.get(key);
            if(count > realSupport){
                frequentPairs.add(key);
            }
        }

        //End timer
        long endTime = System.currentTimeMillis();

        //Calculate runtime
        long runTime = endTime - startTime;

        System.out.println("Runtime: " + runTime);

        System.out.println("realSupport: " + realSupport + " realTotalLines: " + realTotalLines + " LC: " + lineCheck);

        System.out.println("items: " + items.size());
        System.out.println("F-items: " + frequentItems.size());
        System.out.println("pairsCount: " + pairCount.size());
        System.out.println("F-pairs: " + frequentPairs.size());

    }


    // Add frequent items to frequentItems
    static void addFrequentItems (ArrayList<String> items, HashMap<String, Integer> itemCount, Float realSupport, ArrayList<String> frequentItems) {
        for (int i = 0; i < items.size(); i++) {
            if (itemCount.get(items.get(i)) >= realSupport && !frequentItems.contains(items.get(i)))
                frequentItems.add(items.get(i));
        }
    }


}