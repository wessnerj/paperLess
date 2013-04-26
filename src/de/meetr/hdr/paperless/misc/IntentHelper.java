/**
 * paperLess - Android App for taking notes in PDFs
 * Copyright (C) 2013 Joseph Wessner
 * 
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.meetr.hdr.paperless.misc;

import java.util.Hashtable;

public class IntentHelper {
	private static IntentHelper instance = null;
    private Hashtable<String, Object> hash = new Hashtable<String, Object>();
    
    private IntentHelper() {
    	
    }
	
    private static IntentHelper getInstance() {
        if(null == instance) {
        	instance = new IntentHelper();
        }
        
        return instance;
    }

    public static void addObjectForKey(Object object, String key) {
        getInstance().hash.put(key, object);
    }
    
    public static Object getObjectForKey(String key) {
        IntentHelper helper = getInstance();
        Object data = helper.hash.get(key);
        helper.hash.remove(key);
        return data;
    }
}
