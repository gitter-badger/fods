package org.jsoftware.fods.client.ext;



/**
 * Database selection strategy.
 * @author szalik
 */
public interface Selector {

	/**
	 * @param fodsState current fods state
	 * @return name of database that should be used
	 */
	String select(FodsState fodsState);

}
