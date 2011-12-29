package causal_histories;



public class Atomo{
	public int id;
	
	public Atomo(int i){
		this.id = i;
	}
	
	public int getId(){
		return this.id;
	}
	
	public String tostring(){
		String res = new String();
		res = res + this.id;
		return res;
	}
}