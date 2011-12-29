package util;

import java.util.Random;

public class Dice{
	private int sub;
	private int top;
	
	private Random rand;
	
	public Dice(){
		this.sub = 0;
		this.top = 100;
		
		this.rand = new Random();
	}
	
	public Dice(int top){
		this.top = top;
		
		this.rand = new Random();
	}
	
	public Dice(int top, int sub){
		this.top = top;
		this.sub = sub;
		
		this.rand = new Random();
	}
	
	public int roll(){
		int res = this.rand.nextInt(this.top - this.sub + 1);
		
		return res + this.sub;
	}
	
	public int roll(int n){
		int res = 0;
		
		for (int i = 0; i < n; i++){
			res += roll();
		}
		
		return res;
	}
	
	public int iroll(int sub, int top){
		int res = this.rand.nextInt(1 + top - sub);
		
		return res + sub;
	}
}