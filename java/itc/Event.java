package itc;


public class Event{ 
	
	private int type;	
	private int n;
	private Event e1;
	private Event e2;
	
	// type = 1 : i1 = i2 = null ; n = val
	// type = 2 : n = val ; i1 & i2 are Id
	
	public Event(){
		this.n = 0;
		this.type = 1;
		this.e1 = null;
		this.e2 = null;
	}
	
	public Event(int val){
		this.n = val;
		this.type = 1;
		this.e1 = null;
		this.e2 = null;
	}
	
	public Event(Event e){
		this.type = e.getType();
		this.n = e.getN();
		this.e1 = e.getE1();
		this.e2 = e.getE2();
	}
	
	// Metodos de instancia
	public void join_ev(Event e1, Event e2){
		
		if (e1.getType() == 2 && e2.getType() == 2) {
			if (e1.getN() > e2.getN()){
				//System.out.println("caso 1");
				this.join_ev(e2, e1); 
			}else{//System.out.println("caso 2");
				int d = e2.getN() - e1.getN();
				Event tmp = new Event();
				
				this.e1 = new Event();
				this.e2 = new Event();
				
				this.n = e1.getN();
				this.setType(2);
				tmp = e2.getE1();
				tmp.lift(d);
				this.e1.join_ev(e1.getE1(), tmp);
				
				tmp = e2.getE2();
				tmp.lift(d);
				this.e2.join_ev(e1.getE2(), tmp);
				
				this.normalize();
			}
			/*join_ev(E1={N1, _, _}, E2={N2, _, _}) when N1 > N2 -> join_ev(E2, E1);
			join_ev({N1, L1, R1}, {N2, L2, R2}) when N1 =< N2 ->
				D = N2 - N1,
				norm_ev({N1, join_ev(L1, lift(D, L2)), join_ev(R1, lift(D, R2))});*/
		}else if (e1.getType() == 1 && e2.getType() == 2) {// join_ev(N1, {N2, L2, R2}) -> join_ev({N1, 0, 0}, {N2, L2, R2});
			//System.out.println("caso 3");
			e1.setType(2);
			e1.setE1(new Event(0));
			e1.setE2(new Event(0));
			this.join_ev(e1, e2);
		}else if (e1.getType() == 2 && e2.getType() == 1) {// join_ev({N1, L1, R1}, N2) -> join_ev({N1, L1, R1}, {N2, 0, 0});
			//System.out.println("caso 4");
			e2.setType(2);
			e2.setE1(new Event(0));
			e2.setE2(new Event(0));
			this.join_ev(e1, e2);
		}else if (e1.getType() == 1 && e2.getType() == 1) {// join_ev(N1, N2) -> max(N1, N2).
			//System.out.println("caso 5");
			this.type = 1;
			this.n = (int) Math.max((double)e1.getN(), (double)e2.getN());
		}else{
			System.out.println("fail Event ..."+ e1.getType() + " " + e2.getType());
			System.out.println("flail heck ..."+ e1.getN() + " " + e2.getN());
		}
		/*join_ev(E1={N1, _, _}, E2={N2, _, _}) when N1 > N2 -> join_ev(E2, E1);
		join_ev({N1, L1, R1}, {N2, L2, R2}) when N1 =< N2 ->
		  D = N2 - N1,
		  norm_ev({N1, join_ev(L1, lift(D, L2)), join_ev(R1, lift(D, R2))});
		join_ev(N1, {N2, L2, R2}) -> join_ev({N1, 0, 0}, {N2, L2, R2});
		join_ev({N1, L1, R1}, N2) -> join_ev({N1, L1, R1}, {N2, 0, 0});
		join_ev(N1, N2) -> max(N1, N2).*/
	}
	
	public void normalize(){ // transform itself in the normal form
		if (this.type == 2 && this.e1.getType() == 1 && this.e2.getType() == 1 && this.e1.getN() == this.e2.getN()){ // norm_ev({N, M, M}) when is_integer(M) -> N + M;
			this.type = 1;
			this.n += this.e1.getN(); // either e1.n or e2.n, both the same
		}else if(this.type == 2){ // norm_ev({N, L, R}) ( basicamente um else )
			int mm = (int) Math.min((double) this.e1.base(), (double) this.e2.base());
			this.n += mm;
			this.e1.drop(mm);
			this.e2.drop(mm);
		}
		/*norm_ev({N, M, M}) when is_integer(M) -> N + M;
		norm_ev({N, L, R}) ->
		  M = min(base(L), base(R)),
		  {N + M, drop(M, L), drop(M, R)}.*/
	}
	
	public void lift(int val){
		this.n += val;
		
		/*lift(M, {N, L, R}) -> {N + M, L ,R};
		lift(M, N) -> N + M.*/
	}
	
