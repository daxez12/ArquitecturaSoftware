import InstrumentationPackage.*;
import EventPackage.*;
import java.util.*;
import TermioPackage.*;

public class SecuritySensor{
	public static void main(String args[]){
		EventManagerInterface em = null;// Interface object to the event manager
		boolean done = false;			// Loop termination flag
		Termio input = new Termio();
		boolean report = true;
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
			System.out.println("1) Encender Alarma Ventana");
			System.out.println("2) Apagar Alarma Ventana");
			System.out.println("3) Encender Alarma Puerta");
			System.out.println("4) Apagar Alarma Puerta");
			System.out.println("5) Encender Alarma Movimiento");
			System.out.println("6) Apagar Alarma Movimiento");
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
					else if (evt.GetEventId() == 6){
						if (evt.GetMessage().equals("S1")){
							report = true;
						}
						else{
							report = false;
						}
					}
				}
			}
			catch( Exception e ) {
				System.err.println("Error getting event queue::" + e );
			}

			if (report){
				if (option.equals("1")){
					postAlarm(em, "V1");
				}
				if (option.equals("2")){
					postAlarm(em, "V0");
				}
				if (option.equals("3")){
					postAlarm(em, "P1");
				}
				if (option.equals("4")){
					postAlarm(em, "P0");
				}
				if (option.equals("5")){
					postAlarm(em, "M1");
				}
				if (option.equals("6")){
					postAlarm(em, "M0");
				}
			}
		}
	}


	static private void postAlarm(EventManagerInterface ei,  String msg )
	{
		// Here we create the event.

		Event evt = new Event( 3, msg );

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