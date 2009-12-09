package causal_histories;



public class Gerador{
	private int indice;
	
	public Gerador(){
		this.indice = 1;
	}
	
	public void reset(){
		this.indice = 1;
	}
	
	public Atomo seed(){
		this.reset();
		return this.gera();
	}
	
	public Atomo gera(){
		return new Atomo(this.indice++);
	}
}