import InstrumentationPackage.*;
import EventPackage.*;
import java.util.*;

class FireController
{
	public static void main(String args[])
	{
		String EvtMgrIP;				// Event Manager IP address
		Event Evt = null;				// Event object
		EventQueue eq = null;			// Message Queue
		int EvtId = 0;					// User specified event ID
		EventManagerInterface em = null;// Interface object to the event manager
		boolean RociadorState = false;	// Rociador state: false == off, true == on
		int	Delay = 2500;				// The loop delay (2.5 seconds)
		boolean Done = false;			// Loop termination flag

		/////////////////////////////////////////////////////////////////////////////////
		// Get the IP address of the event manager
		/////////////////////////////////////////////////////////////////////////////////

 		if ( args.length == 0 )
 		{
			// event manager is on the local system

			System.out.println("\n\nAttempting to register on the local machine..." );

			try
			{
				// Here we create an event manager interface object. This assumes
				// that the event manager is on the local machine

				em = new EventManagerInterface();
			}

			catch (Exception e)
			{
				System.out.println("Error instantiating event manager interface: " + e);

			} // catch

		} else {

			// event manager is not on the local system

			EvtMgrIP = args[0];

			System.out.println("\n\nAttempting to register on the machine:: " + EvtMgrIP );

			try
			{
				// Here we create an event manager interface object. This assumes
				// that the event manager is NOT on the local machine

				em = new EventManagerInterface( EvtMgrIP );
			}

			catch (Exception e)
			{
				System.out.println("Error instantiating event manager interface: " + e);

			} // catch

		} // if

		// Here we check to see if registration worked. If ef is null then the
		// event manager interface was not properly created.

		if (em != null)
		{
			System.out.println("Registered with the event manager." );

			/* Now we create the temperature control status and message panel
			** We put this panel about 1/3 the way down the terminal, aligned to the left
			** of the terminal. The status indicators are placed directly under this panel
			*/

			float WinPosX = 0.0f; 	//This is the X position of the message window in terms 
								 	//of a percentage of the screen height
			float WinPosY = 0.3f; 	//This is the Y position of the message window in terms 
								 	//of a percentage of the screen height 
			
			MessageWindow mw = new MessageWindow("Fire Controller Status Console", WinPosX, WinPosY);
			
			// Put the status indicators under the panel...
			
			Indicator ri = new Indicator ("Rociador OFF", mw.GetX(), mw.GetY()+mw.Height());
			
			mw.WriteMessage("Registered with the event manager." );

	    	try
	    	{
				mw.WriteMessage("   Participant id: " + em.GetMyId() );
				mw.WriteMessage("   Registration Time: " + em.GetRegistrationTime() );

			} // try

	    	catch (Exception e)
			{
				System.out.println("Error:: " + e);

			} // catch

			KeepAlive ka = new KeepAlive(em, 1000, "FireController", "Controlador del rociador del agua");
			ka.start();

			/********************************************************************
			** Here we start the main simulation loop
			*********************************************************************/

			while ( !Done )
			{

				try
				{
					eq = em.GetEventQueue();

				} // try

				catch( Exception e )
				{
					mw.WriteMessage("Error getting event queue::" + e );

				} // catch

				// If there are messages in the queue, we read through them.
				// We are looking for EventIDs = 8, this is a request to turn the
				// rociador on.

				int qlen = eq.GetSize();

				for ( int i = 0; i < qlen; i++ )
				{
					Evt = eq.GetEvent();

					if ( Evt.GetEventId() == 8 )
					{
						if (Evt.GetMessage().equalsIgnoreCase("F1")) // Rociador on
						{
							RociadorState = true;
							mw.WriteMessage("Received Rociador on event" );

						} // if

						if (Evt.GetMessage().equalsIgnoreCase("F0")) // heater off
						{
							RociadorState = false;
							mw.WriteMessage("Received Rociador off event" );

						} // if

					} // if

					// If the event ID == 99 then this is a signal that the simulation
					// is to end. At this point, the loop termination flag is set to
					// true and this process unregisters from the event manager.

					if ( Evt.GetEventId() == 99 )
					{
						Done = true;

						try
						{
							em.UnRegister();

				    	} // try

				    	catch (Exception e)
				    	{
							mw.WriteMessage("Error unregistering: " + e);

				    	} // catch

				    	mw.WriteMessage( "\n\nSimulation Stopped. \n");

						// Get rid of the indicators. The message panel is left for the
						// user to exit so they can see the last message posted.

						ri.dispose();

					} // if

				} // for

				// Update the lamp status

				if (RociadorState)
				{
					// Set to green, rociador is on

					ri.SetLampColorAndMessage("ROCIADOR ON", 1);

				} else {

					// Set to black, heater is off
					ri.SetLampColorAndMessage("ROCIADOR OFF", 0);

				} // if

				

				try
				{
					Thread.sleep( Delay );

				} // try

				catch( Exception e )
				{
					System.out.println( "Sleep error:: " + e );

				} // catch

			} // while
			ka.finish();

		} else {

			System.out.println("Unable to register with the event manager.\n\n" );

		} // if

	} // main

	/***************************************************************************
	* CONCRETE METHOD:: ConfirmMessage
	* Purpose: This method posts the specified message to the specified event
	* manager. This method assumes an event ID of -5 which indicates a confirma-
	* tion of a command.
	*
	* Arguments: EventManagerInterface ei - this is the eventmanger interface
	*			 where the event will be posted.
	*
	*			 string m - this is the received command.
	*
	* Returns: none
	*
	* Exceptions: None
	*
	***************************************************************************/

	

} // TemperatureController