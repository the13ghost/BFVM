package com.t13g.Brainfuck;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;


public class BFVM {
    
    private static final String VERSION = "v1.0";
    private static final String[] OPTIONS = { "--mem-size","-c","-h"};
    private static final int MEM_SIZE_OPTION = 0;
    private static final int C_OPTION = 1;
    private static final int HELP_OPTION = 2;
    
    public static void main(String[] arg){
        int vmMemSize = 30000;
        int vmCellMin = 0;
        int vmCellMax = 255;
        
        String vmLoadString = "";
        
        if(arg.length == 0){
            help();
            
            return;
        } else {
            if(arg.length == 1){
                int o = whichOp(arg[0]);
                
                if(o == MEM_SIZE_OPTION){
                    help();
                    
                    return;
                } else {
                    StringBuilder in = new StringBuilder();
                    
                    try {
                        File f = new File(arg[0]);
                        FileReader fr = new FileReader(f);
                        BufferedReader br = new BufferedReader(fr);
                        
                        String line;
                        
                        while((line = br.readLine()) != null)
                            in.append(line);   
                        
                        vmLoadString = in.toString();
                        
                    } catch (FileNotFoundException e) {
                        System.out.println("File Not Found");
                        return;
                    } catch (IOException e) {
                        System.out.println("File cannot be Read");
                        return;
                    }
                }
            } else {
                int i = 0;
                
                while (true) {
                    if (i >= arg.length) 
                        break;
                    
                    if ( arg[i].charAt(0) == '-'){
                        
                        int o = whichOp(arg[i]);
                        
                        switch (o) {
                        case MEM_SIZE_OPTION:
                            i++;
                            if(isValidMemSize(arg,i))
                                vmMemSize = Integer.parseInt(arg[i]);
                            else {
                                System.out.println("Invalid number input");
                                help();
                                return;
                            }
                            break;
                            
                        case C_OPTION:
                            i++;
                            if(i < arg.length)
                                vmLoadString = arg[i];
                            else {
                                System.out.println("Invalid number input");
                                help();
                                return;
                            }
                            break;
                            
                        case HELP_OPTION:
                            help();
                            return;
                            
                        default:
                            System.out.println("Option " + arg[i] + " not recognized");
                            help();
                            return;      
                        }
                        
                        i++;
                    } else {

                        StringBuilder in = new StringBuilder();
                        
                        try {
                            File f = new File(arg[0]);
                            FileReader fr = new FileReader(f);
                            BufferedReader br = new BufferedReader(fr);
                            
                            String line;
                            
                            while((line = br.readLine()) != null)
                                in.append(line);   
                            
                            vmLoadString = in.toString();
                            
                        } catch (FileNotFoundException e) {
                            System.out.println("File Not Found");
                            return;
                        } catch (IOException e) {
                            System.out.println("File cannot be Read");
                            return;
                        }
                        
                        break;
                    }
                }
            }
            
            BFVM vm = new BFVM(vmMemSize,vmCellMin,vmCellMax);
            
            if(vm.loadProgram(vmLoadString)){
                vm.run();
            } else {
                System.out.println("Compilation Error. mismatched [ or ]");
            }
        }
    }

    private static boolean isValidCellSize(String[] arg, int i) {
        return isValid(arg,i,false);
    }
    private static boolean isValidMemSize(String[] arg, int i) {
        return isValid(arg,i,true);
    }
    private static boolean isValid(String[] arg, int i, boolean mem) {
        if(i >= arg.length)
            return false;
        try {
            int v = Integer.parseInt(arg[i]);
            if(mem&&v < 1)
                return false;
        } catch (NumberFormatException e){
            return false;
        }
        return true;
    }

    private static int whichOp(String s) {
        for(int i=0;i<OPTIONS.length;i++){
            if(s.equals(OPTIONS[i]))
                return i;
        }
        return -1;
    }

    private static void help() {
        String greet =  "The13ghost's Brainfuck Interpreter version " + VERSION +"\n"+
                        "usage: java BFVM (--mem-size [#]) [[file] | -c [code]]\n";
        System.out.println(greet);
    }

    private InputStream in;
    private OutputStream out;
    private char[] program;
    private int[] jumpTable;
    private int[] memory;

    public BFVM() {
        memory=new int[30000];
        in = System.in;
        out = System.out;
    }

    public BFVM(int vmMemSize, int vmCellMin, int vmCellMax) {
        memory=new int[vmMemSize];
        in = System.in;
        out = System.out;
    }

    public void clearMem(){
        for(int i=0;i<memory.length;i++)
            memory[i]=0;
    }

    public void clearJumpTable(){
        for(int i=0;i<jumpTable.length;i++)
            jumpTable[i]=-1;
    }

    public boolean loadProgram(String s){
        s = s.replaceAll("[^\\+\\-<>,\\[\\]\\.]", "");
        
        program = s.toCharArray();
        jumpTable = new int[program.length];
        
        clearMem();
        clearJumpTable();
        
        for(int i=0;i<program.length;i++){
            if(program[i]=='['){
                int prevCount=0;
                for(int j=i+1;j<program.length;j++){
                    if((program[j]==']')&&prevCount==0){
                        jumpTable[i]=j;
                        jumpTable[j]=i;
                        break;
                    }
                    else if(program[j]==']') prevCount--;
                    else if(program[j]=='[') prevCount++;
                }
            }
        }
        for(int i=0;i<program.length;i++)
            if(program[i]=='['||program[i]==']'){
                if(jumpTable[i]>=0&&jumpTable[jumpTable[i]]>=0){
                    if(i!=jumpTable[jumpTable[i]]){
                        return false;
                    }
                }
                else
                    return false;
            }	
        return true;
    }

    public void run(){
        int pc=0;
        int sp=0;
        while(pc<program.length){
            char op = program[pc++];
            switch(op){
            case '+':
                memory[sp]=(memory[sp]+1)&255;
                break;
            case '-':
                memory[sp]=(memory[sp]-1)&255;
                break;
            case '<':
                if(sp==0)
                    sp=memory.length;
                sp--;
                break;
            case '>':
                if(sp==memory.length-1)
                    sp=-1;
                sp++;
                break;
            case '[':
                if(memory[sp]==0)
                    pc=jumpTable[pc-1]+1;
                break;
            case ']':
                pc=jumpTable[pc-1];
                break;
            case ',':
                try {
                    memory[sp]=System.in.read()&255;
                } catch (IOException e) {
                    memory[sp]=-1;
                }
                break;
            case '.':
                System.out.print((char)memory[sp]);
                break;
            }
        }
    }
}
