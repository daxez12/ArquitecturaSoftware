import InstrumentationPackage.*;
import EventPackage.*;
import java.util.*;
import TermioPackage.*;

public class FireSensor{
	public static void main(String args[]){
		EventManagerInterface em = null;// Interface object to the event manager
		boolean done = false;			// Loop termination flag
		Termio input = new Termio();
		if ( args.length == 0 ){
			System.out.println("\n\nAttempting to register on the local machine..." );
			try	{
				em = new EventManagerInterface();
			}
			catch (Exception e){
				System.out.println("Error instantiating event manager interface: " + e);
			} 
		} 
		else{
			System.out.println("\n\nAttempting to register on the machine:: " + args[0] );
			try{
				em = new EventManagerInterface( args[0] );
			}

			catch (Exception e){
				System.out.println("Error instantiating event manager interface: " + e);
			} 
		}

		while(!done){

			System.out.println("¿Que evento desea simular?");
			System.out.println("1) Hay fuego");
			System.out.println("2) No hay fuego");
			String option = input.KeyboardReadString();
			//Solo leemos el evento 99 para salir
			try	{
				EventQueue eq = em.GetEventQueue();
				while(eq.GetSize()>0){
					Event evt = eq.GetEvent();
					if ( evt.GetEventId() == 99 ){
						done = true;
						break;
					}
				}
			}
			catch( Exception e ) {
				System.err.println("Error getting event queue::" + e );
			}

			if (option.equals("1")){
				postFire(em, "F1");
			}
			if (option.equals("2")){
				postFire(em, "F0");
			}
		}
	}


	static private void postFire(EventManagerInterface ei,  String msg )
	{
		// Here we create the event.

		Event evt = new Event( 7, msg );

		// Here we send the event to the event manager.

		try
		{
			ei.SendEvent( evt );
			//System.out.println( "Sent Temp Event" );			

		} // try

		catch (Exception e)
		{
			System.out.println( "Error Posting Alarm:: " + e );

		} // catch

	} // PostTemperature
}