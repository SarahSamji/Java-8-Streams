/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package homework2;

/**
 *
 * @author Sarah
 */


import java.util.concurrent.locks.ReentrantLock;
import java.util.Random;

						//REMOVE COMMENTS FROM START_SEARCH(), START_INSERT() AND START_REMOVE() METHODS TO SEE VALUE OF nd, ni, ns

public class ConcurrentSearcherList<T> extends Thread{
	

	private static class Node<T>{
		final T item;
		Node<T> next;
		
		Node(T item, Node<T> next){
			this.item = item;
			this.next = next;
		}
	}
	

	private volatile Node<T> first; 
	
	private  volatile int ni=0;
	private volatile int nd=0;
	private  volatile int ns=0;
												//VARIABLES FOR TEST PURPOSES
	/*static int i;	 
	volatile static boolean flag;
	static ConcurrentSearcherList<Integer> ob= new ConcurrentSearcherList<Integer>();
		 
	volatile int c=0;
	static  Thread t1,t2,t3,cur;
	volatile static Random ran=new Random();
	private final ReentrantLock lock = new ReentrantLock();	
*/
	
	public ConcurrentSearcherList() {
		first = null;		
		
	}
	
	public void insert(T item) throws InterruptedException{
		assert item != null: "Error in ConcurrentSearcherList insert:  Attempt to insert null";
		start_insert();
		try{
			first = new Node<T>(item, first); //System.out.println("\nItem created: "+ item);
		}
		finally{
			end_insert();
		}
	}
		
	public boolean search(T item) throws InterruptedException{
		assert item != null: "Error in ConcurrentSearcherList insert:  Attempt to search for null";
		start_search();
		try{
			
			for(Node<T> curr = first;  curr != null ; curr = curr.next){
				if (item.equals(curr.item)) 
						return true;
			}
			
			return false;
		}
		finally{
			end_search();
		}
	}
	
	
	public boolean remove(T item) throws InterruptedException{
		assert item != null: "Error in ConcurrentSearcherList insert:  Attempt to remove null";
		start_remove();
		try{
			if(first == null) return false;
			if (item.equals(first.item)){first = first.next; return true;}
			for(Node<T> curr = first;  curr.next != null ; curr = curr.next){
				if (item.equals(curr.next.item)) {
					curr.next = curr.next.next;
					 //System.out.println("\nItem removed:"+item ); 
					 return true;
				}
			}
			//System.out.println("\nItem not removed:"+item);	

			return false;			
		}
		finally{
			end_remove();
		}
	}
		
	private synchronized void start_insert() throws InterruptedException{		
		
		while(ni==1 || nd==1)				//insert exclusive of removes and other inserters
		{
			wait();
		}
		ni++;
			//cur=Thread.currentThread();
			//System.out.println("ni:"+ni+ " "+ "ns:"+ns+ " "+ "nd:" + nd+ " " + cur.getName());
	}

	private synchronized void end_insert(){
		
		while(ni>0)						//Satisfies invariant that insertors always greater than or equal to 0
			ni--; 
			notifyAll();
			
	} 
	
	private synchronized void start_search() throws InterruptedException{
		while(nd==1)					// search can occur with insert but not with remove
		{
			wait();
		}
		ns++;
			//cur=Thread.currentThread();
			//System.out.println("ni:"+ni+ " "+ "ns:"+ns+ " "+ "nd:" + nd+ " " + cur.getName());
	}
	
	private synchronized void end_search(){
		while(ns>0)							//Satisfies invariant that searchers always greater than or equal to 0
			ns--;
		if(ns==0)
			notifyAll();

	}
	
	private synchronized void start_remove() throws InterruptedException{
		while(ni==1 || ns>0 || nd==1) 			// remove is exclusive of inserts and search
				wait();
		nd++;
			//cur=Thread.currentThread();
			//System.out.println("ni:"+ni+ " "+ "ns:"+ns+ " "+ "nd:" + nd+ " " + cur.getName());
	}
	
	private  synchronized void end_remove() {
		while(nd>0)							//Satisfies invariant that removers always greater than or equal to 0
			nd--;	
		if(nd==0)
		notifyAll();
		
	}


@Override
public void run() 
{
	lock.lock();
	try
	{  cur=Thread.currentThread();
	//if(cur.getName()=="Thread1" || cur.getName()=="Thread3")
	  for(i=1;i<=3;i++)
		
		{
			c++;
		
		ob.insert(c);
		ob.insert(++c);
		if(ob.remove(ran.nextInt(c)))
			System.out.println("\nItem removed:"); //+ " "+ cur.getName());
		else
			System.out.println("\nItem not removed:"); //+ " "+ cur.getName());	
			 
		ob.insert(++c);
		if(ob.search(ran.nextInt(c)))
			System.out.println("\nItem found:"); // + " ") + cur.getName());
		else
			System.out.println("\nItem not found:");  //+ " ") + cur.getName());	

		if(ob.remove(ran.nextInt(c)))
			System.out.println("\nItem removed:");  //+ " "+ cur.getName());
		else
			System.out.println("\nItem not removed:");  //+ " "+ cur.getName());	
		}	
		
	
	}

	
	catch (Exception e)
	{
		System.out.println(" ");
	}
	finally
	{
		lock.unlock();
	}
}


public static void main(String args[]) throws InterruptedException
{
	//long startTime = System.nanoTime();
	
	 t1=new Thread(ob,"Thread1");		
	 t2=new Thread(ob,"Thread2");
	 t3=new Thread(ob,"Thread3");
	 t1.start();
	 t2.start();
	 t3.start();
	 t1.join();
	 t2.join();
	 t3.join();	
	//long endTime = System.nanoTime();
	//System.out.println("Took "+(endTime - startTime) + " ns"); 
}
}

