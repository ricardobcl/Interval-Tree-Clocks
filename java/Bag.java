import java.util.ArrayList;

public class Bag<T>{
	private ArrayList<T> list;
	
	public Bag(){
		this.list = new ArrayList<T>();
	}
	
	public void push(T s){
		this.list.add(s);
	}
	
	public T pop(){
		Dice d = new Dice();
		int ind = d.iroll(0, this.list.size()-1);
		
		return this.list.remove(ind);
	}
	
	public T popInd(int i){
		//if (i < this.list.size()){
			return this.list.remove(i);
		//}
	}
	
	public T getInd(int i){
		//if (i < this.list.size()){
			return this.list.get(i);
		//}
	}
	
	public T getLast(){
		int ind = this.list.size() - 1;
		// if( ind > 0){
			return this.list.get(ind);
		//}
	}
	
	public int getValidIndice(){
		Dice d = new Dice();
		return d.iroll(0, this.list.size()-1);
	}
	
	public int getSize(){
		return this.list.size();
	}

	public ArrayList<T> getList() {
		return this.list;
	}
}