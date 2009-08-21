package pl.eo.apps.bossa.fods.client.ext;



public interface Selector {

	void init(DatabasesState state);

	int select();	

	void onStateChanged();

	void forceSetIndex(int index) throws UnsupportedOperationException;
}
