package itc;

import helpers.Pair;


public class Id{

	private int tipo;
	private int n;
	private Id i1;
	private Id i2;
	
	// tipo = 1 : i1 = i2 = null ; n = val
	// tipo = 2 : n = null ; i1 & i2 are Id
	
	public Id(){
		this.n = 1;
		this.tipo = 1;
		this.i1 = null;
		this.i2 = null;
	}
	
	public Id(int val){
		this.n = val;
		this.tipo = 1;
		this.i1 = null;
		this.i2 = null;
	}
	
	public Id(Id i){
		this.n = i.getN();
		this.tipo = i.getTipo();
		this.i1 = new Id();
		this.i1 = i.getId1();
		this.i2 = new Id();
		this.i2 = i.getId2();
	}
	
	// Metodos de instancia
	public Id split(){ // split shall return the right hand id of the result, while making himself the left hand id
		Id i2 = new Id();
		
		if (this.tipo == 1 && this.n == 0){ // id = 0
			this.tipo = 1;
			this.n = 0;
			
			i2.setTipo(1);
			i2.setN(0);
		}else if (this.tipo == 1 && this.n == 1){ // id = 1
			this.tipo = 2;
			this.n = 0;
			this.i1 = new Id(1);
			this.i2 = new Id(0);
			
			i2.setTipo(2);
			i2.setId1(new Id(0));
			i2.setId2(new Id(1));
		}else{
			if(this.tipo == 2 && (this.i1.getTipo() == 1 && this.i1.getN() == 0) && (this.i2.getTipo() == 2 || this.i2.getN() == 1)){ // id = (0, i)
				this.tipo = 2;
				this.n = 0;
				this.i1 = new Id(0);
				
				i2.setTipo(2);
				i2.setId1(new Id(0));
				i2.setId2(this.i2.split());
			}else if(this.tipo == 2 && (this.i1.getTipo() == 2 || this.i1.getN() == 1) && (this.i2.getTipo() == 1 && this.i2.getN() == 0)){ // id = (i, 0)
				this.tipo = 2;
				this.n = 0;
				this.i2 = new Id(0);
				
				i2.setTipo(2); 
				i2.setId1(this.i1.split());
				i2.setId2(new Id(0));
			}else if(this.tipo == 2 && (this.i1.getTipo() == 2 || this.i1.getN() == 1) && (this.i2.getTipo() == 2 || this.i2.getN() == 1)){ // id = (i1, i2)
				i2.setTipo(2); 
				i2.setId2(this.i2);
				i2.setId1(new Id(0));
				
				this.tipo = 2;
				this.n = 0;
				// this.i1 = this.i1;
				this.i2 = new Id(0);
			}else{
				System.out.println("Bug..." + this.tostring());
			}
		}
 		return i2;
	}
	
	public Pair<Id> split2(){
		Id i1 = new Id();
		Id i2 = new Id();
		
		if (this.tipo == 1 && this.n == 0){ // id = 0
			i1.setTipo(1);
			i1.setN(0);
			
			i2.setTipo(1);
			i2.setN(0);
		}else if (this.tipo == 1 && this.n == 1){ // id = 1
			i1.setTipo(2);
			i1.setN(0);
			i1.setId1(new Id(1));
			i1.setId2(new Id(0));
			
			i2.setTipo(2);
			i2.setN(0);
			i2.setId1(new Id(0));
			i2.setId2(new Id(1));
		}else{
			if(this.tipo == 2 && (this.i1.getTipo() == 1 && this.i1.getN() == 0) && (this.i2.getTipo() == 2 || this.i2.getN() == 1)){ // id = (0, i)				
				Pair<Id> ip = this.i2.split2();
				
				i1.setTipo(2);
				i1.setN(0);
				i1.setId1(new Id(0));
				i1.setId2((Id) ip.getEa());
				
				i2.setTipo(2);
				i2.setN(0);
				i2.setId1(new Id(0));
				i2.setId2((Id) ip.getEb());
			}else if(this.tipo == 2 && (this.i1.getTipo() == 2 || this.i1.getN() == 1) && (this.i2.getTipo() == 1 && this.i2.getN() == 0)){ // id = (i, 0)
				Pair<Id> ip = this.i1.split2();
				
				i1.setTipo(2);
				i1.setN(0);
				i1.setId1((Id) ip.getEa());
				i1.setId2(new Id(0));
				
				i2.setTipo(2);
				i2.setN(0);
				i2.setId1((Id) ip.getEb());
				i2.setId2(new Id(0));
			}else if(this.tipo == 2 && (this.i1.getTipo() == 2 || this.i1.getN() == 1) && (this.i2.getTipo() == 2 || this.i2.getN() == 1)){ // id = (i1, i2)				
				i1.setTipo(2);
				i1.setN(0);
				i1.setId1(this.i1.clone());
				i1.setId2(new Id(0));
				
				i2.setTipo(2);
				i2.setN(0);
				i2.setId1(new Id(0));
				i2.setId2(this.i2.clone());
			}else{
				System.out.println("Bug..." + this.tostring());
			}
		}
		
		Pair<Id> pair = new Pair<Id>();
		pair.setEa(i1);
		pair.setEb(i2);
 		return pair;
	}
	
