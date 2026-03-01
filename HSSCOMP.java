package compilers;

import compilers.Reader;
import java.lang.Math;

public class HSSCOMP extends Reader{
  /* 
  objective: read HSS(my custom language: hammer assembly) code and compile 
  HSS code into machine language for my 8 bit computer(prototype)
  
  hammer assembly(hss) is an entirely custom language designed for my computer and
  is entirely customizable
  
  scripts max length: 16 statements(4bit RAM address)
  
  16 distinct statements suported
  each having 16 step max
  */
  
  public String[] STA;  // HSS statements
  public String[] MI; // machine micro instructions name
  
  int stepBits = 4, // step/instruction counter bits
  STABits = 4, // statenent id bits
  PCBits = 4, // program counter bits
  OPBits = 4; // operand bits
  
  public int maxSteps = (int)Math.pow(2,stepBits);
  public int maxStatements = (int)Math.pow(2,STABits);
  public int maxCodeLines = (int)Math.pow(2,PCBits);
  
  public String[] STAnames;
  public int[][] STAsteps;
  
  public String[] code;
  public String[] compiledCode;
  
  public int curLine = -1; // used to discover what line the compilation error occurred
  // when -1 then is not being used
  
  public HSSCOMP() {
    System.out.println("## loading statements and microinstructions from files");
    
    MI = readFile("machineCodes.hss");
    STA = readFile("statements.hss");
    code = readFile("code.hss");
    
    System.out.println("## system data bits");
    System.out.println("");
    
    System.out.println("Program Counter: "+PCBits+" bits");
    System.out.println("Statement IDs: "+STABits+" bits");
    System.out.println("operand: "+OPBits+" bits");
    System.out.println("step counter: "+stepBits+" bits");
    System.out.println("");
    
    
    //// PRE COMPILING -------------- //
      
    //clean up --------------
    System.out.println("## cleaning code files...");
    
    MI = cleanCode(MI);
    STA = cleanCode(STA);
    code = cleanCode(code);
    
    
    //set up usable statements --------------
    System.out.println("");
    System.out.println("## setting statements...");
    System.out.println("");
    
    STAsteps = new int[maxStatements][maxSteps];
    STAnames = new String[maxStatements];
    setStatements();
    
    //// COMPILE ACTUAL CODE -------------- //
    
    System.out.println("## Compiling...");
    System.out.println("");
    
    compiledCode = new String[(int)Math.pow(2,PCBits)];
    interpretCode();
    
    System.out.println("");
    System.out.println("## compiling completed successfully");
  }
  
  public String[] cleanCode(String[] co) {
    String[] cn = new String[0];
    
    for(int j = 0;j < co.length;j++) {
      String cleanLine = "";
      curLine = j;
      
      for(int i = 0; i < co[j].length();i++) {
        String s = co[j].substring(i,i+1);
        
        switch(s) {
          case "/":
            i = co[j].length()+1; // finished line
            break;
          case ";":
            i = co[j].length()+1;  // finished line
            break;
          case ".":
            cleanLine+=s;
            i = co[j].length()+1;  // finished line
            break;
          case " ":
            break; // skip blank spaces
          default:
            cleanLine+=s;// copy valid characters into the clean string
        }
      }
      
      if (cleanLine.equals("") == false) {
        cn = strPush(cleanLine,cn);
      }
    }
    
    curLine = -1;
    
    return cn;
  }
  
  public void setStatements() { // setup available statements for use
    
    //// setting all arrays as empty
    for(int j = 0; j < maxStatements;j++) {
      for(int i = 0; i < maxSteps;i++) {
        STAsteps[j][i] = 0;
      }
      STAnames[j] = "null";
    }
    
    //// extract STA names and untranslated MI and organize them in steps -------------
    
    int stepindex = 0,
    staindex = -1; // adds before setting mi
    
    // trimmed untranslated microinstruction's
    String STAMI0[][][] = new String[maxStatements][maxSteps][MI.length];
    
    for(int i = 0; i < STA.length;i++) {
      curLine = i;
      String s = STA[i].substring(0,1);
      if (s.equals(">")) { // is a statement line
      
        if (staindex > 0 && stepindex == 0) {
          printError("statement was declared without any steps!");
        }
        staindex++;
        if (staindex > maxStatements) {
          printError("maximum number of statements exceeded");
        }
        stepindex = 0;
        STAnames[staindex] = getSTAName(STA[i]);
      } else { // is a step line
        if (staindex == -1) {
          printError("step found without designated statement");
        } else {
          STAMI0[staindex][stepindex] = trimMI(STA[i]);
          stepindex++;
          if (staindex > maxSteps) {
            printError("maximum number of steps exceeded in statement ("+STAnames[staindex]+")");
          }
        }
      }
    }
    curLine = -1;
    
    // hex steps array
    String[][] STAMI = new String[maxStatements][maxSteps];
    
    for(int j = 0;j < maxStatements;j++) {
      for(int i = 0;i < maxSteps;i++) {  
        // translate each step
        STAMI[i][j] = translateStep(STAMI0[j][i]);
      }
    }
    //// print so it can be copyed to logism -------------
    
    System.out.println("## statements set! copy machine code into control logic:");
    
    printMatrixBundle(STAMI,8);
    
    System.out.println("");
    System.out.println("## available statements are:");
    
    
    printNumTxt(STAnames,10,2);
  }
  
