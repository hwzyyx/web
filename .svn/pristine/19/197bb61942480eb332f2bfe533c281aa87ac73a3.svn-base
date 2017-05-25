package com.callke8.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ArrayUtils {
	
	public static Object[] copyArray(int index,Object[] arr) {
		
		Object[] arrEnd = new Object[index];
		
		System.arraycopy(arr, 0, arrEnd, 0, index);
		
		return arrEnd;
		
	}
	
	@SuppressWarnings("unchecked")
	public static List removeDuplicateWithOrder(List list) { 

        Set set = new HashSet(list.size()); 
        set.addAll(list); 

        List newList = new ArrayList(set.size()); 

        newList.addAll(set); 

        return newList; 

    } 
	
}
