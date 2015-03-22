package com.copypastestash.persistence;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Persists the copy/paste stash map into a text file.
 */
public class StashPersistence {

    private static final String FILE_PATH =
            System.getProperty("user.home") + "/.copyPasteStash";

    private Map<String, String> stash;


    /** Gets strings that match given text.
     * @param txt Text to searc for.
     * @return Matching strings.
     */
    public List<String> getMatchingStrings(String txt) {
        ArrayList<String> matchList = new ArrayList<String>();
        for (String key : getStash().keySet()) {
            if (key.startsWith(txt)) {
                matchList.add(key);
            }
        }
        return matchList;
    }

    /**
     * Gets the copy/paste stash map, either from local memory if available, or
     * the file.
     * @return Stash map.
     */
    public Map<String, String> getStash() {
        if (stash == null) {
            loadStashFromFile();
        }
        return stash;
    }

    /**
     * Puts an item into the stash map.
     * @param key
     *            Key to locate the item by.
     * @param value
     *            String to copy and paste.
     */
    public void putItem(String key, String value) {
        if (stash == null) {
            loadStashFromFile();
        }

        stash.put(key, value);
    }

    /**
     * Writes the current stash to persistence.
     */
    public void writeStash() {
        if (stash == null) {
            return;
        }

        PrintWriter out;
        try {
            out = new PrintWriter(new BufferedWriter(new FileWriter(FILE_PATH,
                    false)));
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        for (Map.Entry<String, String> entry : stash.entrySet()) {
            out.println(entry.getKey());
            out.println(entry.getValue());
        }

        out.close();
    }

    /**
     * Loads the stash from the persistence file.
     */
    private void loadStashFromFile() {
        File stashFile = new File(FILE_PATH);
        try {
            HashMap<String, String> stashMap = new HashMap<String, String>();
            BufferedReader br = new BufferedReader(new FileReader(stashFile));
            String line = null;
            String key = null;

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (key == null) {
                    key = line;
                } else {
                    stashMap.put(key, line);
                    key = null;
                }
            }
            br.close();

            stash = stashMap;
        } catch (FileNotFoundException e) {
            stash = new HashMap<String, String>();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

}
