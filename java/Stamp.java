
public class Stamp{
	
	private Event e;
	private Id i;
	
	public Stamp(){
		e = new Event();
		i = new Id();
	}

	public Stamp(Id i, Event e){
		this.i = i;
		this.e = e;
	}

	public Stamp(Stamp s){
		this.e = s.getEvent().clone();
		this.i = s.getId().clone();
	}
	
	// Metodos de instancia
	public Stamp fork(){ // self fork : s1 self (left), return s2 // new policy return a Stamp
		
		Stamp s2 = new Stamp();
		s2.setEvent(this.e.clone());
		s2.setId(this.i.split()); // split shall return the right hand id, while making himself the left hand id
		
		return s2;
	}
	
	public Pair<Stamp> fork2(){
		Stamp a = new Stamp();
		Stamp b = new Stamp();
		
		a.setEvent(this.e.clone());
		b.setEvent(this.e.clone());
		
		Pair<Id> par = this.i.split2();
		a.setId((Id) par.getEa());
		b.setId((Id) par.getEb());
		
		Pair<Stamp> p = new Pair<Stamp>();
		p.setEa(a);
		p.setEb(b);
		
		return p;
	}
	

	public void join(Stamp s2){ // joins two stamps becoming itself the result stamp
		this.i.sum(this.getId(), s2.getId());
		this.e.join_ev(this.getEvent(), s2.getEvent());
		/* join({I1, E1}, {I2, E2}) -> {sum(I1,I2), join_ev(E1, E2)}. */
	}

	public static Stamp join(Stamp s1, Stamp s2){ // joins two stamps, returning the resulting stamp
		Id i = new Id();
		i.sum(s1.getId(), s2.getId());
		Event e = new Event();
		e.join_ev(s1.getEvent(), s2.getEvent());
		return new Stamp(i,e);
	}

	public Stamp peek() {
		Id id = new Id(0);
		Event ev = this.e.clone();
		return new Stamp(id,ev);
	}
	
	public Stamp event(){ // returns the new updated Stamp
		Stamp res = new Stamp();
		Event tmp = new Event( fill(this.i, this.e) );
		
		if (this.e.equals(tmp)){
			res = grow(this.i, this.e);
			res.setId(this.i.clone());
		}else{
			res.setEvent(tmp);
			res.setId(this.i.clone());
		}
		
		return res;
		/*event({I, E}) ->
		{I,
		 case fill(I, E) of
		 	E -> {_, E1} = grow(I, E), E1;
	 	 	E1 -> E1
		 end
		}.*/
	}
	
	public boolean leq(Stamp s2){
		return this.e.leq_ev(s2.getEvent());
	}
	
