package helpers;

public class Pair<T>{
	private T a;
	private T b;
	
	public Pair(){
	}
	
	public Pair(T a, T b){
		this.a = a;
		this.b = b;
	}
	
	// Metodos de instancia
	// gets e sets
	public void setEa(T a){
		this.a = a;
	}
	public void setEb(T b){
		this.b = b;
	}
	public T getEa(){
		return this.a;
	}
	public T getEb(){
		return this.b;
	}
}