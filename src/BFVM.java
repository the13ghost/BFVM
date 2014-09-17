import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;


public class BFVM {
	public static void main(String[] arg){
		String greet =  "The13Ghost's Java Brainfuck interpreter\n"+
						"\t30k memory size\n"+
						"\t0-255 cell range\n";
		
		Scanner input = new Scanner(System.in);
		BFVM vm = new BFVM();
		
		System.out.println(greet);
		String in;
		
		do {
			System.out.print(">");
			in = input.nextLine();
			if(in.compareToIgnoreCase("q")!=0){
				if(vm.loadProgram(in))
					vm.run();
				else
					System.out.println("Compilation Error. mismatched [ or ]");
			}
			System.out.println();
			for(int i=0;i<100;i++)
				System.out.print(vm.memory[i]+", ");
			System.out.println();
		}while(in.compareToIgnoreCase("q")!=0);
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
					sp=30000;
				sp--;
				break;
			case '>':
				if(sp==29999)
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