	public void drop(int val){ // drops itself for val
		if (val <= this.n) {
			this.n -= val;
		}// else do nothing
		
		/*drop(M, {N, L, R}) when M =< N -> {N - M, L ,R};
		drop(M, N) when M =< N -> N - M.*/
	}
	
	public int base(){
		return this.n;
		
		/*base({N, _, _}) -> N;
		base(N) -> N.*/
	}
	
	public int height(){
		if (this.type == 1) return this.n;
		else return this.n + (int) Math.max((double) e1.height(), (double) e2.height());
		
		/*height({N, L, R}) -> N + max(height(L), height(R));
		height(N) -> N.*/
	}
	
	public boolean leq_ev(Event e2){
		if (this.type == 2 && e2.getType() == 2) {
			Event xl1 = new Event(); Event xl2 = new Event();
			Event xr1 = new Event(); Event xr2 = new Event();
			xl1 = this.getE1();xl1.lift(this.n);
			xl2 = e2.getE1();xl2.lift(e2.getN());
			
			xr1 = this.getE2(); xr1.lift(this.n);
			xr2 = e2.getE2(); xr2.lift(e2.getN());
			
			return (this.n <= e2.getN()) && xl1.leq_ev(xl2) && xr1.leq_ev(xr2);
		}else if (this.type == 2 && e2.getType() == 1) {
			Event xl1 = new Event(); 
			Event xr1 = new Event();
			
			xl1 = this.getE1();xl1.lift(this.n);
			xr1 = this.getE2();xr1.lift(this.n);
			
			return (this.n <= e2.getN()) && xl1.leq_ev(e2) && xr1.leq_ev(e2);
		}else if (this.type == 1 && (e2.getType() == 2 || e2.getType() == 1)) {
			return this.n <= e2.getN();
		}else{
			System.out.println("Something is wrong.");
		}
		
		/*leq_ev({N1, L1, R1}, {N2, L2, R2}) ->
		  N1 =< N2 andalso
		  leq_ev(lift(N1, L1), lift(N2, L2)) andalso
		  leq_ev(lift(N1, R1), lift(N2, R2));

		leq_ev({N1, L1, R1}, N2) ->
		  N1 =< N2 andalso
		  leq_ev(lift(N1, L1), N2) andalso
		  leq_ev(lift(N1, R1), N2);

		leq_ev(N1, {N2, _, _}) -> N1 =< N2;

		leq_ev(N1, N2) -> N1 =< N2.*/
		return false;
	}
	
	public BitArray encode(BitArray bt){
		if (bt == null)
			bt = new BitArray();
		
		if (this.type == 1){
			bt.addbits(1, 1);//printf("g\n");
			enc_n(bt, this.n, 2);
		} else if ((this.type == 2 && this.n == 0) && (e1.getType() == 1 && e1.getN() == 0) && (e2.getType() == 2 || e2.getN() != 0)){//printf("a\n");
			bt.addbits(0, 1);
			bt.addbits(0, 2);
			this.e2.encode(bt);
		} else if ((this.type == 2 && this.n == 0) && (e1.getType() == 2 || e1.getN() != 0) && (e2.getType() == 1 && e2.getN() == 0)){//printf("b\n");
			bt.addbits(0, 1);
			bt.addbits(1, 2);
			this.e1.encode(bt);
		} else if ((this.type == 2 && this.n == 0) && (e1.getType() == 2 || e1.getN() != 0) && (e2.getType() == 2 || e2.getN() != 0)){//printf("c\n");
			bt.addbits(0, 1);
			bt.addbits(2, 2);
			this.e1.encode(bt);
			this.e2.encode(bt);
		} else if ((this.type == 2 && this.n != 0) && (e1.getType() == 1 && e1.getN() == 0) && (e2.getType() == 2 || e2.getN() != 0)){//printf("d\n");
			bt.addbits(0, 1);
			bt.addbits(3, 2);
			bt.addbits(0, 1);
			bt.addbits(0, 1);
			enc_n(bt, this.n, 2);
			this.e2.encode(bt);
		} else if ((this.type == 2 && this.n != 0) && (e1.getType() == 2 || e1.getN() != 0) && (e2.getType() == 1 && e2.getN() == 0)){//printf("e\n");
			bt.addbits(0, 1);
			bt.addbits(3, 2);
			bt.addbits(0, 1);
			bt.addbits(1, 1);
			enc_n(bt, this.n, 2);
			this.e1.encode(bt);
		} else if ((this.type == 2 && this.n != 0) && (e1.getType() == 2 || e1.getN() != 0) && (e2.getType() == 2 || e2.getN() != 0)){//printf("f\n");
			bt.addbits(0, 1);
			bt.addbits(3, 2);
			bt.addbits(1, 1);
			enc_n(bt, this.n, 2);
			this.e1.encode(bt);
			this.e2.encode(bt);
		} else{
			System.out.println("Something is wrong : encode " + this.type + " " + this.n);
			if(this.type == 2){
				System.out.println("                   : encode " + e1.getType() + " " + e1.getN());
				System.out.println("                   : encode " + e2.getType() + " " + e2.getN());
			}
		}
		
		return bt;
	}
	
