package bughunter.utils;

import java.io.Serializable;
import java.util.*;
import java.util.logging.Logger;

public class StringStorage implements Serializable {

	private static final long serialVersionUID = 4752316503948123135L;

	protected static int randomNumbers[] = {
		  1, 14,110, 25, 97,174,132,119,138,170,125,118, 27,233,140, 51,
		 87,197,177,107,234,169, 56, 68, 30,  7,173, 73,188, 40, 36, 65,
		 49,213,104,190, 57,211,148,223, 48,115, 15,  2, 67,186,210, 28,
		 12,181,103, 70, 22, 58, 75, 78,183,167,238,157,124,147,172,144,
		176,161,141, 86, 60, 66,128, 83,156,241, 79, 46,168,198, 41,254,
		178, 85,253,237,250,154,133, 88, 35,206, 95,116,252,192, 54,221,
		102,218,255,240, 82,106,158,201, 61,  3, 89,  9, 42,155,159, 93,
		166, 80, 50, 34,175,195,100, 99, 26,150, 16,145,  4, 33,  8,189,
		121, 64, 77, 72,208,245,130,122,143, 55,105,134, 29,164,185,194,
		193,239,101,242,  5,171,126, 11, 74, 59,137,228,108,191,232,139,
		  6, 24, 81, 20,127, 17, 91, 92,251,151,225,207, 21, 98,113,112,
		 84,226, 18,214,199,187, 13, 32, 94,220,224,212,247,204,196, 43,
		249,236, 45,244,111,182,153,136,129, 90,217,202, 19,165,231, 71,
		230,142, 96,227, 62,179,246,114,162, 53,160,215,205,180, 47,109,
		 44, 38, 31,149,135,  0,216, 52, 63, 23, 37, 69, 39,117,146,184,
		163,200,222,235,248,243,219, 10,152,131,123,229,203, 76,120,209
	};

	protected int noBuckets;
	protected int count[];
	protected TreeMap<Integer,String> strTable[];
	
	public StringStorage() {
		this(511);
	}

	public StringStorage(int buckets) {
		noBuckets = 511;
		strTable = new TreeMap[noBuckets];
		count = new int[noBuckets];

		for (int i = 0; i < noBuckets; i++) {
			strTable[i] = new TreeMap<Integer, String>();
			count[i] = 1;
		}
	}

	private int hash(String string){
		int hashHi = 0;
		int hashLow = 0;

		if (string == null)
			return 0;

		if(string.length() == 0)
			return 0;

		char[] convertedString = string.toCharArray();

		for (int i = 0; i < convertedString.length; i++) {
			convertedString[i] &= 0xFF;
			hashHi = randomNumbers[hashHi ^ convertedString[i]];
		}
		convertedString[0]++;
		convertedString[0] &= 0xFF;

		for (int i = 0; i < convertedString.length; i++)
			hashLow = randomNumbers[hashLow ^ convertedString[i]];

		return (hashHi << 8) | hashLow;
	}

	private int get(String s, int hashValue, int bucketIndex) {
		TreeMap<Integer,String> bucket = strTable[bucketIndex];
		SortedMap<Integer,String> sm = bucket.subMap(hashValue << 16, (hashValue << 16) + 0xFFFF);

		for (Iterator<Map.Entry<Integer,String>> it = sm.entrySet().iterator(); it.hasNext(); ) {
			Map.Entry<Integer,String> entry = it.next();
			if (entry.getValue().equals(s))
				return entry.getKey();
		}

		String entry = bucket.get((hashValue + 1) << 16);
		if (entry != null && entry.equals(s))
			return ((hashValue + 1) << 16);

		return 0;
	}

	public int get(String s) {
		if(s == null)
			return 0;

		if(s.length() == 0)
			return 0;

		int hashValue = hash(s);
		int bucketIndex = hashValue % noBuckets;

		return get(s, hashValue, bucketIndex);
	}

	public String get(int key) {

		if (key == 0)
			return "";

		int bucketIndex = (key >>> 16) % noBuckets;
		String value = strTable[bucketIndex].get(key);
		if (value != null)
			return value;

		return "";
	}

	public int set(String s) {
		
		if (s == null)
			return 0;
		
		if (s.length() == 0)
			return 0;

		int key = 0;
		int hashValue = hash(s);
		int bucketIndex = hashValue % noBuckets;

		if ((key = get(s, hashValue, bucketIndex)) == 0) {
			key = (hashValue << 16) | count[bucketIndex]++;

			strTable[bucketIndex].put(key, s);
		}			
		return key;
	}
}

