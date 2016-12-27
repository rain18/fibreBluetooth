package rain.fibreoblue;

import android.app.Application;

public class App extends Application{
	
	public static App app;
	
	@Override
	public void onCreate() {
		app=this;
		super.onCreate();
		
	}

}