	public void sum(Id i1, Id i2){ // this becomes the sum between i1 and i2
		
		this.i1 = new Id();
		this.i2 = new Id();
		
		if(i1.getTipo() == 1 && i1.getN() == 0 && i2.getTipo() == 1 && i2.getN() == 0){
			this.tipo = 1;
			this.n = 0;
		}else if(i1.getTipo() == 1 && i1.getN() == 0 && (i2.getN() == 1 || i2.getTipo() == 2)){ // sum(0, X) -> X;
			this.tipo = i2.getTipo();
			this.n = i2.getN();
			this.i1 = i2.getId1();
			this.i2 = i2.getId2();
		}else if((i1.getN() == 1 || i1.getTipo() == 2) && i2.getTipo() == 1 && i2.getN() == 0){ // sum(X, 0) -> X;
			this.tipo = i1.getTipo();
			this.n = i1.getN();
			this.i1 = i1.getId1();
			this.i2 = i1.getId2();
		}else if(i1.getTipo() == 2 && i2.getTipo() == 2){ // sum({L1,R1}, {L2, R2}) -> norm_id({sum(L1, L2), sum(R1, R2)}).
			this.tipo = 2;
			this.i1.sum(i1.getId1(), i2.getId1());
			this.i2.sum(i1.getId2(), i2.getId2());
			this.normalize();
		}else {
			System.out.println("fail Id ..."+ i1.getTipo() + " " + i2.getTipo());
			System.out.println("flail heck ..."+ i1.getN() + " " + i2.getN());
		}// else do nothing
	}
	
	public void normalize(){
		if (this.tipo == 2 && this.i1.getTipo() == 1 && this.i1.getN() == 0 && this.i2.getTipo() == 1 && this.i2.getN() == 0){
			this.tipo = 1;
			this.n = 0;
			this.i1 = this.i2 = null;
		}else if (this.tipo == 2 && this.i1.getTipo() == 1 && this.i1.getN() == 1 && this.i2.getTipo() == 1 && this.i2.getN() == 1){
			this.tipo = 1;
			this.n = 1;
			this.i1 = this.i2 = null;
		}// else do nothing
	}

	public char[] dEncode(){
		return this.encode(null).unify();
	}
	
	// code and decode dos ids
	public BitArray encode(BitArray bt){
		if (bt == null){
			bt = new BitArray();
		}
		
		if ( this.tipo == 1 && this.n == 0 ){//System.out.println("id enc a1");
			bt.addbits(0, 3);
		}else if ( this.tipo == 1 && this.n == 1 ){//System.out.println("id enc a2");
			bt.addbits(0, 2);
			bt.addbits(1, 1); 
		}else if (this.tipo == 2 && (this.i1.getTipo() == 1 && this.i1.getN() == 0) && (this.i2.getTipo() == 2 || this.i2.getN() == 1)){//System.out.println("id enc b");
			bt.addbits(1, 2);
			this.i2.encode(bt);
		}else if (this.tipo == 2 && (this.i2.getTipo() == 1 && this.i2.getN() == 0) && (this.i1.getTipo() == 2 || this.i1.getN() == 1)){//System.out.println("id enc c");
			bt.addbits(2, 2);
			this.i1.encode(bt);
		}else if (this.tipo == 2 && (this.i2.getTipo() == 2 || this.i2.getN() == 1) && (this.i1.getTipo() == 2 || this.i1.getN() == 1)){//System.out.println("id enc d");
			bt.addbits(3, 2); 	
			this.i1.encode(bt);
			this.i2.encode(bt);
		}else {
			System.out.println("BUG - ENCODE");
			System.out.println("this tipo " + this.tipo);
			System.out.println(" i1 tipo " + this.i1.getTipo());
			System.out.println(" i2 tipo " + this.i2.getTipo());
		}
		//System.out.println(" i2 tipo " + (int)bt.getIndex(0));
		return bt;
	}
	
	public void decode(BitArray bt){
		int val = bt.readbits(2);
		if ( val == 0 ){//System.out.println("Id dec a");
			int x = bt.readbits(1);//printf("chceck\n");
			
			this.tipo = 1;
			this.n = x;
		}else if ( val == 1 ){//System.out.println("Id dec b");
			this.tipo = 2;
			
			this.i1 = new Id(0);
			this.i2 = new Id();
			this.i2.decode(bt);
		}else if ( val == 2 ){//System.out.println("Id dec c");
			this.tipo = 2;
			
			this.i1 = new Id();
			this.i1.decode(bt);
			this.i2 = new Id(0);
		}else if ( val == 3 ){//System.out.println("Id dec d");
			this.tipo = 2;
			
			this.i1 = new Id();
			this.i1.decode(bt);
			this.i2 = new Id();
			this.i2.decode(bt);
		}else{
			System.out.println("BUG - DECODE");
		}
	}
	
	
	// gets, sets e outros
	public void setTipo(int v){ this.tipo = v; }
	public void setN(int v){ this.n = v; }
	public void setId1(Id ni){ this.i1 = ni; }
	public void setId2(Id ni){ this.i2 = ni; }
	
	public int getTipo(){ return this.tipo; }
	public int getN(){ return this.n; }
	public Id getId1(){ 
		if (this.i1 != null) return this.i1.clone();
		else return null;
	}
	public Id getId2(){
		if (this.i2 != null) return this.i2.clone();
		else return null;
	}
	
	public String tostring(){
		String res = new String();
		
		if (this.tipo == 1){
			res = res + this.n;
		}else if (this.tipo == 2){
			res = "("+res + this.i1.tostring() + ", " + this.i2.tostring() +")";
		}
		
		return res;
	}
	
	public Id clone(){
		Id res = new Id();
		
		res.setN(this.n);
		res.setTipo(this.tipo);
		if ( this.tipo == 2){
			res.setId1(this.i1.clone());
			res.setId2(this.i2.clone());
		}else{
			res.setId1(this.i1);
			res.setId2(this.i2);
		}
		
		
		return res;
	}
}