  public void interpretCode() {
    int hexCodeLen = ((OPBits+STABits-1) / 4)+1;//finds hou many is needed (rounds up)
    
    //// COMPILE CODE
    
    for(int i = 0;i < maxCodeLines;i++) {
      curLine = i;
      if (i < code.length) { // extract code
        String sta = "";
        String op = "";
        
        //// fetch and translate the statement name and operand from code line
        boolean foundM = false; // found ":" mark
        for (int j = 0;j < code[i].length();j++) { 
          String s = code[i].substring(j,j+1);
          String binIdent = " ";
          if (s.equals(":")) {
            foundM = true;
            if (j+2 <= code[i].length()) {
              binIdent = code[i].substring(j+1,j+2); // binary indicator
            }
            sta = code[i].substring(0,j);// fetch statement's name
            
            sta = toBinary(identifySTA(sta,true),STABits); // turn into binary
            
            if (binIdent.equals("*")) { // look for binary indicator
              // its already in binary
              op = code[i].substring(j+2, code[i].length());
              if (op == null || op.equals("") || op.equals(" ")) {
                op = toHex("0",OPBits);
              }
            } else {
              // its not in binary
              op = code[i].substring(j+1, code[i].length());
              if (op == null || op.equals("") || op.equals(" ")) {
                op = toHex("0",OPBits);
              } else {
                op = toBinary(toInt(op),OPBits); // convert to binary(string);
              }
            }
          }
        }
        
        if (foundM == false) {
          printError("':' is missing from code");
        }
        
        //// complete code line translated into machine code
        compiledCode[i] = toHex(sta+op,hexCodeLen);
      } else {
        // fill unused addresses with zero
        compiledCode[i] = toHex("0000",hexCodeLen);
      }
    }
    curLine = -1;
    //// print compiled code
    
    System.out.println("## compiling completed -> copy code into Instruction Ram");
    printVectorBundle(compiledCode,8);
    
  }
  
  public String[] trimMI(String line) {
    String result[] = new String[0];// instructions
    
    boolean finished = false; // check for sintax error: step not properly finished
    
    int s = 0,f = 0;// start and end of current MI being trimmed
    for(int i = 0; i < line.length();i++) {
      String c = line.substring(i,i+1);
      String inst;//instruction
      switch(c) {
        case ",":
          f = i;
          inst = line.substring(s,f);
          result = strPush(inst,result);  
          s = f+1;
          break;
        case ".":
          f = i;
          inst = line.substring(s,f);
          result = strPush(inst,result);   
          finished = true;
          break;
      }
    }
    
    if (finished == false) {
      printError("statement step not finished('.' expected)");
      return null;
    }
    
    return result;
  }
  
  public String getSTAName(String line) {
    String name = "";
    for(int i = 0;i < line.length();i++) {
      String c = line.substring(i,i+1);
      if (c.equals(":")) {
        name = line.substring(1,i);
        if (name.equals("") || name == null) {
          printError("statement name could not be found");
        }
        return name;
      }
    }
    printError("statement declaration poorly structured");
    return "ERROR NAME NOT FOUND";
  }
  
  public String translateStep(String[] step) {// microinstruction array -> hex value(in string form)
    int[] binaryStep = new int[MI.length];
    //set them all as 0
    for(int i = 0; i < MI.length;i++) {
      binaryStep[i] = 0;
    }
    
    int[] MIID = new int[step.length];
    for(int i = 0; i < step.length;i++) {
      MIID[i] = identifyMI(step[i],false);

      if (MIID[i] == -1) {
        // null
        continue;
      }
      
      //plot all identifyed MI as 1 -> inverted
      binaryStep[binaryStep.length-1-MIID[i]] = 1;
    }
    
    String s = stepToString(binaryStep);
    
    return toHex(s,5);
  }
  
  public String stepToString(int[] step) {
    String s = "";
    
    for(int i = 0; i < MI.length;i++) {
      s += ""+step[i];
    }
    
    return s;
  }
  
  public int identifySTA(String str, boolean showError) {
    String error = null;
    if (showError) {
      error = "STA";
    }
    return matchString(str,STAnames,error);
  }
  
