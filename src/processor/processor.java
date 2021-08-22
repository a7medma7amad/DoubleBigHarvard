package processor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

public class processor {
	static byte[] registers = new byte[64];
	static short[] instructionMemory = new short[1024];
	static byte[] dataMemory = new byte[2048];
	static short pc = 0;
	static byte statReg = 0b00000000;
	static short opcode;
	static short r1, r2;
	static short fetched;
	public static int numOfInstructions;

	public static void fetch() {

		fetched = instructionMemory[pc];

		System.out.println("Fetch: " + fetched);

		pc++;

	}

	public static void decode(short instruction) {

		opcode = (short) ((instruction >> 12)&15);
		r1 = (short) ((instruction & 0b0000111111000000) >> (6));
		r2 = (short) (instruction & 0b0000000000111111);
		System.out.println("Decode: " + "( opcode: " + opcode + " )" + "( R1: " + r1 + " )" + "( R2/imm: " + r2 + " )");
	}

	public static void execute(int opcode, short r1, short r2) {
		short valueR1 = registers[r1];
		short valueR2 ;
		short result = 0;
		byte overflow = 0;
		byte negative;

		System.out.print("Execute: ");
		switch (opcode) {
		case (0b0000):
			// add r1=r1+r2
			valueR2 = registers[r2];
			result = (byte) (valueR1 + valueR2);

			// carry
			if (valueR1 + valueR2 > Byte.MAX_VALUE) {
				statReg = (byte) (statReg | 0b00010000);
			} else {
				statReg = (byte) (statReg & 0b11101111);
			}

			// overflow
			if ((valueR1 > 0 && valueR1 > 0 && result < 0) || (valueR1 < 0 && valueR1 < 0 && result > 0)) {
				statReg = (byte) (statReg | 0b00001000);
				overflow = 1;
			} else {
				statReg = (byte) (statReg & 0b11110111);
				overflow = 0;
			}

			// negative
			if (result < 0) {
				statReg = (byte) (statReg | 0b00000100);
				negative = 1;
			} else {
				statReg = (byte) (statReg & 0b11111011);
				negative = 0;
			}

			// sign
			if ((negative ^ overflow) == 1) {
				statReg = (byte) (statReg | 0b00000010);
			} else {
				statReg = (byte) (statReg & 0b11111101);
			}

			// zero
			if (result == 0) {
				statReg = (byte) (statReg | 0b00000001);
			} else {
				statReg = (byte) (statReg & 0b11111110);
			}
			
			System.out.println("ADD executed, R"+r1+" ------>"+result );
			
			registers[r1] = (byte) result;
			break;
		case (0b0001):
			// sub r1=r1-r2
			valueR2 = registers[r2];
			result = (byte) (valueR1 - valueR2);

			// carry
			if (valueR1 - valueR2 > Byte.MAX_VALUE) {
				statReg = (byte) (statReg | 0b00010000);
			} else {
				statReg = (byte) (statReg & 0b11101111);
			}
			// overflow
			if ((valueR1 > 0 && valueR2 < 0 && result < 0) || (valueR1 < 0 && valueR2 > 0 && result > 0)) {
				statReg = (byte) (statReg | 0b00001000);
				overflow = 1;
			} else {
				statReg = (byte) (statReg & 0b11110111);
				overflow = 0;
			}

			// negative
			if (result < 0) {
				statReg = (byte) (statReg | 0b00000100);
				negative = 1;
			} else {
				statReg = (byte) (statReg & 0b11111011);
				negative = 0;
			}

			// sign
			if ((negative ^ overflow) == 1) {
				statReg = (byte) (statReg | 0b00000010);
			} else {
				statReg = (byte) (statReg & 0b11111101);
			}

			// zero
			if (result == 0) {
				statReg = (byte) (statReg | 0b00000001);
			} else {
				statReg = (byte) (statReg & 0b11111110);
			}
			System.out.println("SUB executed, R"+r1+" ------>"+result );
			registers[r1] = (byte) result;
			break;
		case (0b0010):
			// mul r1=r1*r2
			valueR2 = registers[r2];
			result = (byte) (valueR1 * valueR2);

			// carry
			if (valueR1 * valueR2 > Byte.MAX_VALUE) {
				statReg = (byte) (statReg | 0b00010000);
			} else {
				statReg = (byte) (statReg & 0b11101111);
			}
			// negative
			if (result < 0) {
				statReg = (byte) (statReg | 0b00000100);
				negative = 1;
			} else {
				statReg = (byte) (statReg & 0b11111011);
				negative = 0;
			}
			// zero
			if (result == 0) {
				statReg = (byte) (statReg | 0b00000001);
			} else {
				statReg = (byte) (statReg & 0b11111110);
			}
			System.out.println("MUL executed, R"+r1+" ------>"+result );
			registers[r1] = (byte) (valueR1 * valueR2);
			break;
		case (0b0011):
			// LDI r1=imm
			
			registers[r1] = (byte) r2;
			System.out.println("LDI executed, R"+r1+" ------>"+r2 );
			break;
		case (0b0100):
			// BEQZ if(r1==0) : pc=pc+1+imm
			if (r1 == 0) {
				pc = (short) (pc + r2);
				System.out.println("BEQZ executed, PC ------> "+pc);
			}
			else
				System.out.println("BEQZ executed, PC ------> "+(pc+1));
			break;
		case (0b0101):
			// and r1=r1&r2
			valueR2 = registers[r2];
			result = (short) (valueR1 & valueR2);
			// negative
			if (result < 0) {
				statReg = (byte) (statReg | 0b00000100);
				negative = 1;
			} else {
				statReg = (byte) (statReg & 0b11111011);
				negative = 0;
			}
			// zero
			if (result == 0) {
				statReg = (byte) (statReg | 0b00000001);
			} else {
				statReg = (byte) (statReg & 0b11111110);
			}
			System.out.println("AND executed, R"+r1+" ------> "+result );
			registers[r1] = (byte) result;
			break;
		case (0b0110):
			// or r1=r1|r2
			valueR2 = registers[r2];
			result = (short) (valueR1 | valueR2);
			// negative
			if (result < 0) {
				statReg = (byte) (statReg | 0b00000100);
				negative = 1;
			} else {
				statReg = (byte) (statReg & 0b11111011);
				negative = 0;
			}
			// zero
			if (result == 0) {
				statReg = (byte) (statReg | 0b00000001);
			} else {
				statReg = (byte) (statReg & 0b11111110);
			}
			System.out.println("OR executed, R"+r1+" ------> "+result );
			registers[r1] = (byte) result;
			break;
		case (0b0111):
			// jr pc = r1||r2
			valueR2 = registers[r2];
			short r11 = valueR1;
			short r22 = valueR2;

			pc = (short) (r22 + (r11 << 6));
			
			System.out.println("JR executed, PC ------> "+pc);
			break;
		case (0b1000):
			// slc r1= r1<<imm | r1>>>8 - imm
			valueR2 = registers[r2];
			result = (short) ((valueR1 << valueR2) | (valueR1 >>> (8 - valueR2)));
			// negative
			if (result < 0) {
				statReg = (byte) (statReg | 0b00000100);
				negative = 1;
			} else {
				statReg = (byte) (statReg & 0b11111011);
				negative = 0;
			}
			// zero
			if (result == 0) {
				statReg = (byte) (statReg | 0b00000001);
			} else {
				statReg = (byte) (statReg & 0b11111110);
			}
			registers[r1] = (byte) result;
			System.out.println("SLC executed, R"+r1+" ------>"+result );
			break;
		case (0b1001):
			// src r1=r1>>>imm|r1<<8-imm
			valueR2 = registers[r2];
			result = (short) ((valueR1 >>> valueR2) | (valueR1 << (8 - valueR2)));
			// negative
			if (result < 0) {
				statReg = (byte) (statReg | 0b00000100);
				negative = 1;
			} else {
				statReg = (byte) (statReg & 0b11111011);
				negative = 0;
			}
			// zero
			if (result == 0) {
				statReg = (byte) (statReg | 0b00000001);
			} else {
				statReg = (byte) (statReg & 0b11111110);
			}
			registers[r1] = (byte) result;
			System.out.println("SRC executed, R"+r1+" ------>"+result );
			break;
		case (0b1010):
			// LB r1=mem[address]

			registers[r1] = dataMemory[r2];
			System.out.println("LB executed, R"+r1+" ------> "+"Memory["+r2+"] (value= " +dataMemory[r2]+")" );
			break;
		case (0b1011):
			// SB mem[address]= r1
			dataMemory[r2] = (byte) valueR1;
		System.out.println("SB executed, "+"Memory["+r2+"]" +" ------> " + valueR1 );
			break;

		}

	}

