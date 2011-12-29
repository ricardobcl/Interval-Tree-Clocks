package causal_histories;

import java.util.ArrayList;

public class CStamp{
	ArrayList<Atomo> lista;
	
	public CStamp(){
		this.lista = new ArrayList<Atomo>();
	}
	
	public CStamp(CStamp cs2){
		this.lista = new ArrayList<Atomo>(cs2.getLista());
	}
	
	// Métodos de instancia
	public void seed(Atomo a){
		this.lista.clear();
		this.lista.add(a);
	}
	
	public CStamp fork(){
		return this.copia();
	}
	
	public void join(CStamp cs1, CStamp cs2){
		this.lista.addAll(cs1.getLista());
		
		for(Atomo aa : cs2.getLista()){
			if (! this.lista.contains(aa))
				this.lista.add(aa);
		}
	}
	
	public void event(Atomo a){
		this.lista.add(a);
	}
	
	public boolean leq(CStamp s2){
		for(Atomo aa : this.lista){
			if (!s2.getLista().contains(aa)){
				return false;
			}
		}
		return true;
	}
	
	// Metodos complementares
	public ArrayList<Atomo> getLista(){
		ArrayList<Atomo> res = new ArrayList<Atomo>();
		res.addAll(this.lista);
		
		return res;
	}
	
	public void setLista(ArrayList<Atomo> a){
		this.lista.clear();
		this.lista.addAll(a);
	}
	public void addLista(ArrayList<Atomo> a){
		this.lista.addAll(a);
	}
	
	public CStamp copia(){
		CStamp res = new CStamp();
		res.setLista(this.lista);
		
		return res;
	}
	
	public String tostring(){
		String res = new String();
		
		for(Atomo aa : this.lista){
			res = res + aa.tostring() + " ";
		}
		
		return res;
	}
}