  public int identifyMI(String str, boolean showError) {
    String error = null;
    if (showError) {
      error = "MI";
    }
    return matchString(str,MI,error);
  }
  
  public int matchString(String str,String[] array,String arrError) {
    if (str == null || str.equals("null")) {
      return -1;
    }
    for(int i = 0;i < array.length;i++) {
      if (str.equals(array[i])) {
        return i;
      }
    }
    if (arrError != null) {
      // arrError -> name of the array thats faulty
      // arrError == null then thir error message wont be printed
      printError("String("+str+") not identified in "+arrError);
    }
    return -1;
  }
  
  public static int[] intPush(int val,int[] arr) {
    int[] narr = new int[arr.length+1];
    
    for(int i = 0; i < arr.length;i++) {
      narr[i] = arr[i];
    }
    narr[arr.length] = val;
    
    return narr;
  }
  
  public int toInt(String s) {
    int v = 0;
    try {
      v = Integer.parseInt(s);
    }
    catch (NumberFormatException e) {
      printError("String '"+s+"' cant be converted into number");
      v = -1;
    }
    return v;
  }
  
  public String toBinary(int dec,int digits) {
    int binary[] = new int[digits];
    int count[] = {1,2,4,8,16,32,64,128,256,512,1024};
    int max = (int)(Math.pow(2,digits)-1);
    // ex => 0 - 15 (4 bit)
    
    if (dec < max) {// count up to max or up
      max = dec;
    } else {
      printError("binary conversion faulty: "+dec+" is bigger than max("+max+")");
    }
    
    
    for(int i = 1;i <= max;i++) {
      for(int j = 0;j < digits;j++) {
        if (i % count[j] == 0) {
          if (binary[j] == 0) {
            binary[j] = 1;
          } else {
            binary[j] = 0;
          }
        }
      }
    }
    
    String result = "";
    for(int i = digits-1; i >= 0;i--) {
      result += binary[i];
    }
    
    return result;
  }
  
  public String toHex(String binaryStr) {
    int decimal = Integer.parseInt(binaryStr,2);
    String hexStr = Integer.toString(decimal,16);
    return hexStr;
  }
  
  public String toHex(String binaryStr,int numOfBits) {
    int decimal = Integer.parseInt(binaryStr,2);
    String hexStr = Integer.toString(decimal,16);
    int aditionalZeros = numOfBits-hexStr.length();
    
    String result = "";
    for(int i = 0;i < aditionalZeros;i++) {
      result+="0";
    }
    result += hexStr;
    
    return result;
  }
  
  
  public void printTxt(String[] str) {
    for(int i = 0;i < str.length;i++) {
      System.out.println(str[i]);
    }
  }
  
  public void printError(String errorText) {
    if (curLine >= 0) {
      System.out.println("ERROR: "+errorText+" at line "+curLine);
    } else {
      System.out.println("ERROR: "+errorText);
    }
    
    System.exit(0);
  }
  
  public void printMatrixBundle(String[][] array, int linesPerLine) {
    int count = 0;
    String line = "";
    for(int j = 0;j < array[0].length;j++) {
      for(int i = 0;i < array.length;i++) {
          count++;
          line+= array[i][j];
          line+= " ";
          if (count >= linesPerLine) {
            System.out.println(line);
            line = "";
            count = 0;
          }
          
      }
    }
  }
  
  public void printVectorBundle(String[] array, int linesPerLine) {
    String line = "";
    int count = 0;
    for(int i = 0;i < array.length;i++) {
      line += array[i] + " ";
      count++;
      if (count >= 8 || i >= array.length-1) {
        System.out.println(line);
        line = "";
        count = 0;
      }
    }
  }
  
  public void printNumTxt(String[] str) {
    for(int i = 0;i < str.length;i++) {
      System.out.println(i+": "+str[i]);
    }
  }
  
  public void printNumTxt(String[] str, int bundleSize) {
    int counter = 0;
    String line = "";
    for(int i = 0;i < str.length;i++) {
      if (counter < bundleSize && i < str.length-1) {
        counter++;
        line += i+": "+str[i]+" ";
      } else {
        counter = 0;
        System.out.println(line);
        line = "";
      }
    }
  }
  
  public void printNumTxt(String[] str,int linelen, int bundleSize) {
    int counter = 0;
    
    String line = "";
    for(int i = 0;i < str.length;i++) {
      if (counter < bundleSize && i < str.length-1) {
        counter++;
        line += i+": "+str[i];
        int v = linelen*counter - line.length();
        
        for(int j = 0; j <= v;j++) {
          line += " ";
        }
        
      } else {
        System.out.println(line);
        counter = 0;
        line = "";
      }
    }
  }
  
  public static void main(String[] args) {
    HSSCOMP s = new HSSCOMP();
  }
}

// pc do re 7 ou do re 4 original