	public static void assemble(String filepath) {
		File assembledFile = new File(filepath);
		try {
			BufferedReader csvReader = new BufferedReader(new FileReader(assembledFile));
			String row;
			int index = 0;
			while ((row = csvReader.readLine()) != null) {
				short inst = convertToML(row);
				instructionMemory[index] = inst;
				index++;

			}
			numOfInstructions = index;
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static short convertToML(String line) {
		short result = 0;
		String[] ins = line.split(" ");
		short opcode = 0;
		String r1 = ins[1];
		String r2 = ins[2];
		short r1Val = 0;
		short r2Val = 0;
		switch (ins[0].toUpperCase()) {
		case "ADD":
			opcode = 0;
			r1Val = regNameToNum(r1);
			r2Val = regNameToNum(r2);
			break;
		case "SUB":
			opcode = 1;
			r1Val = regNameToNum(r1);
			r2Val = regNameToNum(r2);
			break;
		case "MUL":
			opcode = 2;
			r1Val = regNameToNum(r1);
			r2Val = regNameToNum(r2);
			break;
		case "LDI":
			opcode = 3;
			r1Val = regNameToNum(r1);
			// r2Val = regNameToNum(r2);
			r2Val = Short.parseShort(r2);
			break;
		case "BEQZ":
			opcode = 4;
			r1Val = regNameToNum(r1);
			r2Val = Short.parseShort(r2);

			break;
		case "AND":
			opcode = 5;
			r1Val = regNameToNum(r1);
			r2Val = regNameToNum(r2);
			break;
		case "OR":
			opcode = 6;
			r1Val = regNameToNum(r1);
			r2Val = regNameToNum(r2);
			break;
		case "JR":
			opcode = 7;
			r1Val = regNameToNum(r1);
			r2Val = regNameToNum(r2);
			break;
		case "SLC":
			opcode = 8;
			r1Val = regNameToNum(r1);
			r2Val = Short.parseShort(r2);
			break;
		case "SRC":
			opcode = 9;
			r1Val = regNameToNum(r1);
			r2Val = Short.parseShort(r2);
			break;
		case "LB":
			opcode = 10;
			r1Val = regNameToNum(r1);
			r2Val = Short.parseShort(r2);
			break;
		case "SB":
			opcode = 11;
			r1Val = regNameToNum(r1);
			r2Val = Short.parseShort(r2);
			break;
		}
		opcode = (short) (opcode << 12);
		r1Val = (short) (r1Val << 6);

		result = (short) (opcode + r1Val + r2Val);
		return result;

	}

	public static short regNameToNum(String regName) {
		short res = 0;
		regName = regName.replace("R", "");
		regName = regName.replace("r", "");
		regName = regName.trim();

		res = Short.parseShort(regName);
		return res;
	}
	public static void runAssemblyProgram(String filePath) {
		assemble(filePath);
		int clockCycle = 1;
		int maxCycles = (3 + (numOfInstructions - 1));
		for (; clockCycle <= maxCycles; clockCycle++) {
			System.out.println("clock cycle number: " + clockCycle);
			System.out.println();
			if (clockCycle >= 3) {
				execute(opcode, r1, r2);
				System.out.println("successfully Executed instruction " + (clockCycle - 2));
				System.out.println();
			}
			if (clockCycle >= 2 & clockCycle < maxCycles) {
				decode(fetched);
				System.out.println("successfully Decoded instruction " + (clockCycle - 1));
				System.out.println();
			}

			if (clockCycle < maxCycles - 1) {
				fetch();
				System.out.println("successfully Fetched instruction " + clockCycle);
				System.out.println();
			}
			System.out.println("-------------------------------------------");
		}
		
		System.out.println("printing contents of Registers .....");
		System.out.println(Arrays.toString(registers));
		System.out.println("-------------------------------------------");
		
		System.out.println("printing contents of Instruction Memory .....");
		System.out.println(Arrays.toString(instructionMemory));
		System.out.println("-------------------------------------------");
		
		System.out.println("printing contents of Main Memory .....");
		System.out.println(Arrays.toString(dataMemory));
		System.out.println("-------------------------------------------");
		
	}
	public static void main(String[] args) {
		
		runAssemblyProgram("src/data/assemblyProgram.txt");
		//System.out.println(convertToML("SRC R1 7"));
	}

}
