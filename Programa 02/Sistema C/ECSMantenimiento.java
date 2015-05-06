import java.util.*;
import TermioPackage.*;
import EventPackage.*;
import InstrumentationPackage.*;

public class ECSMantenimiento extends Thread{
	private EventManagerInterface em;
	private boolean Registered = true;
	private String EvtMgrIP = null;	
	private TreeMap<Long, String> saved = new TreeMap<Long, String>();
	private boolean done = false;
	private int delay = 2500;
	public ECSMantenimiento()
	{
		// event manager is on the local system
		try
		{
			// Here we create an event manager interface object. This assumes
			// that the event manager is on the local machine

			em = new EventManagerInterface();

		}

		catch (Exception e)
		{
			System.out.println("ECSMantenimiento::Error instantiating event manager interface: " + e);
			Registered = false;

		} // catch

	} //Constructor

	public ECSMantenimiento( String EvmIpAddress )
	{
		// event manager is not on the local system

		EvtMgrIP = EvmIpAddress;

		try
		{
			// Here we create an event manager interface object. This assumes
			// that the event manager is NOT on the local machine

			em = new EventManagerInterface( EvtMgrIP );

		}

		catch (Exception e)
		{
			System.out.println("ECSMantenimiento::Error instantiating event manager interface: " + e);
			Registered = false;

		} // catch
	} // Constructor
	public void finish(){
		done = true;
	}
	public void run(){
		
		ListWindow lw = new ListWindow();
		lw.setTitle("ECS Mantenimiento");
		lw.setVisible(true);
		lw.addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e){
				e.getWindow().dispose();
				ECSMantenimiento.this.finish();
			}
		});
		KeepAlive ka = new KeepAlive(em, 1000, "ECSMantenimiento","Mantenimiento de la cola de eventos");
		ka.start();
		EventQueue eq = null;
		while(!done){
			ArrayList<Long> ids = null;
			try{
				ids = em.GetRegistersIDs();
			}
			catch(Exception e){
			}
			if (ids != null){
				try	{
					eq = em.GetEventQueue();
				} // try
				catch( Exception e ){
					System.err.println("Error getting event queue::" + e );
				} // catch

				lw.clearItems();

				while(eq.GetSize() > 0 ){
					Event evt = eq.GetEvent();
					long id = evt.GetSenderId();
			
					if (evt.GetEventId() == 9){
						String value = evt.GetMessage();
						saved.put(id, value);
					}
				}

				Set<Long> keys = saved.keySet();
				for (Iterator<Long> i = keys.iterator(); i.hasNext();) {
					long key =  i.next().longValue();
					if (ids.contains(key)){
						String value = saved.get(key);
						String msg[];
						if (value == null){
							msg = new String[3];
							msg[0] = msg[1] = "No disponible";
							msg[2] = "No responde";
						}
						else{
							msg = value.split("\\|");
							String newValue = msg[0] + "|" + msg[1] + "|" + "No responde";
							saved.put(key, newValue);
						}
						lw.addItem(new String[]{key + "", msg[0], msg[1], msg[2]});
					}
				}				
			}
			else{
				System.err.println("No se pudo obtener la lista de dispositivos");
			}
			try{
				Thread.sleep(delay);
			}
			catch(Exception e){
				System.err.println("Error al dormir el hilo");
			}
				
		}
		ka.finish();
		try
		{
			em.UnRegister();

    	} // try

    	catch (Exception e)
    	{
			System.err.println("Error unregistering: " + e);

    	} // catch

	}


	public static void main(String args[]){
		ECSMantenimiento man;
		Termio input = new Termio();
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex){}

		if (args.length == 0){
			man = new ECSMantenimiento();
		}
		else{
			man = new ECSMantenimiento(args[0]);
		}
		if (man.IsRegistered()){
			man.start();
		}
		else{
			System.out.println("\n\nUnable start the mantainer.\n\n" );
		}
	}

	public boolean IsRegistered(){
		return Registered;
	}

}
