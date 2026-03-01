package compilers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Reader {
  
  public String[] readFile(String path) {
    String[] text = new String[0];
    
    try (BufferedReader br = new BufferedReader(new FileReader(path))) {
      String line;
      while ((line = br.readLine()) != null) {
        if(isEmpty(line) == false && line.equals("") == false) {
          text = strPush(line,text);
        }
      }
    } catch (IOException e) {
      System.out.println("Error reading file");
    }
    return text;
  }
  
  public boolean isEmpty(String line) {
    for(int i = 0;i < line.length();i++) {
      String c = line.substring(i,i+1);
      if (c.equals(" ") == false) {
        return false;
      }
    }
    return true;
  }
  
  public static String[] strPush(String val,String[] arr) {
    String[] narr = new String[arr.length+1];
    
    for(int i = 0; i < arr.length;i++) {
      narr[i] = arr[i];
    }
    narr[arr.length] = val;
    
    return narr;
  }
}

