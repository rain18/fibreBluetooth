package rain.fibreoblue;

import android.app.Application;
import android.content.Context;

public class App extends Application{

	static Context context;
	public static App app;
	@Override
	public void onCreate() {
		app=this;
		context = getApplicationContext();
		super.onCreate();
	}

	public static Context getContext() {
		return context;
	}
}
