import EventPackage.*;

public class KeepAlive extends Thread{
	private EventManagerInterface em;
	private int delay;
	private String name;
	private String description;
	private boolean done = false;
	public void finish(){
		done = true;
	}
	public KeepAlive(EventManagerInterface em, int delay, String name, String description){
		this.em = em;
		this.delay = delay;
		this.name = name;
		this.description = description;
	}

	public void run(){
		while(!done){
			Event evt = new Event(9, name + "|" + description + "|Funcionando" );
			try{
				em.SendEvent(evt);
				Thread.sleep(delay);
			}
			catch(Exception e){
				System.err.println(e.getMessage());
			}
		}
	}

}