	public Event fill(Id i, Event e){
		Event res = new Event();
		
		if (i.getTipo() == 1 && i.getN() == 0){
			return e.clone();
		} else if (i.getTipo() == 1 && i.getN() == 1){
			res.setType(1);
			res.setN(e.height());
			
			return res;
		} else if (e.getType() == 1){
			return e.clone();
		} else if (i.getTipo() == 2 && i.getId1().getN() == 1 && i.getId1().getTipo() == 1 && (i.getId2().getTipo() == 2 || (i.getId2().getN() == 0 && i.getId2().getTipo() == 1)) && e.getType() == 2){
			res.setType(2);
			res.setN(e.getN());
			res.setE2(fill(i.getId2(), e.getE2()));
			res.setE1(new Event((int) Math.max((double) e.getE1().height(), (double) res.getE2().base())));
			
			res.normalize();
		} else if (i.getTipo() == 2 && i.getId2().getTipo() == 1 && i.getId2().getN() == 1 && (i.getId1().getTipo() == 2 || (i.getId1().getN() == 0 && i.getId1().getTipo() == 1)) && e.getType() == 2){
			res.setType(2);
			res.setN(e.getN());
			res.setE1(fill(i.getId1(), e.getE1()));
			res.setE2(new Event((int) Math.max((double) e.getE2().height(), (double) res.getE1().base())));
			
			res.normalize();
		} else if (i.getTipo() == 2 && e.getType() == 2) {
			res.setType(2);
			res.setN(e.getN());
			res.setE1(new Event(fill(i.getId1(), e.getE1())));
			res.setE2(new Event(fill(i.getId2(), e.getE2())));
			
			res.normalize();
		}else{
			System.out.println("ZOMG xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
		}
		
		return res;
		/*fill(0, E) -> E;
		fill(1, E={_, _, _}) -> height(E);
		fill(_, N) when is_integer(N) -> N;
		fill({1, R}, {N, El, Er}) ->
		  Er1 = fill(R, Er),
		  D = max(height(El), base(Er1)),
		  norm_ev({N, D, Er1});
		fill({L, 1}, {N, El, Er}) ->
		  El1 = fill(L, El),
		  D = max(height(Er), base(El1)),
		  norm_ev({N, El1, D});
		fill({L, R}, {N, El, Er}) ->
		  norm_ev({N, fill(L, El), fill(R, Er)}).*/
	}
	
	public Stamp grow(Id i, Event e){// Stamp returned has a fake Id, only it holds the height of something
		Stamp res = new Stamp();
		
		if (i.getTipo() == 1 && i.getN() == 1 && e.getType() == 1) {
			res.setId( new Id(0) );
			res.setEvent( new Event(e.getN() + 1) );
		}else if (i.getTipo() == 2 && i.getId1().getN() == 0 && (i.getId2().getTipo() == 2 || (i.getId2().getN() == 1 && i.getId2().getTipo() == 1)) && e.getType() == 2) {
			Stamp aux = new Stamp();
			
			aux = grow(i.getId2(), e.getE2());
			res.setId(new Id(aux.getId().getN()+1));
			
			Event rese = new Event(e.getN());
			rese.setType(2);
			rese.setE1(e.getE1());
			rese.setE2(aux.getEvent());
			res.setEvent(rese);
		}else if (i.getTipo() == 2 && i.getId2().getN() == 0 && (i.getId1().getTipo() == 2 || (i.getId1().getN() == 1 && i.getId1().getTipo() == 1)) && e.getType() == 2) {
			Stamp aux = new Stamp();
			
			aux = grow(i.getId1(), e.getE1());
			res.setId(new Id(aux.getId().getN()+1));
			
			Event rese = new Event(e.getN());
			rese.setType(2);
			rese.setE1(aux.getEvent());
			rese.setE2(e.getE2());
			res.setEvent(rese);
		}else if (i.getTipo() == 2 && (i.getId1().getTipo() == 2 || i.getId1().getN() == 1) && (i.getId2().getTipo() == 2 || i.getId2().getN() == 1) && e.getType() == 2) {
			Stamp left = new Stamp();
			Stamp right = new Stamp();
			
			left = grow(i.getId1(), e.getE1());
			right = grow(i.getId2(), e.getE2());
			if (left.getId().getN() < right.getId().getN()){
				res.setId(new Id(left.getId().getN()+1));
				
				Event rese = new Event(e.getN());
				rese.setType(2);
				rese.setE1(left.getEvent());
				rese.setE2(e.getE2());
				res.setEvent(rese);
			}else{
				res.setId(new Id(right.getId().getN()+1));
				
				Event rese = new Event(e.getN());
				rese.setType(2);
				rese.setE1(e.getE1());
				rese.setE2(right.getEvent());
				res.setEvent(rese);
			}
		}else if (e.getType() == 1){
			Event aux = new Event(e.getN());
			aux.setType(2);
			aux.setE1(new Event(0));
			aux.setE2(new Event(0));
			
			res = grow(i, aux);
			res.setId(new Id(1000 + res.getId().getN()));
		}else{
			System.out.println("OMFG ^^ ^^ ^ ^^ ^ ^^ ^^^^^^ ^^ ^^ ^^ ^^ ^^ ^^ ^^ ^^ ^ ^ ^^ ^ ^ ^ ^ ^ ^^^^");
		}
		
		return res;
		/*grow(1, N) when is_integer(N)->
		  {0, N + 1};
		grow({0, I}, {N, L, R}) ->
		  {H, E1} = grow(I, R),
		  {H + 1, {N, L, E1}};
		grow({I, 0}, {N, L, R}) ->
		  {H, E1} = grow(I, L),
		  {H + 1, {N, E1, R}};
		grow({Il, Ir}, {N, L, R}) ->
		  {Hl, El} = grow(Il, L),
		  {Hr, Er} = grow(Ir, R),
		  if
		    Hl < Hr -> {Hl + 1, {N, El, R}};
		    true ->    {Hr + 1, {N, L, Er}}
		  end;
		grow(I, N) when is_integer(N)->
		  {H, E} = grow(I, {N, 0, 0}),
		  {H + 1000, E}.*/
	}
	
	public BitArray encode(){
		return this.e.encode(null);
	}
	
	public char[] dEncode(){
		char[] res;
		
		char[] idcode = this.i.dEncode();
		char[] eventcode = this.e.dEncode();
		
		int dts = 16;
		int isize = 3 + ((int) (idcode[0]+idcode[1]) / dts);
		int esize = 3 + ((int) (eventcode[0]+eventcode[1]) / dts);
		
		res = new char[isize + esize];
		System.arraycopy(idcode, 0, res, 0, isize);
		System.arraycopy(eventcode, 0, res, isize, esize);
		
		return res;
	}
	
	
	//size of stamp in bits
	public int sizeInBits(){
		int Ibits = this.i.encode(null).getSizeBits();
		int Ebits = this.e.encode(null).getSizeBits();
		return Ibits+Ebits;
	}
	//size of stamp in byts
	public int sizeInBytes(){
		int Ibits = this.i.encode(null).getSizeBits();
		int Ebits = this.e.encode(null).getSizeBits();
		return (int)((Ibits+Ebits+4)/8);
	}
//
//	//size of stamp in bits
//	public int sizeInBits(char[] array){
//		int ilen = (array[0])/8;
//		int ilen2 = 3 + ((array[0] + array[1])/16);
//		int elen = (array[ilen2])/8;
//		return ilen+elen;
//	}
	
	public void decode(BitArray bt){
		this.e.decode(bt);
	}
	
	public void dDecode(char[] array){ // tamanhos completos, incluindo metadados (ub, fb, sb)
		int ilen = 3 + ((array[0] + array[1])/16);
		int elen = 3 + ((array[ilen] + array[ilen + 1])/16);
		char[] icode = new char[ilen];
		char[] ecode = new char[elen];
		
		System.arraycopy(array, 0, icode, 0, ilen);
		System.arraycopy(array, ilen, ecode, 0, elen);
		
		BitArray bi = new BitArray(icode);
		BitArray be = new BitArray(ecode);
		
		this.i.decode(bi);
		this.e.decode(be);
	}
	
	// Sets e gets e outros usuais
	public Event getEvent(){ return this.e; } // estes nao colonados quando retornados
	public Id getId(){ return this.i; } // estes nao colonados quando retornados
		
	public void setEvent(Event e){ this.e = e; }
	public void setId(Id i){ this.i = i; }
		
	public String tostring(){
		//return new String("( " + i.tostring() + ",  )");
		//return new String("( , " + e.tostring()+ " )");
		return new String("( " + i.tostring() + ", " + e.tostring()+ " )");
	}
}
