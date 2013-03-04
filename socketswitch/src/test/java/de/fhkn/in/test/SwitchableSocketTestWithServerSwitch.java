package de.fhkn.in.test;


/**
 * Does the same as superclass {@link de.htwg_konstanz.in.switchable.tests.SwitchableSocketTest}
 * but with the difference that the server which is used is successive listening on two ServerSockets which different Ports
 * so a different ControllInstance {@link de.htwg_konstanz.in.switchable.tests.ControlInstanceServerSwitching} is needed
 *
 *
 * @author Steven B�ckle
 *
 */
public class SwitchableSocketTestWithServerSwitch extends SwitchableSocketTest{
	
	public static ControlInstanceServerSwitching control;
	@Override
	public void startControllInstance(){
		control = new ControlInstanceServerSwitching(
				switchableSocket);
		controlThread = new Thread(control);
		controlThread.start();
	}
	@Override
	public void shutDownControllInstance() {
		control.finished = true;
		controlThread.interrupt();
	}
	
}
