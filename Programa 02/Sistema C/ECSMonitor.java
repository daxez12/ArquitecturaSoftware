/******************************************************************************************************************
* File:ECSMonitor.java
* Course: 17655
* Project: Assignment A3
* Copyright: Copyright (c) 2009 Carnegie Mellon University
* Versions:
*	1.0 March 2009 - Initial rewrite of original assignment 3 (ajl).
*
* Description:
*
* This class monitors the environmental control systems that control museum temperature and humidity. In addition to
* monitoring the temperature and humidity, the ECSMonitor also allows a user to set the humidity and temperature
* ranges to be maintained. If temperatures exceed those limits over/under alarm indicators are triggered.
*
* Parameters: IP address of the event manager (on command line). If blank, it is assumed that the event manager is
* on the local machine.
*
* Internal Methods:
*	static private void Heater(EventManagerInterface ei, boolean ON )
*	static private void Chiller(EventManagerInterface ei, boolean ON )
*	static private void Humidifier(EventManagerInterface ei, boolean ON )
*	static private void Dehumidifier(EventManagerInterface ei, boolean ON )
*
******************************************************************************************************************/
import InstrumentationPackage.*;
import EventPackage.*;
import java.util.*;

class ECSMonitor extends Thread
{
	private EventManagerInterface em = null;// Interface object to the event manager
	private String EvtMgrIP = null;			// Event Manager IP address
	private float TempRangeHigh = 100;		// These parameters signify the temperature and humidity ranges in terms
	private float TempRangeLow = 0;			// of high value and low values. The ECSmonitor will attempt to maintain
	private float HumiRangeHigh = 100;		// this temperature and humidity. Temperatures are in degrees Fahrenheit
	private float HumiRangeLow = 0;			// and humidity is in relative humidity percentage.
	boolean Registered = true;				// Signifies that this class is registered with an event manager.
	MessageWindow mw = null;				// This is the message window
	Indicator ti;							// Temperature indicator
	Indicator hi;							// Humidity indicator
	Indicator indicadorPuerta;
	Indicator indicadorVentana;
	Indicator indicadorMovimiento;
	Indicator indicadorFuego;
	boolean fire = false;
	boolean userResponse = false;			//Determina si el usuario fue quien apagó/encendió el rociador