	public void enc_n(BitArray bt, int val, int nb){
		//printf("enc %d %d\n", val, nb);
		if (val < (1 << nb)){
			bt.addbits(0, 1);
			//printf("%d\t enc %d %d\n", be->ub, val, nb);
			bt.addbits(val, nb);
		}else{
			bt.addbits(1, 1);
			enc_n(bt, val - (1 << nb), nb+1);
		}
	}
	
	
	public void decode(BitArray bt){
		int val = bt.readbits(1);
		if (val == 1){//printf("g\n");
			this.type = 1;
			this.n = dec_n(bt);
		}else if (val == 0){
			val = bt.readbits(2);
			if (val == 0){//printf("a\n");
				this.type = 2;
				this.n = 0;
				this.e1 = new Event(0);
				this.e2 = new Event();
				this.e2.decode(bt);
			}else if(val == 1){//printf("b\n");
				this.type = 2;
				this.n = 0;
				this.e1 = new Event();
				this.e1.decode(bt);
				this.e2 = new Event(0);
			}else if(val == 2){//printf("c\n");
				this.type = 2;
				this.n = 0;
				this.e1 = new Event();
				this.e1.decode(bt);
				this.e2 = new Event();
				this.e2.decode(bt);
			}else if(val == 3){
				val = bt.readbits(1);
				if ( val == 0 ){ // 0
					val = bt.readbits(1);
					if (val == 0){ //printf("d\n");// 0
						this.type = 2;
						this.n = dec_n(bt);
						this.e1 = new Event(0);
						this.e2 = new Event();
						this.e2.decode(bt);
					}else if (val == 1){//printf("e\n");
						this.type = 2;
						this.n = dec_n(bt);
						this.e1 = new Event();
						this.e1.decode(bt);
						this.e2 = new Event(0);
					}else {System.out.println("Something is wrong : decode a");}
				}else if (val == 1){//printf("f\n");
					this.type = 2;
					this.n = dec_n(bt);
					this.e1 = new Event();
					this.e1.decode(bt);
					this.e2 = new Event();
					this.e2.decode(bt);
				}else{System.out.println("Something is wrong : decode b");}
			}else{System.out.println("Something is wrong : decode c");}
		}else{System.out.println("Something is wrong : decode d");}
	}
	
	public char dec_n(BitArray bt){

		int n = 0;
		int b = 2;
		while(bt.readbits(1) == 1){
			n += (1 << b);
			//printf("%d\tdec %d %d\n",be->sb, n, b);
			b++;
		}
		int n2 = bt.readbits(b);
		n+=n2;
		//printf("val %d %d -- %d\n", n2, b, be->sb);
		return (char) n;
	}
	
	public char[] dEncode(){
		return this.encode(null).unify();
	}
	
	// gets e sets
	public void setType(int val){ this.type = val; }
	public void setN(int val){ this.n = val; }
	public void setE1(Event e){ this.e1 = e; }
	public void setE2(Event e){ this.e2 = e; }
	
	public int getType(){ return this.type; }
	public int getN(){ return this.n; }
	public Event getE1(){ 
		if (this.e1 != null) return this.e1.clone();
		else return null;
	}
	public Event getE2(){
		if (this.e2 != null) return this.e2.clone();
		else return null;
	}
	
	public String tostring(){
		String res = new String();
			
		if (this.type == 1){
			res = res + (int) this.n;
		}else if (this.type == 2){
			res = "(" + (int) this.n + " (" + e1.tostring() + ", " + e2.tostring() + "))";
		}else{
			System.out.println("ERROR tostring unknown type ");
		}
		
		return res;
	}
	
	public boolean equals(Event e2){
		if(this.type == 1 && this.type == e2.getType() && this.n == e2.getN()) return true;
		if(this.type == 2 && this.type == e2.getType()) return this.n == e2.getN() && this.e1.equals(e2.getE1()) && this.e2.equals(e2.getE2());
		return false;
	}
	
	public Event clone(){
		Event res = new Event();
		
		res.setType(this.type);
		res.setN(this.n);
		
		if (this.type == 1) {
			res.setE1(e1);
			res.setE2(e2);
		}else{
			res.setE1(this.e1.clone());
			res.setE2(this.e2.clone());
		}
		
		return res;
	}
}