	public ECSMonitor()
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
			System.out.println("ECSMonitor::Error instantiating event manager interface: " + e);
			Registered = false;

		} // catch

	} //Constructor

	public ECSMonitor( String EvmIpAddress )
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
			System.out.println("ECSMonitor::Error instantiating event manager interface: " + e);
			Registered = false;

		} // catch

	} // Constructor

	public void run()
	{
		Event Evt = null;				// Event object
		EventQueue eq = null;			// Message Queue
		int EvtId = 0;					// User specified event ID
		float CurrentTemperature = 0;	// Current temperature as reported by the temperature sensor
		float CurrentHumidity= 0;		// Current relative humidity as reported by the humidity sensor
		int	Delay = 1000;				// The loop delay (1 second)
		boolean Done = false;			// Loop termination flag
		boolean ON = true;				// Used to turn on heaters, chillers, humidifiers, and dehumidifiers
		boolean OFF = false;			// Used to turn off heaters, chillers, humidifiers, and dehumidifiers
		boolean alarmaPuerta = false;
		boolean alarmaVentana = false;
		boolean alarmaMovimiento = false;
		
		if (em != null)
		{
			// Now we create the ECS status and message panel
			// Note that we set up two indicators that are initially yellow. This is
			// because we do not know if the temperature/humidity is high/low.
			// This panel is placed in the upper left hand corner and the status 
			// indicators are placed directly to the right, one on top of the other

			mw = new MessageWindow("ECS Monitoring Console", 0, 0);
			ti = new Indicator ("TEMP UNK", mw.GetX()+ mw.Width(), 0);
			hi = new Indicator ("HUMI UNK", mw.GetX()+ mw.Width(),  ti.Height() * 1, 2 );
			indicadorPuerta = new Indicator ("Puerta - OFF", mw.GetX() + mw.Width(),  ti.Height() * 2);
			indicadorVentana = new Indicator ("Ventana - OFF", mw.GetX()+ mw.Width(), ti.Height() * 3);
			indicadorMovimiento = new Indicator ("Movimiento - OFF", mw.GetX() + mw.Width(), ti.Height()*4);
			indicadorFuego = new Indicator("Fuego: NO", mw.GetX() + mw.Width(), ti.Height() * 5);
			mw.WriteMessage( "Registered with the event manager." );

	    	try
	    	{
				mw.WriteMessage("   Participant id: " + em.GetMyId() );
				mw.WriteMessage("   Registration Time: " + em.GetRegistrationTime() );

			} // try

	    	catch (Exception e)
			{
				System.out.println("Error:: " + e);

			} // catch

			KeepAlive ka = new KeepAlive(em, 1000, "ECSMonitor", "Monitor del sistema");
			ka.start();

			/********************************************************************
			** Here we start the main simulation loop
			*********************************************************************/

			while ( !Done )
			{
				// Here we get our event queue from the event manager

				try
				{
					eq = em.GetEventQueue();

				} // try

				catch( Exception e )
				{
					mw.WriteMessage("Error getting event queue::" + e );

				} // catch

				// If there are messages in the queue, we read through them.
				// We are looking for EventIDs = 1 or 2. Event IDs of 1 are temperature
				// readings from the temperature sensor; event IDs of 2 are humidity sensor
				// readings. Note that we get all the messages at once... there is a 1
				// second delay between samples,.. so the assumption is that there should
				// only be a message at most. If there are more, it is the last message
				// that will effect the status of the temperature and humidity controllers
				// as it would in reality.

				int qlen = eq.GetSize();

				for ( int i = 0; i < qlen; i++ )
				{
					Evt = eq.GetEvent();

					if ( Evt.GetEventId() == 1 ) // Temperature reading
					{
						try
						{
							CurrentTemperature = Float.valueOf(Evt.GetMessage()).floatValue();

						} // try

						catch( Exception e )
						{
							mw.WriteMessage("Error reading temperature: " + e);

						} // catch

					} // if

					if ( Evt.GetEventId() == 2 ) // Humidity reading
					{
						try
						{
				
							CurrentHumidity = Float.valueOf(Evt.GetMessage()).floatValue();

						} // try

						catch( Exception e )
						{
							mw.WriteMessage("Error reading humidity: " + e);

						} // catch

					} // if


					if (Evt.GetEventId() == 3 ){ //Lector de alarmas de seguridad
						String device = Evt.GetMessage();
						if (device.equalsIgnoreCase("P0")){
							alarmaPuerta = false;
							mw.WriteMessage("Evento de Alarma Puerta OFF recibido" );
						}
						if (device.equalsIgnoreCase("P1")){
							alarmaPuerta = true;
							mw.WriteMessage("Evento de Alarma Puerta ON recibido" );
						}
						if (device.equalsIgnoreCase("V0")){
							alarmaVentana = false;
							mw.WriteMessage("Evento de Alarma Ventana OFF recibido" );
						}
						if (device.equalsIgnoreCase("V1")){
							alarmaVentana = true;
							mw.WriteMessage("Evento de Alarma Ventana ON recibido" );
						}
						if (device.equalsIgnoreCase("M0")){
							alarmaMovimiento = false;
							mw.WriteMessage("Evento de Alarma Movimiento OFF recibido" );
						}
						if (device.equalsIgnoreCase("M1")){
							alarmaMovimiento = true;
							mw.WriteMessage("Evento de Alarma Movimiento ON recibido" );
						}
					}

					if (Evt.GetEventId() == 7){ //Fire reading
						String msg = Evt.GetMessage();
						if (msg.equalsIgnoreCase("F1")){
							mw.WriteMessage("Evento de presencia de fuego recibido");
							userResponse = false;
							planificarRociador();
							fire = true;
						}
						else{
							mw.WriteMessage("Evento de ausencia de fuego recibido");
							fire = false;
						}
					}

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

						hi.dispose();
						ti.dispose();
						indicadorPuerta.dispose();
						indicadorVentana.dispose();
						indicadorMovimiento.dispose();
						indicadorFuego.dispose();

					} // if

				} // for

				mw.WriteMessage("Temperature:: " + CurrentTemperature + "F  Humidity:: " + CurrentHumidity );

				// Check temperature and effect control as necessary

				if (CurrentTemperature < TempRangeLow) // temperature is below threshhold
				{
					ti.SetLampColorAndMessage("TEMP LOW", 3);
					Heater(ON);
					Chiller(OFF);

				} else {

					if (CurrentTemperature > TempRangeHigh) // temperature is above threshhold
					{
						ti.SetLampColorAndMessage("TEMP HIGH", 3);
						Heater(OFF);
						Chiller(ON);

					} else {

						ti.SetLampColorAndMessage("TEMP OK", 1); // temperature is within threshhold
						Heater(OFF);
						Chiller(OFF);

					} // if
				} // if

				// Check humidity and effect control as necessary

				if (CurrentHumidity < HumiRangeLow)
				{
					hi.SetLampColorAndMessage("HUMI LOW", 3); // humidity is below threshhold
					Humidifier(ON);
					Dehumidifier(OFF);

				} else {

					if (CurrentHumidity > HumiRangeHigh) // humidity is above threshhold
					{
						hi.SetLampColorAndMessage("HUMI HIGH", 3);
						Humidifier(OFF);
						Dehumidifier(ON);

					} else {

						hi.SetLampColorAndMessage("HUMI OK", 1); // humidity is within threshhold
						Humidifier(OFF);
						Dehumidifier(OFF);

					} // if

				} // if

				if (alarmaPuerta){
					indicadorPuerta.SetLampColorAndMessage("Puerta - ON", 1);
				}
				else{
					indicadorPuerta.SetLampColorAndMessage("Puerta - OFF", 0);
				}

				if (alarmaVentana){
					indicadorVentana.SetLampColorAndMessage("Ventana - ON", 1);
				}
				else{
					indicadorVentana.SetLampColorAndMessage("Ventana - OFF", 0);
				}

				if (alarmaMovimiento){
					indicadorMovimiento.SetLampColorAndMessage("Movimiento - ON", 1);
				}
				else{
					indicadorMovimiento.SetLampColorAndMessage("Movimiento - OFF", 0);
				}

				if (fire){
					indicadorFuego.SetLampColorAndMessage("Fuego: SI", 3);

				}
				else{
					indicadorFuego.SetLampColorAndMessage("Fuego: NO", 0);
				}

				// This delay slows down the sample rate to Delay milliseconds

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
	* CONCRETE METHOD:: IsRegistered
	* Purpose: This method returns the registered status
	*
	* Arguments: none
	*
	* Returns: boolean true if registered, false if not registered
	*
	* Exceptions: None
	*
	***************************************************************************/

	public boolean IsRegistered()
	{
		return( Registered );

	} // SetTemperatureRange

	/***************************************************************************
	* CONCRETE METHOD:: SetTemperatureRange
	* Purpose: This method sets the temperature range
	*
	* Arguments: float lowtemp - low temperature range
	*			 float hightemp - high temperature range
	*
	* Returns: none
	*
	* Exceptions: None
	*
	***************************************************************************/

	public void SetTemperatureRange(float lowtemp, float hightemp )
	{
		TempRangeHigh = hightemp;
		TempRangeLow = lowtemp;
		mw.WriteMessage( "***Temperature range changed to::" + TempRangeLow + "F - " + TempRangeHigh +"F***" );

	} // SetTemperatureRange
	private class Planificador extends Thread{
		private int mDelay;
		public Planificador(int delay){
			mDelay = delay;
		}
		public void run(){
			try{
				Thread.sleep(mDelay);
				if (fire && userResponse == false){
					EnableRociador(true);
					mw.WriteMessage("Solicitud para encender rociador enviada automaticamente");
				}
			}
			catch(Exception e){
				System.err.println("Error al dormir el hilo planificador");
			}
		}
	}
	public void planificarRociador(){
		Planificador p = new Planificador(15000);
		p.start();
	}

	public void EnableRociador(boolean value){
		Event evt;
		userResponse = true;
		if ( value )
		{
			evt = new Event( (int) 8, "F1" );

		} else {

			evt = new Event( (int) 8, "F0" );

		} // if

		// Here we send the event to the event manager.

		try
		{
			em.SendEvent( evt );

		} // try

		catch (Exception e)
		{
			System.out.println("Error sending fire control message::  " + e);

		} // catch
	}

	public boolean IsPresentFire(){
		return fire;
	}
	public void EnableSecurityAlarms(boolean value){
		
		if (value){
			mw.WriteMessage("***El subsistema de alarmas se ha activado***");
			
		}
		else{
			mw.WriteMessage("***El subistema de alarmas se ha desactivado***");
		}

		Event evt = new Event(6, value?"S1":"S0");
		try
		{
			em.SendEvent( evt );

		} // try

		catch (Exception e)
		{
			System.out.println("Error sending security alarms message:: " + e);

		} // catch
	
	}
	/***************************************************************************
	* CONCRETE METHOD:: SetHumidityRange
	* Purpose: This method sets the humidity range
	*
	* Arguments: float lowhimi - low humidity range
	*			 float highhumi - high humidity range
	*
	* Returns: none
	*
	* Exceptions: None
	*
	***************************************************************************/

	public void SetHumidityRange(float lowhumi, float highhumi )
	{
		HumiRangeHigh = highhumi;
		HumiRangeLow = lowhumi;
		mw.WriteMessage( "***Humidity range changed to::" + HumiRangeLow + "% - " + HumiRangeHigh +"%***" );

	} // SetTemperatureRange

	/***************************************************************************
	* CONCRETE METHOD:: Halt
	* Purpose: This method posts an event that stops the environmental control
	*		   system.
	*
	* Arguments: none
	*
	* Returns: none
	*
	* Exceptions: Posting to event manager exception
	*
	***************************************************************************/

	public void Halt()
	{
		mw.WriteMessage( "***HALT MESSAGE RECEIVED - SHUTTING DOWN SYSTEM***" );

		// Here we create the stop event.

		Event evt;

		evt = new Event( (int) 99, "XXX" );

		// Here we send the event to the event manager.

		try
		{
			em.SendEvent( evt );

		} // try

		catch (Exception e)
		{
			System.out.println("Error sending halt message:: " + e);

		} // catch

	} // Halt

	/***************************************************************************
	* CONCRETE METHOD:: Heater
	* Purpose: This method posts events that will signal the temperature
	*		   controller to turn on/off the heater
	*
	* Arguments: boolean ON(true)/OFF(false) - indicates whether to turn the
	*			 heater on or off.
	*
	* Returns: none
	*
	* Exceptions: Posting to event manager exception
	*
	***************************************************************************/

	private void Heater( boolean ON )
	{
		// Here we create the event.

		Event evt;

		if ( ON )
		{
			evt = new Event( (int) 5, "H1" );

		} else {

			evt = new Event( (int) 5, "H0" );

		} // if

		// Here we send the event to the event manager.

		try
		{
			em.SendEvent( evt );

		} // try

		catch (Exception e)
		{
			System.out.println("Error sending heater control message:: " + e);

		} // catch

	} // Heater

	/***************************************************************************
	* CONCRETE METHOD:: Chiller
	* Purpose: This method posts events that will signal the temperature
	*		   controller to turn on/off the chiller
	*
	* Arguments: boolean ON(true)/OFF(false) - indicates whether to turn the
	*			 chiller on or off.
	*
	* Returns: none
	*
	* Exceptions: Posting to event manager exception
	*
	***************************************************************************/

	private void Chiller( boolean ON )
	{
		// Here we create the event.

		Event evt;

		if ( ON )
		{
			evt = new Event( (int) 5, "C1" );

		} else {

			evt = new Event( (int) 5, "C0" );

		} // if

		// Here we send the event to the event manager.

		try
		{
			em.SendEvent( evt );

		} // try

		catch (Exception e)
		{
			System.out.println("Error sending chiller control message:: " + e);

		} // catch

	} // Chiller

	/***************************************************************************
	* CONCRETE METHOD:: Humidifier
	* Purpose: This method posts events that will signal the humidity
	*		   controller to turn on/off the humidifier
	*
	* Arguments: boolean ON(true)/OFF(false) - indicates whether to turn the
	*			 humidifier on or off.
	*
	* Returns: none
	*
	* Exceptions: Posting to event manager exception
	*
	***************************************************************************/

	private void Humidifier( boolean ON )
	{
		// Here we create the event.

		Event evt;

		if ( ON )
		{
			evt = new Event( (int) 4, "H1" );

		} else {

			evt = new Event( (int) 4, "H0" );

		} // if

		// Here we send the event to the event manager.

		try
		{
			em.SendEvent( evt );

		} // try

		catch (Exception e)
		{
			System.out.println("Error sending humidifier control message::  " + e);

		} // catch

	} // Humidifier

	/***************************************************************************
	* CONCRETE METHOD:: Deumidifier
	* Purpose: This method posts events that will signal the humidity
	*		   controller to turn on/off the dehumidifier
	*
	* Arguments: boolean ON(true)/OFF(false) - indicates whether to turn the
	*			 dehumidifier on or off.
	*
	* Returns: none
	*
	* Exceptions: Posting to event manager exception
	*
	***************************************************************************/

	private void Dehumidifier( boolean ON )
	{
		// Here we create the event.

		Event evt;

		if ( ON )
		{
			evt = new Event( (int) 4, "D1" );

		} else {

			evt = new Event( (int) 4, "D0" );

		} // if

		// Here we send the event to the event manager.

		try
		{
			em.SendEvent( evt );

		} // try

		catch (Exception e)
		{
			System.out.println("Error sending dehumidifier control message::  " + e);

		} // catch

	} // Dehumidifier

} // ECSMonitor